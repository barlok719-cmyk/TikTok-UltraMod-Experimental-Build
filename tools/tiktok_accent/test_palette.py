import hashlib
import json
import struct
import unittest
import zlib

from tools.tiktok_accent.apply import serialize_pairs
from tools.tiktok_accent.palette import captures, map_colour
from tools.tiktok_accent.arsc import Arsc
from tools.tiktok_accent.dex import rewrite_smali_text, _validate_rebuilt_dex, _candidate_dex

REFERENCE = 0xFFFE2C55


class PaletteTests(unittest.TestCase):
    def test_reference_maps_exactly_to_target(self):
        target = 0xFF6750A4
        self.assertEqual(map_colour(REFERENCE, REFERENCE, target), target)

    def test_alpha_is_preserved(self):
        source = 0x66FE2C55
        mapped = map_colour(source, REFERENCE, 0xFF6750A4)
        self.assertEqual(mapped >> 24, 0x66)

    def test_cyan_is_not_in_red_family(self):
        cyan = 0xFF25F4EE
        self.assertFalse(captures(cyan, REFERENCE))
        self.assertEqual(map_colour(cyan, REFERENCE, 0xFF6750A4), cyan)

    def test_neutral_gray_is_not_in_red_family(self):
        gray = 0xFF777777
        self.assertFalse(captures(gray, REFERENCE))

    def test_original_accent_is_noop(self):
        shade = 0xFFFF1764
        self.assertEqual(map_colour(shade, REFERENCE, REFERENCE), shade)

    def test_runtime_pairs_are_stable_and_keep_both_colours(self):
        pairs = {
            0xFF6750A4: 0xFFFE2C55,
            0x806750A4: 0x80FE2C55,
        }
        payload = json.loads(serialize_pairs(pairs))
        self.assertEqual(payload["format"], 1)
        self.assertEqual(payload["reference"], "FE2C55")
        self.assertEqual(
            payload["pairs"],
            {
                "0x806750A4": "0x80FE2C55",
                "0xFF6750A4": "0xFFFE2C55",
            },
        )




class DexHeaderValidationTests(unittest.TestCase):
    @staticmethod
    def _valid_dex040():
        data = bytearray(112)
        data[:8] = b"dex\n040\0"
        struct.pack_into("<I", data, 32, len(data))
        struct.pack_into("<I", data, 36, 112)
        struct.pack_into("<I", data, 40, 0x12345678)
        data[12:32] = hashlib.sha1(data[32:]).digest()
        struct.pack_into("<I", data, 8, zlib.adler32(data[12:]) & 0xFFFFFFFF)
        return bytes(data)

    def test_accepts_valid_dex040_header(self):
        _validate_rebuilt_dex(self._valid_dex040(), "classes12.dex")

    def test_rejects_dex041_output(self):
        data = bytearray(self._valid_dex040())
        data[4:7] = b"041"
        with self.assertRaisesRegex(RuntimeError, "version 041"):
            _validate_rebuilt_dex(bytes(data), "classes.dex")


class DexAccentTests(unittest.TestCase):
    def test_only_audited_colour_constants_are_rewritten(self):
        source = (
            ".method public static test()V\n"
            "    const v0, -0x1d3ab\n"
            "    const v1, -0xda0b12\n"
            ".end method\n"
        )
        known = {0xFFFE2C55, 0xFF25F4EE}
        updated, count = rewrite_smali_text(source, 0xFF8E44AD, known)
        self.assertEqual(count, 1)
        self.assertIn("const v0, -0x71bb53", updated)
        self.assertIn("const v1, -0xda0b12", updated)


class DexCandidateScanTests(unittest.TestCase):
    def test_finds_even_aligned_const31i_colour_literal(self):
        data = bytearray(12)
        data[0] = 0x14  # const/31i opcode
        struct.pack_into("<I", data, 2, REFERENCE)
        self.assertTrue(_candidate_dex(bytes(data), {REFERENCE}))

    def test_ignores_odd_aligned_candidate(self):
        data = bytearray(12)
        data[1] = 0x14
        struct.pack_into("<I", data, 3, REFERENCE)
        self.assertFalse(_candidate_dex(bytes(data), {REFERENCE}))

    def test_returns_false_when_no_audited_literal_is_present(self):
        data = bytearray(12)
        data[0] = 0x14
        struct.pack_into("<I", data, 2, 0xFF25F4EE)
        self.assertFalse(_candidate_dex(bytes(data), {REFERENCE}))


class ResourceBagTests(unittest.TestCase):
    def test_style_bag_colour_values_are_exposed_and_writable(self):
        # One type chunk, one complex entry, two ResTable_map colour values.
        header_size = 84
        entries_start = 88
        data = bytearray(entries_start + 16 + 2 * 12)
        struct.pack_into("<HHI", data, 0, 0x0201, header_size, len(data))
        data[8] = 1
        struct.pack_into("<I", data, 12, 1)
        struct.pack_into("<I", data, 16, entries_start)
        struct.pack_into("<I", data, 20, 64)
        struct.pack_into("<I", data, header_size, 0)
        struct.pack_into("<HHIII", data, entries_start, 16, 0x0001, 0, 0, 2)
        first_value = entries_start + 16 + 4
        second_value = entries_start + 28 + 4
        struct.pack_into("<I", data, entries_start + 16, 0x01010000)
        struct.pack_into("<HBBI", data, first_value, 8, 0, 0x1C, 0xFFFE2C55)
        struct.pack_into("<I", data, entries_start + 28, 0x01010001)
        struct.pack_into("<HBBI", data, second_value, 8, 0, 0x1C, 0xFF25F4EE)

        arsc = Arsc.__new__(Arsc)
        arsc.data = data
        values = arsc._entry_values(0, 0, "style")

        self.assertEqual([value.data for value in values], [0xFFFE2C55, 0xFF25F4EE])
        arsc.set_value(values[0], 0x1C, 0xFF8E44AD)
        self.assertEqual(struct.unpack_from("<I", arsc.data, first_value + 4)[0], 0xFF8E44AD)
        self.assertEqual(struct.unpack_from("<I", arsc.data, second_value + 4)[0], 0xFF25F4EE)


if __name__ == "__main__":
    unittest.main()
