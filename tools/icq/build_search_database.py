#!/usr/bin/env python3
"""Build/reuse decoded DEX files and run exact-token multi-target searches."""
import argparse, gzip, hashlib, re, subprocess, zipfile
from pathlib import Path

DEX_RE = re.compile(r"^classes(?:[2-9]|[1-9][0-9]+)?\.dex$")
CLASS_RE = re.compile(r"^\.class\b.*?\s(L[^;]+;)")
METHOD_RE = re.compile(r"^\.method\b")
INVOKE_RE = re.compile(r"invoke-[^\s]+\s+\{[^}]*\},\s*(L[^;]+;->[^\s]+)")

def digest(path):
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()

def disassemble(apk, jar, db, api):
    db.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(apk) as z:
        dexes = sorted(
            (n for n in z.namelist() if DEX_RE.fullmatch(Path(n).name) and "/" not in n),
            key=lambda n: 1 if n == "classes.dex" else int(re.search(r"\d+", n).group()) if re.search(r"\d+", n) else 999
        )
        for i, name in enumerate(dexes, 1):
            dest = db / Path(name).stem
            marker = dest / ".complete"
            if marker.exists():
                print(f"[cache] {name} already decoded", flush=True)
                continue
            dest.mkdir(parents=True, exist_ok=True)
            dex = db / (Path(name).stem + ".dex")
            dex.write_bytes(z.read(name))
            cmd = ["java", "-Xmx5g", "-cp", str(jar), "com.android.tools.smali.baksmali.Main",
                   "d", "-a", str(api), "-j", "4", "-o", str(dest), str(dex)]
            print(f"[decode] {name} ({i}/{len(dexes)})", flush=True)
            p = subprocess.run(cmd, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            dex.unlink(missing_ok=True)
            if p.returncode:
                raise RuntimeError(f"baksmali failed for {name}:\n{p.stdout[-5000:]}")
            marker.write_text("ok")
    return len(dexes)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apk", required=True, type=Path)
    ap.add_argument("--jar", required=True, type=Path)
    ap.add_argument("--db", required=True, type=Path)
    ap.add_argument("--terms", required=True)
    ap.add_argument("--output", required=True, type=Path)
    ap.add_argument("--api", type=int, default=35)
    ap.add_argument("--focus-classes", default="", help="Semicolon-separated exact class descriptors to dump, e.g. LX/0YtY;LX/1E00")
    a = ap.parse_args()
    sha = digest(a.apk)
    dex_count = disassemble(a.apk, a.jar, a.db, a.api)
    terms = [x.strip() for x in a.terms.split(";") if x.strip()]
    patterns = {t: re.compile(r"(?<![A-Za-z0-9])" + re.escape(t) + r"(?![A-Za-z0-9])", re.I) for t in terms}
    class_hits = {t: [] for t in terms}
    method_hits = {t: [] for t in terms}
    class_count = method_count = invoke_count = 0
    class_rows, invoke_rows = [], []
    for smali in sorted(a.db.rglob("*.smali")):
        try:
            lines = smali.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue
        decl = next((x for x in lines[:30] if CLASS_RE.match(x)), "")
        match = CLASS_RE.match(decl)
        owner = match.group(1) if match else "?"
        class_count += 1
        class_rows.append((owner, str(smali.relative_to(a.db))))
        cur_sig, body = None, []
        for line in lines:
            if METHOD_RE.match(line):
                cur_sig, body = line, [line]
            elif cur_sig is not None:
                body.append(line)
                inv = INVOKE_RE.search(line)
                if inv:
                    invoke_count += 1
                    callee = inv.group(1)
                    if any(p.search(callee) for p in patterns.values()):
                        invoke_rows.append((owner, callee))
                if line.startswith(".end method"):
                    method_count += 1
                    joined = "\n".join(body)
                    for term, pattern in patterns.items():
                        if pattern.search(joined) and len(method_hits[term]) < 150:
                            method_hits[term].append((owner, cur_sig, joined[:2600]))
                    cur_sig, body = None, []
        for term, pattern in patterns.items():
            if pattern.search(owner) and len(class_hits[term]) < 500:
                class_hits[term].append((owner, str(smali.relative_to(a.db))))
    a.output.mkdir(parents=True, exist_ok=True)
    focus = {x.strip().strip("L;") for x in a.focus_classes.split(";") if x.strip()}
    if focus:
        focus_dir = a.output / "focused-smali"
        focus_dir.mkdir(parents=True, exist_ok=True)
        copied = []
        for smali in sorted(a.db.rglob("*.smali")):
            try:
                lines = smali.read_text(encoding="utf-8", errors="ignore").splitlines()
            except OSError:
                continue
            decl = next((x for x in lines[:30] if CLASS_RE.match(x)), "")
            match = CLASS_RE.match(decl)
            owner = match.group(1).strip("L;") if match else ""
            if owner in focus:
                target = focus_dir / (owner.replace("/", "_") + ".smali")
                target.write_text("\n".join(lines), encoding="utf-8")
                copied.append((owner, str(target.relative_to(a.output))))
        (a.output / "focused-class-manifest.tsv").write_text(
            "descriptor\tfile\n" + "".join(f"{owner}\t{path}\n" for owner, path in copied),
            encoding="utf-8"
        )
        print(f"[focus] dumped {len(copied)}/{len(focus)} exact classes", flush=True)
    report = [f"# ICQ multi-target DEX search (exact-token v2)\n\n- APK SHA-256: {sha}\n- DEX files: {dex_count}\n- Decoded classes: {class_count}\n- Methods: {method_count}\n- Invoke references scanned: {invoke_count}\n- Relevant invoke references retained: {len(invoke_rows)}\n- Search terms: {len(terms)}\n"]
    for term in terms:
        report.append(f"\n## {term}\n\nClass matches: {len(class_hits[term])}; method matches (capped at 150): {len(method_hits[term])}.\n")
        for owner, path in class_hits[term][:100]:
            report.append(f"- CLASS {owner} — {path}\n")
        for owner, sig, body in method_hits[term]:
            report.append(f"\n### {owner} {sig}\n\nSMALI:\n{body}\n")
    (a.output / "multi-target-search.md").write_text("".join(report), encoding="utf-8")
    with gzip.open(a.output / "class-index.tsv.gz", "wt", encoding="utf-8") as f:
        f.write("owner\tfile\n")
        for owner, path in class_rows:
            f.write(f"{owner}\t{path}\n")
    with gzip.open(a.output / "relevant-invoke-index.tsv.gz", "wt", encoding="utf-8") as f:
        f.write("caller\tcallee\n")
        for owner, callee in invoke_rows:
            f.write(f"{owner}\t{callee}\n")
    (a.output / "provenance.md").write_text(
        f"APK SHA-256: {sha}\nDEX database: decoded from this exact APK and cached by SHA-256.\n"
        "Read-only static analysis. No APK modifications or builds.\n"
        "Search matching: case-insensitive exact token boundaries; method snippets emitted once per method.\n",
        encoding="utf-8"
    )
    print(f"[done] classes={class_count}, methods={method_count}, terms={len(terms)}, sha256={sha}", flush=True)

if __name__ == "__main__":
    main()
