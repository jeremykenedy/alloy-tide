"""Encode the pinned CC0 studio HDR as an RGBM texture for GLES2."""

import hashlib
import math
from pathlib import Path

import bpy

ROOT = Path(__file__).resolve().parents[1]
source = ROOT / "build/source-media/studio_small_09_1k.hdr"
expected = "e7cfda5f4e98e623db12b8bfd0184e048488e4855d9c83e2751fb44a32e80c45"
if hashlib.sha256(source.read_bytes()).hexdigest() != expected:
    raise ValueError("Unexpected studio HDR source")
image = bpy.data.images.load(str(source))
image.scale(512, 256)
pixels = list(image.pixels)
for offset in range(0, len(pixels), 4):
    peak = max(pixels[offset : offset + 3])
    multiplier = max(1 / 255, min(1, math.ceil(peak / 16 * 255) / 255))
    for channel in range(3):
        pixels[offset + channel] = min(1, pixels[offset + channel] / (multiplier * 16))
    pixels[offset + 3] = multiplier
result = bpy.data.images.new("Studio RGBM", width=512, height=256, alpha=True)
result.colorspace_settings.name = "Non-Color"
result.alpha_mode = "STRAIGHT"
result.pixels = pixels
result.filepath_raw = str(ROOT / "assets/studio-rgbm.png")
result.file_format = "PNG"
result.save()
