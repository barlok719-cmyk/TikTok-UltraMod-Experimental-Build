#!/usr/bin/env python3
"""Build/reuse a decoded DEX database and search many symbols in one pass."""
import argparse, hashlib, re, subprocess, zipfile
from pathlib import Path

DEX_RE = re.compile(r"^classes(?:[2-9]|[1-9][0-9]+)?\\.dex$")
CLASS_RE = re.compile(r"^\\.class\\b.*?\\s(L[^;]+;)")
METHOD_RE = re.compile(r"^\\.method\\b")
INVOKE_RE = re.compile(r"invoke-[^\\s]+\\s+\\{[^}]*\\},\\s*(L[^;]+;->[^\\s]+)")

def digest(path):
    h=hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda:f.read(1024*1024),b""): h.update(chunk)
    return h.hexdigest()

def disassemble(apk, jar, db, api):
    db.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(apk) as z:
        dexes=sorted((n for n in z.namelist() if DEX_RE.fullmatch(Path(n).name) and "/" not in n),
                     key=lambda n: 1 if n=="classes.dex" else int(re.search(r"\\d+",n).group()) if re.search(r"\\d+",n) else 999)
        for i,name in enumerate(dexes,1):
            dest=db/Path(name).stem
            marker=dest/".complete"
            if marker.exists():
                print(f"[cache] {name} already decoded",flush=True); continue
            dest.mkdir(parents=True,exist_ok=True)
            dex=db/(Path(name).stem+".dex")
            dex.write_bytes(z.read(name))
            cmd=["java","-Xmx5g","-cp",str(jar),"com.android.tools.smali.baksmali.Main","d","-a",str(api),"-j","4","-o",str(dest),str(dex)]
            print(f"[decode] {name} ({i}/{len(dexes)})",flush=True)
            p=subprocess.run(cmd,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
            dex.unlink(missing_ok=True)
            if p.returncode: raise RuntimeError(f"baksmali failed for {name}:\\n{p.stdout[-5000:]}")
            marker.write_text("ok")
    return len(dexes)

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--apk",required=True,type=Path)
    ap.add_argument("--jar",required=True,type=Path)
    ap.add_argument("--db",required=True,type=Path)
    ap.add_argument("--terms",required=True)
    ap.add_argument("--output",required=True,type=Path)
    ap.add_argument("--api",type=int,default=35)
    a=ap.parse_args()
    sha=digest(a.apk)
    dex_count=disassemble(a.apk,a.jar,a.db, a.api)
    terms=[x.strip() for x in a.terms.split(";") if x.strip()]
    class_hits={t:[] for t in terms}; method_hits={t:[] for t in terms}
    class_count=0; method_count=0; invoke_count=0
    class_rows=[]; invoke_rows=[]
    for smali in sorted(a.db.rglob("*.smali")):
        try: lines=smali.read_text(encoding="utf-8",errors="ignore").splitlines()
        except OSError: continue
        decl=next((x for x in lines[:30] if CLASS_RE.match(x)),"")
        m=CLASS_RE.match(decl); owner=m.group(1) if m else "?"
        simple=owner.rsplit("/",1)[-1].rstrip(";")
        class_count+=1
        class_rows.append((owner,str(smali.relative_to(a.db))))
        cur_sig=None; body=[]
        for line in lines:
            if METHOD_RE.match(line):
                if cur_sig is not None:
                    method_count+=1
                    joined="\\n".join(body)
                    for t in terms:
                        if t.lower() in (cur_sig+"\\n"+joined).lower() and len(method_hits[t])<250:
                            method_hits[t].append((owner,cur_sig,joined[:2200]))
                cur_sig=line; body=[line]
            elif cur_sig is not None:
                body.append(line)
                mm=INVOKE_RE.search(line)
                if mm:
                    invoke_count+=1; invoke_rows.append((owner,mm.group(1)))
                if line.startswith(".end method"):
                    method_count+=1
                    joined="\\n".join(body)
                    for t in terms:
                        if t.lower() in (cur_sig+"\\n"+joined).lower() and len(method_hits[t])<250:
                            method_hits[t].append((owner,cur_sig,joined[:2200]))
                    cur_sig=None; body=[]
        hay=(owner+"\\n"+decl).lower()
        for t in terms:
            if t.lower() in hay and len(class_hits[t])<250:
                class_hits[t].append((owner,str(smali.relative_to(a.db))))
    a.output.mkdir(parents=True,exist_ok=True)
    report=[f"# ICQ multi-target DEX search\\n\\n- APK SHA-256: \\x60{sha}\\x60\\n- DEX files: {dex_count}\\n- Decoded classes: {class_count}\\n- Methods: {method_count}\\n- Invoke references: {invoke_count}\\n- Search terms: {len(terms)}\\n"]
    for t in terms:
        report.append(f"\\n## {t}\\n\\nClass matches: {len(class_hits[t])}; method matches: {len(method_hits[t])}.\\n")
        for owner,path in class_hits[t][:80]: report.append(f"- CLASS \\x60{owner}\\x60 — \\x60{path}\\x60\\n")
        for owner,sig,body in method_hits[t][:40]:
            report.append(f"\\n### \\x60{owner}\\x60 {sig}\\n\\n\\x60\\x60\\x60smali\\n{body}\\n\\x60\\x60\\x60\\n")
    (a.output/"multi-target-search.md").write_text("".join(report),encoding="utf-8")
    (a.output/"class-index.tsv").write_text("owner\\tfile\\n"+"".join(f"{o}\\t{p}\\n" for o,p in class_rows),encoding="utf-8")
    (a.output/"invoke-index.tsv").write_text("caller\\tcallee\\n"+"".join(f"{o}\\t{v}\\n" for o,v in invoke_rows),encoding="utf-8")
    (a.output/"provenance.md").write_text(f"APK SHA-256: {sha}\\nDEX database: decoded from this exact APK and cached by SHA-256.\\nRead-only static analysis. No APK modifications or builds.\\n",encoding="utf-8")
    print(f"[done] classes={class_count}, methods={method_count}, terms={len(terms)}, sha256={sha}",flush=True)

if __name__=="__main__": main()
