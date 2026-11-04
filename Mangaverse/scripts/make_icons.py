#!/usr/bin/env python3
"""Draw the app icons (PWA, Apple touch, favicon) from the logo: a warm gradient tile with an "M"."""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/static/img"
FONT = "/usr/share/fonts/opentype/inter/Inter-Bold.otf"
TOP, MID, BOTTOM = (255, 176, 122), (255, 138, 76), (232, 98, 42)
INK = (26, 13, 5)


def gradient(size):
    tile = Image.new("RGB", (size, size))
    px = tile.load()
    for y in range(size):
        for x in range(size):
            t = (x + y) / (2 * (size - 1))
            a, b, k = (TOP, MID, t / 0.55) if t < 0.55 else (MID, BOTTOM, (t - 0.55) / 0.45)
            px[x, y] = tuple(int(a[i] + (b[i] - a[i]) * k) for i in range(3))
    return tile


def icon(size, *, corner, glyph_scale, transparent_corners):
    tile = gradient(size)
    draw = ImageDraw.Draw(tile)
    font = ImageFont.truetype(FONT, int(size * glyph_scale))
    box = draw.textbbox((0, 0), "M", font=font)
    w, h = box[2] - box[0], box[3] - box[1]
    draw.text(((size - w) / 2 - box[0], (size - h) / 2 - box[1]), "M", font=font, fill=INK)
    if not transparent_corners:
        return tile
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size - 1, size - 1), radius=int(size * corner), fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(tile, (0, 0), mask)
    return out


def main():
    # "any": rounded tile. "maskable": full bleed, glyph inside the 80% safe zone. Apple adds its own mask.
    icon(192, corner=0.22, glyph_scale=0.56, transparent_corners=True).save(OUT / "icon-192.png", optimize=True)
    icon(512, corner=0.22, glyph_scale=0.56, transparent_corners=True).save(OUT / "icon-512.png", optimize=True)
    icon(512, corner=0, glyph_scale=0.42, transparent_corners=False).save(OUT / "icon-maskable-512.png", optimize=True)
    icon(180, corner=0, glyph_scale=0.56, transparent_corners=False).save(OUT / "apple-touch-icon.png", optimize=True)
    icon(64, corner=0.22, glyph_scale=0.6, transparent_corners=True).save(OUT / "favicon.png", optimize=True)


if __name__ == "__main__":
    main()
