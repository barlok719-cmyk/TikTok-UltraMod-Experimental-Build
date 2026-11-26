#!/usr/bin/env python3
"""Bake a TikTok accent into typed Android color resources and compiled XML.

Adapted from TikTok MI (GPL-3.0); see NOTICE.md.
This stage intentionally does not rewrite DEX constants. It is an opt-in,
build-time resource pass and leaves the original merged APK untouched.
"""
from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path
from typing import Dict

from .apkzip import Apk, STORED
from .arsc import Arsc
from .accent import bake

REFERENCE = 0xFFFE2C55


def parse_hex(value: str) -> int:
    value = value.strip().lstrip("#")
    if len(value) != 6:
        raise argparse.ArgumentTypeError("accent must be exactly six HEX digits, e.g. 6750A4")
    try:
        return 0xFF000000 | int(value, 16)
    except ValueError as exc:
        raise argparse.ArgumentTypeError("accent contains non-HEX characters") from exc


def serialize_pairs(pairs: Dict[int, int]) -> str:
    """Serialize baked-colour -> original-colour pairs for later runtime use."""
    payload = {
        "format": 1,
        "reference": "FE2C55",
        "pairs": {
            "0x%08X" % baked: "0x%08X" % original
            for baked, original in sorted(pairs.items())
        },
    }
    return json.dumps(payload, ensure_ascii=True, indent=2) + "\n"


def write_pairs(path: Path, pairs: Dict[int, int]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(path.name + ".tmp")
    try:
        temporary.write_text(serialize_pairs(pairs), encoding="utf-8")
        os.replace(temporary, path)
    finally:
        if temporary.exists():
            temporary.unlink()


def run(
    source: Path,
    destination: Path,
    accent: int,
    dry_run: bool = False,
    pairs_output: Path | None = None,
) -> list[str]:
    if not source.is_file():
        raise FileNotFoundError(f"input APK not found: {source}")
    if source.resolve() == destination.resolve() and not dry_run:
        raise ValueError("input and output must be different files; original APK is kept intact")

    apk = Apk(str(source))
    try:
        if not apk.has("resources.arsc"):
            raise RuntimeError("APK has no resources.arsc; refusing to continue")

        arsc = Arsc(apk.read("resources.arsc"))
        pairs: Dict[int, int] = {}
        report = bake(apk, arsc, REFERENCE, accent, pairs)

        if dry_run or accent == REFERENCE:
            return report

        apk.replace("resources.arsc", arsc.build(), method=STORED)
        removed = apk.drop_signature()
        report.append(f"removed stale APK signature entries: {len(removed)}")

        destination.parent.mkdir(parents=True, exist_ok=True)
        temporary = destination.with_name(destination.name + ".tmp")
        try:
            apk.write(str(temporary))
            os.replace(temporary, destination)
        finally:
            if temporary.exists():
                temporary.unlink()

        if pairs_output is not None:
            write_pairs(pairs_output, pairs)
            report.append(f"saved runtime colour map: {len(pairs)} pairs -> {pairs_output}")

        report.append(f"wrote: {destination}")
        return report
    finally:
        apk.close()


def main() -> int:
    parser = argparse.ArgumentParser(description="Recolor TikTok's red accent family in APK resources.")
    parser.add_argument("input", type=Path, help="unsigned merged TikTok APK")
    parser.add_argument("accent", type=parse_hex, help="target accent as RRGGBB")
    parser.add_argument("--output", type=Path, help="output APK; input is never overwritten")
    parser.add_argument(
        "--pairs-output",
        type=Path,
        help="optional JSON file containing baked-colour -> original-colour pairs",
    )
    parser.add_argument("--dry-run", action="store_true", help="count matches without writing an APK")
    args = parser.parse_args()

    if not args.dry_run and args.output is None:
        parser.error("--output is required unless --dry-run is used")

    try:
        report = run(
            args.input,
            args.output or args.input,
            args.accent,
            args.dry_run,
            args.pairs_output,
        )
    except Exception as exc:
        print(f"ERROR: {type(exc).__name__}: {exc}", file=sys.stderr)
        return 1

    print(f"Accent: #{args.accent & 0xFFFFFF:06X}")
    for line in report:
        print(f"  {line}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
