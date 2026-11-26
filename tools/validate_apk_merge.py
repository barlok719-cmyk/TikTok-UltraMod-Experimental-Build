#!/usr/bin/env python3
"""Verify that APKEditor retained all DEX and native-library payloads from input APK splits."""
from __future__ import annotations

import argparse
import hashlib
import sys
import zipfile
from pathlib import Path


def payloads(apk: Path) -> dict[str, set[str]]:
    found = {"dex": set(), "native": set(), "assets": set()}
    try:
        with zipfile.ZipFile(apk) as archive:
            bad = archive.testzip()
            if bad:
                raise ValueError(f"corrupt ZIP entry: {bad}")
            for info in archive.infolist():
                name = info.filename
                if info.is_dir():
                    continue
                kind = None
                if name.endswith(".dex") and "/" not in name:
                    kind = "dex"
                elif name.startswith("lib/") and name.endswith(".so"):
                    kind = "native"
                elif name.startswith("assets/"):
                    kind = "assets"
                if kind:
                    found[kind].add(hashlib.sha256(archive.read(info)).hexdigest())
    except (OSError, zipfile.BadZipFile) as exc:
        raise ValueError(f"{apk.name}: not a readable APK archive ({exc})") from exc
    return found


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-dir", required=True, type=Path)
    parser.add_argument("--merged-apk", required=True, type=Path)
    parser.add_argument(
        "--require-universal-original",
        action="store_true",
        help="Require one standalone source APK and both ARM64 and ARMv7 libraries in the merged APK.",
    )
    args = parser.parse_args()

    sources = sorted(args.source_dir.glob("*.apk"))
    if not sources:
        print("ERROR: no source APK splits found", file=sys.stderr)
        return 2

    expected = {"dex": set(), "native": set(), "assets": set()}
    for apk in sources:
        parts = payloads(apk)
        expected["dex"].update(parts["dex"])
        expected["native"].update(parts["native"])
        expected["assets"].update(parts["assets"])

    actual = payloads(args.merged_apk)
    missing = {kind: expected[kind] - actual[kind] for kind in expected}

    errors = []
    if args.require_universal_original:
        if len(sources) != 1:
            errors.append(
                f"original TikTok must have exactly one standalone source APK; found {len(sources)}"
            )
        try:
            with zipfile.ZipFile(args.merged_apk) as archive:
                names = [info.filename for info in archive.infolist() if not info.is_dir()]
            for abi in ("arm64-v8a", "armeabi-v7a"):
                if not any(name.startswith(f"lib/{abi}/") and name.endswith(".so") for name in names):
                    errors.append(f"merged original APK is missing {abi} native libraries")
        except (OSError, zipfile.BadZipFile) as exc:
            errors.append(f"merged original APK cannot be inspected ({exc})")

    print(f"Source APK files checked: {len(sources)}")
    print(f"Unique source DEX payloads: {len(expected['dex'])}; merged DEX payloads: {len(actual['dex'])}")
    print(f"Unique source native libraries: {len(expected['native'])}; merged native libraries: {len(actual['native'])}")
    print(f"Unique source assets: {len(expected['assets'])}; merged assets: {len(actual['assets'])}")

    for kind, digests in missing.items():
        if digests:
            errors.append(f"{kind}: {len(digests)} source payload(s) missing from merged APK")
    if not expected["dex"]:
        errors.append("source set contains no DEX files")
    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1

    print("PASS: every unique source DEX, native-library, and asset payload is present byte-for-byte in the merged APK.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
