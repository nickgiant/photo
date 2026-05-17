#!/usr/bin/env python3
"""
process_photo.py — CDN image processing pipeline for photoact.net

Usage:
    python3 scripts/process_photo.py --input <path> --cdn-root <dir> --filename <name>

Output: JSON to stdout with variant metadata.

Install:
    pip install Pillow piexif
    pip install pillow-heif   # optional — enables HEIC/HEIF input

CDN variants generated:
    og/          1200×630   Landscape  — Facebook, LinkedIn, Twitter, Threads, WhatsApp
    pinterest/   1000×1500  Portrait   — Pinterest 2:3 optimal
    medium/      ≤1200×?    Keep-AR    — Web display (main gallery)
    thumb/       400×300    Crop       — Grid thumbnails

Best practices applied:
  • Auto-orient via EXIF before any resize
  • Strip EXIF from outputs (privacy + smaller files)
  • Progressive JPEG for faster browser rendering
  • Smart centre-crop for exact-size variants
  • Never upscale beyond 2× — letterbox instead
"""

import argparse
import io
import json
import os
import sys
from pathlib import Path

# ── Dependency check ──────────────────────────────────────────────────────────

try:
    from PIL import Image, ImageOps
except ImportError:
    print(json.dumps({"error": "Pillow not installed. Run: pip install Pillow piexif"}),
          file=sys.stderr)
    sys.exit(1)

try:
    import piexif  # noqa: F401 — used implicitly via Pillow exif strip
except ImportError:
    pass  # Optional but recommended

# Optional HEIC support (iPhone photos)
HEIC_SUPPORTED = False
try:
    import pillow_heif
    pillow_heif.register_heif_opener()
    HEIC_SUPPORTED = True
except ImportError:
    pass

# ── Constants ─────────────────────────────────────────────────────────────────

VARIANTS: dict[str, tuple[int, int | None]] = {
    "og":        (1200, 630),   # landscape — exact crop
    "pinterest": (1000, 1500),  # portrait  — exact crop
    "medium":    (1200, None),  # keep aspect, max width
    "thumb":     (400,  300),   # square-ish — exact crop
}

JPEG_QUALITY: dict[str, int] = {
    "og":        85,
    "pinterest": 82,
    "medium":    82,
    "thumb":     75,
}

MAX_UPSCALE = 2.0  # never upscale more than 2× in a single dimension


# ── Image helpers ─────────────────────────────────────────────────────────────

def auto_orient(img: Image.Image) -> Image.Image:
    """Rotate/flip based on EXIF orientation tag."""
    try:
        return ImageOps.exif_transpose(img)
    except Exception:
        return img


def strip_exif(img: Image.Image) -> Image.Image:
    """Return a copy of the image with no EXIF metadata (privacy + size)."""
    clean_bytes = io.BytesIO()
    # Save to buffer without EXIF, then reload
    img_no_exif = Image.new(img.mode, img.size)
    img_no_exif.putdata(list(img.getdata()))
    return img_no_exif


def smart_crop(img: Image.Image, target_w: int, target_h: int) -> Image.Image:
    """
    Scale then centre-crop to exactly target_w × target_h.
    If the image would need to be upscaled more than MAX_UPSCALE, letterbox.
    """
    src_w, src_h = img.size
    scale = max(target_w / src_w, target_h / src_h)

    if scale > MAX_UPSCALE:
        # Letterbox: paste into a white/black canvas
        img_resized = img.resize(
            (int(src_w * MAX_UPSCALE), int(src_h * MAX_UPSCALE)), Image.LANCZOS
        )
        canvas = Image.new("RGB", (target_w, target_h), (0, 0, 0))
        offset_x = (target_w - img_resized.width)  // 2
        offset_y = (target_h - img_resized.height) // 2
        canvas.paste(img_resized, (offset_x, offset_y))
        return canvas

    new_w = max(int(src_w * scale), target_w)
    new_h = max(int(src_h * scale), target_h)
    img = img.resize((new_w, new_h), Image.LANCZOS)

    left = (new_w - target_w) // 2
    top  = (new_h - target_h) // 2
    return img.crop((left, top, left + target_w, top + target_h))


def resize_keep_ar(img: Image.Image, max_width: int) -> Image.Image:
    """Resize so width ≤ max_width, preserving aspect ratio. Never upscales."""
    w, h = img.size
    if w <= max_width:
        return img
    ratio = max_width / w
    return img.resize((max_width, int(h * ratio)), Image.LANCZOS)


def save_jpeg(img: Image.Image, dest: Path, quality: int) -> int:
    """Convert to RGB, save as progressive JPEG. Returns file size in bytes."""
    if img.mode != "RGB":
        img = img.convert("RGB")
    img.save(
        str(dest),
        format="JPEG",
        quality=quality,
        optimize=True,
        progressive=True,
        subsampling=0,   # 4:4:4 chroma — better quality for photography
    )
    return dest.stat().st_size


# ── Main processing ───────────────────────────────────────────────────────────

def process(input_path: str, cdn_root: str, filename: str) -> dict:
    src = Path(input_path)
    if not src.exists():
        return {"error": f"Input file not found: {input_path}"}

    # Create CDN directories
    cdn = Path(cdn_root)
    for v in list(VARIANTS.keys()) + ["original"]:
        (cdn / v).mkdir(parents=True, exist_ok=True)

    # Load image
    try:
        img = Image.open(str(src))
        img.load()  # force decode (needed for HEIC lazy load)
    except Exception as exc:
        return {"error": f"Cannot open image '{src.name}': {exc}"}

    # Normalise to RGB and apply EXIF orientation
    img = auto_orient(img)
    original_size = list(img.size)

    # Derive output filename (always .jpg)
    stem = Path(filename).stem
    out_name = stem + ".jpg"

    result: dict = {
        "filename":      out_name,
        "original_size": original_size,
        "heic_support":  HEIC_SUPPORTED,
        "variants":      {},
    }

    for variant, dims in VARIANTS.items():
        target_w, target_h = dims
        dest = cdn / variant / out_name

        if target_h is not None:
            processed = smart_crop(img, target_w, target_h)
        else:
            processed = resize_keep_ar(img, target_w)

        # Strip EXIF (clean copy)
        processed = strip_exif(processed)

        size_bytes = save_jpeg(processed, dest, JPEG_QUALITY[variant])

        result["variants"][variant] = {
            "path":       str(dest),
            "url_path":   f"/cdn/{variant}/{out_name}",
            "width":      processed.width,
            "height":     processed.height,
            "size_bytes": size_bytes,
            "size_kb":    round(size_bytes / 1024, 1),
        }

    return result


# ── Entry point ───────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Generate CDN image variants (OG, Pinterest, medium, thumb)"
    )
    parser.add_argument("--input",    required=True, help="Source image path")
    parser.add_argument("--cdn-root", required=True, help="CDN root directory")
    parser.add_argument("--filename", required=True, help="Output filename (stem or full name)")
    args = parser.parse_args()

    result = process(args.input, args.cdn_root, args.filename)
    print(json.dumps(result, indent=2))

    if "error" in result:
        sys.exit(1)


if __name__ == "__main__":
    main()
