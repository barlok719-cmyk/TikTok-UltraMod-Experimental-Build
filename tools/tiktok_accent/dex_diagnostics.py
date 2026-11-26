#!/usr/bin/env python3
"""Collect focused Smali evidence for TikTok profile-background and seekbar fingerprints."""
from __future__ import annotations

import argparse
import hashlib
import re
import shutil
import sys
import tempfile
from pathlib import Path

from .apkzip import Apk
from .dex import _run_smali

DEX_NAME = re.compile(r"^classes(?:[2-9]|[1-9][0-9]+)?\.dex$")
MARKERS = {
    "seekbar_message": b"can not show seekbar, state: 1, not in resume",
    "profile_background_key": b"profile_bg_in_allow_list",
    "profile_background_prefix": b"profile_bg",
    "seekbar_text": b"seekbar",
    "old_seekbar_holder": b"LX/06Bt;",
    "observed_seekbar_holder_48_0_2": b"LX/05zi;",
    "old_profile_holder": b"LX/0Ngv;",
    "old_profile_snapshot": b"LX/0NhE;",
}
TARGET_CLASSES = {
    "LX/06Bt;", "LX/05zi;", "LX/0Ngv;", "LX/0NhE;",
}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", type=Path)
    parser.add_argument("--smali-jar", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--api", type=int, default=35)
    args = parser.parse_args()

    args.output.mkdir(parents=True, exist_ok=True)
    candidates_dir = args.output / "candidate-smali"
    candidates_dir.mkdir(exist_ok=True)
    report = [
        "TikTok DEX fingerprint diagnostics",
        f"APK: {args.apk.name}",
        f"APK SHA256: {hashlib.sha256(args.apk.read_bytes()).hexdigest()}",
        "",
    ]
    apk = None
    try:
        apk = Apk(str(args.apk))
        dex_names = sorted(n for n in apk.names() if DEX_NAME.fullmatch(n))
        report.append(f"DEX files: {len(dex_names)}")
        if not dex_names:
            raise RuntimeError("APK contains no classes*.dex files")

        with tempfile.TemporaryDirectory(prefix="tiktok-dex-fingerprint-") as temp:
            temp_root = Path(temp)
            analyzed = 0
            for dex_index, dex_name in enumerate(dex_names, 1):
                raw = apk.read(dex_name)
                hits = [name for name, marker in MARKERS.items() if marker in raw]
                if not hits:
                    continue

                analyzed += 1
                report.append(f"\n[{dex_name}] raw marker hits: {', '.join(hits)}")
                dex_path = temp_root / dex_name
                dex_path.write_bytes(raw)
                smali_dir = temp_root / f"smali-{dex_index}"
                _run_smali(
                    args.smali_jar,
                    "com.android.tools.smali.baksmali.Main",
                    ["d", "-a", str(args.api), "-j", "4", "-o", str(smali_dir), str(dex_path)],
                )

                kept = 0
                for smali_file in smali_dir.rglob("*.smali"):
                    source = smali_file.read_text(encoding="utf-8", errors="replace")
                    class_match = re.search(r"^\.class\s+[^\n]*?\s(L[^;]+;)", source, re.MULTILINE)
                    descriptor = class_match.group(1) if class_match else ""
                    lower = source.lower()
                    relevant = (
                        any(marker.decode("ascii", "ignore").lower() in lower
                            for marker in (MARKERS["seekbar_message"], MARKERS["profile_background_prefix"]))
                        or descriptor in TARGET_CLASSES
                        or "lx/05zi;->lji(" in lower
                        or "lx/06bt;->lji(" in lower
                    )
                    if not relevant:
                        continue

                    relative = smali_file.relative_to(smali_dir)
                    destination = candidates_dir / dex_name / relative
                    destination.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(smali_file, destination)
                    kept += 1
                    report.append(f"  class: {descriptor or relative.as_posix()} | file: {dex_name}/{relative.as_posix()}")

                report.append(f"  retained Smali classes: {kept}")

            report.extend([
                "",
                f"DEX files disassembled: {analyzed}",
                "Only DEX files containing one or more target markers were disassembled.",
                "candidate-smali/ contains complete relevant class files for source-level fingerprint review.",
            ])
        (args.output / "fingerprint-report.txt").write_text("\n".join(report) + "\n", encoding="utf-8")
        print("\n".join(report), flush=True)
        return 0
    except Exception as exc:
        report.extend(["", f"DIAGNOSTIC ERROR: {type(exc).__name__}: {exc}"])
        (args.output / "fingerprint-report.txt").write_text("\n".join(report) + "\n", encoding="utf-8")
        print(f"DEX fingerprint diagnostics failed: {type(exc).__name__}: {exc}", file=sys.stderr, flush=True)
        return 1
    finally:
        if apk is not None:
            apk.close()


if __name__ == "__main__":
    raise SystemExit(main())
