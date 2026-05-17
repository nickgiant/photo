#!/usr/bin/env python3
"""
backfill_cdn.py — Generate CDN variants for all existing photos.

Scans {photos-dir}/photo-show/ and runs the same pipeline as process_photo.py
for every image that does not yet have a CDN og/ variant (or all, with --force).

Usage:
    python3 scripts/backfill_cdn.py \
        --photos-dir /home/pi/lazy-photos \
        --cdn-root   /var/www/photoact/cdn \
        [--workers   4] \
        [--force]        # reprocess even if CDN variants already exist
        [--dry-run]      # print what would be processed, do nothing

Install deps first:
    pip install Pillow piexif
    pip install pillow-heif   # optional HEIC support
"""

import argparse
import json
import os
import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

# Reuse the processing logic from process_photo.py in the same directory
SCRIPT_DIR = Path(__file__).parent
sys.path.insert(0, str(SCRIPT_DIR))

try:
    from process_photo import process
except ImportError as e:
    print(f"ERROR: Cannot import process_photo: {e}", file=sys.stderr)
    sys.exit(1)

SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp", ".heic", ".heif", ".tiff", ".tif"}


def should_skip(filename: str, cdn_root: Path) -> bool:
    """Return True if the og/ variant already exists for this filename."""
    stem = Path(filename).stem
    return (cdn_root / "og" / (stem + ".jpg")).exists()


def process_one(src: Path, cdn_root: Path, force: bool) -> dict:
    filename = src.name

    if not force and should_skip(filename, cdn_root):
        return {"file": filename, "status": "skipped"}

    try:
        result = process(str(src), str(cdn_root), filename)
        if "error" in result:
            return {"file": filename, "status": "error", "reason": result["error"]}
        variants = list(result.get("variants", {}).keys())
        return {"file": filename, "status": "ok", "variants": variants}
    except Exception as exc:
        return {"file": filename, "status": "error", "reason": str(exc)}


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Backfill CDN variants for all existing photos"
    )
    parser.add_argument("--photos-dir", required=True,
                        help="Base photos directory (e.g. /home/pi/lazy-photos)")
    parser.add_argument("--cdn-root", required=True,
                        help="CDN root directory (e.g. /var/www/photoact/cdn)")
    parser.add_argument("--workers", type=int, default=4,
                        help="Parallel worker threads (default: 4)")
    parser.add_argument("--force", action="store_true",
                        help="Reprocess even if CDN variants already exist")
    parser.add_argument("--dry-run", action="store_true",
                        help="List files to be processed without actually running")
    args = parser.parse_args()

    photos_dir = Path(args.photos_dir)
    show_dir   = photos_dir / "photo-show"
    cdn_root   = Path(args.cdn_root)

    if not show_dir.exists():
        print(f"ERROR: photo-show directory not found: {show_dir}", file=sys.stderr)
        sys.exit(1)

    # Collect all image files
    files = sorted(
        f for f in show_dir.iterdir()
        if f.is_file() and f.suffix.lower() in SUPPORTED_EXTENSIONS
    )

    if not files:
        print(f"No image files found in {show_dir}")
        sys.exit(0)

    # Separate pending vs already-done
    pending = [f for f in files if args.force or not should_skip(f.name, cdn_root)]
    already  = len(files) - len(pending)

    print(f"Found {len(files)} photos  |  {already} already have CDN variants  |  {len(pending)} to process")

    if args.dry_run:
        for f in pending:
            print(f"  would process: {f.name}")
        print("Dry run — nothing written.")
        return

    if not pending:
        print("Nothing to do.")
        return

    # Process in parallel
    t0 = time.time()
    counts = {"ok": 0, "skipped": 0, "error": 0}
    errors = []

    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        futures = {pool.submit(process_one, f, cdn_root, args.force): f for f in pending}
        done = 0
        for future in as_completed(futures):
            result = future.result()
            done += 1
            status = result["status"]
            counts[status] = counts.get(status, 0) + 1

            if status == "error":
                errors.append(result)
                print(f"  [{done}/{len(pending)}] ERROR  {result['file']}: {result.get('reason', '')}")
            elif status == "skipped":
                print(f"  [{done}/{len(pending)}] SKIP   {result['file']}")
            else:
                print(f"  [{done}/{len(pending)}] OK     {result['file']}  {result.get('variants', [])}")

    elapsed = time.time() - t0
    print(f"\nDone in {elapsed:.1f}s  — ok:{counts['ok']}  skipped:{counts['skipped']}  errors:{counts['error']}")

    if errors:
        print("\nErrors:")
        for e in errors:
            print(f"  {e['file']}: {e.get('reason', '')}")
        sys.exit(1)


if __name__ == "__main__":
    main()
