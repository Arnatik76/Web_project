#!/usr/bin/env python3
"""Generate responsive AVIF and WebP variants for cover images.

For every static/images/<name>.jpg|png this writes static/images/variants/<name>-<width>.<avif|webp>.
Pages pick the best format the browser supports through <picture>; the original stays as fallback.
Run after adding covers:  python3 scripts/optimize_covers.py   (requires Pillow with AVIF support)
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent / "src/main/resources/static/images"
OUT = ROOT / "variants"
WIDTHS = (192, 375)          # card thumbnails at 1x/2x; 375 is the largest original we ship
QUALITY = {"avif": 55, "webp": 78}


def main() -> None:
    OUT.mkdir(exist_ok=True)
    for source in sorted([*ROOT.glob("*.jpg"), *ROOT.glob("*.png")]):
        with Image.open(source) as image:
            image = image.convert("RGB")
            for width in WIDTHS:
                if width > image.width:
                    continue
                height = round(image.height * width / image.width)
                resized = image if width == image.width else image.resize((width, height), Image.LANCZOS)
                for fmt, quality in QUALITY.items():
                    target = OUT / f"{source.stem}-{width}.{fmt}"
                    resized.save(target, quality=quality, **({"method": 6} if fmt == "webp" else {"speed": 4}))
        print(f"{source.name}: done")


if __name__ == "__main__":
    main()
