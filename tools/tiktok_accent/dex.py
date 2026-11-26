#!/usr/bin/env python3
"""Recolour TikTok accent constants embedded in DEX bytecode.

The resource pass cannot reach integer colour literals compiled into methods.
This pass disassembles only DEX files that contain candidate const/31i
instructions, rewrites confirmed members of the resource pass's source palette,
and assembles only files that actually changed.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import struct
import subprocess
import sys
import tempfile
import time
import zlib
from pathlib import Path

from .apkzip import Apk
from .palette import map_colour

REFERENCE = 0xFFFE2C55
DEX_NAME = re.compile(r"^classes(?:[2-9]|[1-9][0-9]+)?\.dex$")
CONST_LINE = re.compile(
    r"^(\s*)const\s+([vp]\d+)\s*,\s*(-?0x[0-9a-fA-F]+)(\s*(?:#.*)?)$"
)


def _signed_hex(value: int) -> str:
    value &= 0xFFFFFFFF
    if value & 0x80000000:
        return "-0x%x" % ((1 << 32) - value)
    return "0x%x" % value


def _candidate_dex(data: bytes, known_sources: set[int]) -> bool:
    """Find candidate const/31i encodings without a Python-level full-file byte loop.

    bytes.find() performs the opcode search in optimized native code. We retain
    the original even-offset check and exact literal comparison; baksmali still
    makes the final decision about whether a match is an instruction.
    """
    position = 0
    last_start = len(data) - 6
    while position <= last_start:
        at = data.find(b"\x14", position)
        if at < 0 or at > last_start:
            return False
        if (at & 1) == 0:
            value = struct.unpack_from("<I", data, at + 2)[0]
            if value in known_sources:
                return True
        position = at + 1
    return False


def _validate_rebuilt_dex(data: bytes, name: str = "<memory>") -> None:
    """Validate standard DEX output and reject the experimental DEX 041 container format."""
    if len(data) < 112 or data[:4] != b"dex\n" or data[7:8] != b"\0":
        raise RuntimeError("reassembled DEX has an invalid magic/header: %s" % name)
    version_bytes = data[4:7]
    if not version_bytes.isdigit():
        raise RuntimeError("reassembled DEX has an invalid version: %s" % name)
    version = int(version_bytes)
    if version > 40:
        raise RuntimeError("reassembled DEX uses unsupported version %03d (expected <=040): %s" % (version, name))
    file_size = struct.unpack_from("<I", data, 32)[0]
    header_size = struct.unpack_from("<I", data, 36)[0]
    endian_tag = struct.unpack_from("<I", data, 40)[0]
    if file_size != len(data) or header_size != 112 or endian_tag != 0x12345678:
        raise RuntimeError("reassembled DEX header validation failed for %s (file_size=%d/%d, header_size=%d, endian=0x%08x)" % (name, file_size, len(data), header_size, endian_tag))
    if data[12:32] != hashlib.sha1(data[32:]).digest():
        raise RuntimeError("reassembled DEX SHA-1 signature mismatch: %s" % name)
    checksum = struct.unpack_from("<I", data, 8)[0]
    if checksum != (zlib.adler32(data[12:]) & 0xFFFFFFFF):
        raise RuntimeError("reassembled DEX Adler-32 checksum mismatch: %s" % name)


def rewrite_smali_text(source: str, accent: int, known_sources: set[int]) -> tuple[str, int]:
    """Replace only full-width const instructions holding audited TikTok colours."""
    if accent == REFERENCE or not known_sources:
        return source, 0

    changed = 0
    output = []
    for line in source.splitlines(keepends=True):
        body = line.rstrip("\r\n")
        ending = line[len(body):]
        match = CONST_LINE.match(body)
        if not match:
            output.append(line)
            continue
        try:
            original = int(match.group(3), 16) & 0xFFFFFFFF
        except ValueError:
            output.append(line)
            continue

        if original not in known_sources:
            output.append(line)
            continue

        mapped = map_colour(original, REFERENCE, accent)
        if mapped == original:
            output.append(line)
            continue

        output.append(
            "%sconst %s, %s%s%s"
            % (match.group(1), match.group(2), _signed_hex(mapped), match.group(4), ending)
        )
        changed += 1

    return "".join(output), changed


def _run_smali(jar: Path, main: str, args: list[str]) -> None:
    env = os.environ.copy()
    # Keep the workflow's global JVM options from being applied twice.
    env.pop("JAVA_TOOL_OPTIONS", None)
    command = ["java", "-Xmx4g", "-cp", str(jar), main, *args]
    tool_name = main.rsplit(".", 1)[-1]
    print("[DEX] Starting Java: %s" % tool_name, flush=True)

    # Stream tool output into Actions logs instead of buffering potentially
    # large Smali output until the entire subprocess exits.
    tail: list[str] = []
    with subprocess.Popen(
        command,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        bufsize=1,
        env=env,
    ) as process:
        assert process.stdout is not None
        for line in process.stdout:
            print("[smali] " + line.rstrip(), flush=True)
            tail.append(line.rstrip())
            if len(tail) > 80:
                del tail[: len(tail) - 80]
        return_code = process.wait()

    if return_code:
        details = "\n".join(tail)
        raise RuntimeError(
            "%s failed with exit code %d; last output:\n%s"
            % (tool_name, return_code, details)
        )
    print("[DEX] Java completed successfully: %s" % tool_name, flush=True)


def patch_apk(source: Path, destination: Path, accent: int, pairs_file: Path,
              smali_jar: Path, api: int = 35, assemble_api: int = 34,
              require_hits: bool = False) -> int:
    payload = json.loads(pairs_file.read_text(encoding="utf-8"))
    if payload.get("format") != 1 or payload.get("reference") != "FE2C55":
        raise ValueError("unsupported accent map format or reference colour")
    known_sources = {
        int(value, 16) & 0xFFFFFFFF
        for value in payload.get("pairs", {}).values()
    }
    known_sources.add(REFERENCE)

    if not smali_jar.is_file():
        raise FileNotFoundError("smali/apktool CLI jar not found: %s" % smali_jar)
    if source.resolve() == destination.resolve():
        raise ValueError("input and output APK paths must differ")

    apk = Apk(str(source))
    total = 0
    touched = []
    try:
        dex_names = sorted(name for name in apk.names() if DEX_NAME.match(name))
        if not dex_names:
            raise RuntimeError("APK contains no classes*.dex files")

        print(
            "[DEX] APK contains %d DEX files; audited colour literals: %d"
            % (len(dex_names), len(known_sources)),
            flush=True,
        )

        with tempfile.TemporaryDirectory(prefix="tiktok-accent-dex-") as temp:
            root = Path(temp)
            for index, name in enumerate(dex_names, start=1):
                started = time.monotonic()
                print("[DEX] [%d/%d] Reading %s" % (index, len(dex_names), name), flush=True)
                raw = apk.read(name)
                print(
                    "[DEX] [%d/%d] %s read (%s bytes); scanning candidate literals"
                    % (index, len(dex_names), name, len(raw)),
                    flush=True,
                )
                if not _candidate_dex(raw, known_sources):
                    print(
                        "[DEX] [%d/%d] %s: no candidate constants (%.1fs)"
                        % (index, len(dex_names), name, time.monotonic() - started),
                        flush=True,
                    )
                    continue

                print("[DEX] [%d/%d] %s: candidate found; starting disassembly" % (index, len(dex_names), name), flush=True)
                dex_in = root / ("input-%d.dex" % index)
                smali_dir = root / ("smali-%d" % index)
                dex_out = root / ("output-%d.dex" % index)
                dex_in.write_bytes(raw)

                _run_smali(
                    smali_jar,
                    "com.android.tools.smali.baksmali.Main",
                    ["d", "-a", str(api), "-j", "4", "-o", str(smali_dir), str(dex_in)],
                )

                changed_here = 0
                smali_files = list(smali_dir.rglob("*.smali"))
                print(
                    "[DEX] %s: disassembled %d Smali files; checking constants"
                    % (name, len(smali_files)),
                    flush=True,
                )
                for smali_file in smali_files:
                    original_text = smali_file.read_text(encoding="utf-8")
                    updated_text, count = rewrite_smali_text(original_text, accent, known_sources)
                    if count:
                        smali_file.write_text(updated_text, encoding="utf-8", newline="\n")
                        changed_here += count

                print("[DEX] %s: %d constants selected for replacement" % (name, changed_here), flush=True)
                if not changed_here:
                    print(
                        "[DEX] %s: no Smali constants matched after disassembly (%.1fs)"
                        % (name, time.monotonic() - started),
                        flush=True,
                    )
                    continue

                print("[DEX] %s: starting DEX assembly" % name, flush=True)
                _run_smali(
                    smali_jar,
                    "com.android.tools.smali.smali.Main",
                    ["a", "-a", str(assemble_api), "-j", "4", "-o", str(dex_out), str(smali_dir)],
                )
                rebuilt = dex_out.read_bytes()
                _validate_rebuilt_dex(rebuilt, name)
                apk.replace(name, rebuilt)
                total += changed_here
                touched.append("%s:%d" % (name, changed_here))
                print(
                    "[DEX] %s: rebuilt and validated; elapsed %.1fs"
                    % (name, time.monotonic() - started),
                    flush=True,
                )

        if require_hits and total == 0:
            raise RuntimeError(
                "no audited TikTok accent constants were found in DEX; refusing to ship an unchanged accent"
            )

        print("[DEX] Writing patched APK: %s" % destination, flush=True)
        apk.write(str(destination))
        print("[DEX] Patched APK write completed", flush=True)
    finally:
        apk.close()

    print("DEX accent: #%06X" % (accent & 0xFFFFFF), flush=True)
    print("Audited source colours: %d" % len(known_sources), flush=True)
    print("Rewritten DEX constants: %d" % total, flush=True)
    for item in touched:
        print("  %s" % item, flush=True)
    print("Wrote: %s" % destination, flush=True)
    return total


def main() -> int:
    parser = argparse.ArgumentParser(description="Recolour TikTok's audited accent constants in DEX.")
    parser.add_argument("input", type=Path)
    parser.add_argument("accent", help="target accent as RRGGBB")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--pairs-file", type=Path, required=True)
    parser.add_argument("--smali-jar", type=Path, required=True)
    parser.add_argument("--api", type=int, default=35,
                        help="Android API used to disassemble input DEX (default: 35)")
    parser.add_argument("--assemble-api", type=int, default=34,
                        help="Android API used to assemble output DEX; 34 avoids malformed DEX 041 output")
    parser.add_argument("--require-hits", action="store_true")
    args = parser.parse_args()

    try:
        value = args.accent.strip().lstrip("#")
        if not re.fullmatch(r"[0-9A-Fa-f]{6}", value):
            raise ValueError("accent must be exactly six HEX digits")
        accent = 0xFF000000 | int(value, 16)
        patch_apk(args.input, args.output, accent, args.pairs_file,
                  args.smali_jar, args.api, args.assemble_api, args.require_hits)
    except Exception as exc:
        print("ERROR: %s: %s" % (type(exc).__name__, exc), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
