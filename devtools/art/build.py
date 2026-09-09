"""The art of Dynamite, as code: the bundle's icon, and the three sounds cut
from CC0 recordings listed in sounds/SOURCES.md.

    uv run --no-project python devtools/art/build.py            # everything
    uv run --no-project python devtools/art/build.py textures   # the icon only
    uv run --no-project python devtools/art/build.py sounds     # the sounds only

Everything it writes lands under src/main/resources/assets/dynamite/ and is
committed; this script is the source of truth for those files. The sounds
need ffmpeg and the recordings under devtools/art/sounds/src/.
"""

from __future__ import annotations

import struct
import subprocess
import sys
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/dynamite"
SOUND_SRC = ROOT / "devtools/art/sounds/src"


def write_png(path: Path, width: int, height: int, pixels) -> None:
    """pixels: rows of (r, g, b, a) tuples, top row first."""
    raw = b"".join(b"\x00" + b"".join(bytes(p) for p in row) for row in pixels)

    def chunk(kind: bytes, data: bytes) -> bytes:
        return (struct.pack(">I", len(data)) + kind + data
                + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF))

    png = (b"\x89PNG\r\n\x1a\n"
           + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
           + chunk(b"IDAT", zlib.compress(raw, 9))
           + chunk(b"IEND", b""))
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


# The bundle: three sticks of waxed red paper standing together, two turns of
# twine round them, one fuse rising from the middle stick. Drawn upright, the
# way a thrown item is held, in the game's own pixel grammar: a dark outline,
# a mid tone, a highlight down one side, a shadow down the other.
CLEAR = (0, 0, 0, 0)
OUTLINE = (58, 18, 14, 255)
RED = (178, 40, 32, 255)
RED_LIGHT = (214, 72, 58, 255)
RED_DARK = (128, 26, 22, 255)
CAP = (90, 26, 20, 255)
TWINE = (196, 160, 96, 255)
TWINE_DARK = (140, 108, 58, 255)
FUSE = (72, 62, 52, 255)
FUSE_LIGHT = (120, 106, 88, 255)
SPARK = (255, 214, 96, 255)
SPARK_HOT = (255, 250, 210, 255)

ICON = [
    "................",
    ".........s......",
    "........sS......",
    "........f.......",
    "........f.......",
    "..OO..OOfO..OO..",
    ".OCCOOCCCCOOCCO.",
    ".OLRDOLRRDOLRDO.",
    ".OLRDOLRRDOLRDO.",
    ".OtttttttttttTO.",
    ".OLRDOLRRDOLRDO.",
    ".OLRDOLRRDOLRDO.",
    ".OtttttttttttTO.",
    ".OLRDOLRRDOLRDO.",
    ".OCCOOCCCCOOCCO.",
    "..OO..OOOO..OO..",
]
KEY = {
    ".": CLEAR, "O": OUTLINE, "R": RED, "L": RED_LIGHT, "D": RED_DARK, "C": CAP,
    "t": TWINE, "T": TWINE_DARK, "f": FUSE, "F": FUSE_LIGHT, "s": SPARK, "S": SPARK_HOT,
}


def icon():
    return [[KEY[c] for c in row] for row in ICON]


# The sounds: each a cut of a CC0 recording, trimmed, faded and normalised
# with ffmpeg. (source file, start seconds, length seconds, gain dB, fade-out seconds)
SOUNDS = {
    "throw": ("throw.ogg", 0.0, 0.45, 0.0, 0.12),
    "fuse": ("fuse.ogg", 2.0, 0.7, 0.0, 0.1),
    "land": ("land.ogg", 0.0, 0.4, 0.0, 0.1),
}


def sound(name: str, spec) -> None:
    src, start, length, gain, fade = spec
    path = SOUND_SRC / src
    if not path.exists():
        raise SystemExit(f"missing recording {path}: see devtools/art/sounds/SOURCES.md")
    out = ASSETS / "sounds" / f"{name}.ogg"
    out.parent.mkdir(parents=True, exist_ok=True)
    filters = f"atrim=start={start}:duration={length},asetpts=PTS-STARTPTS,afade=t=out:st={max(0.0, length - fade)}:d={fade},volume={gain}dB,loudnorm=I=-16:TP=-1.5"
    subprocess.run([
        "ffmpeg", "-y", "-loglevel", "error", "-i", str(path), "-af", filters,
        "-ac", "1", "-ar", "44100", "-c:a", "libvorbis", "-q:a", "5", str(out),
    ], check=True)


def main(argv) -> int:
    what = set(argv[1:]) or {"textures", "sounds"}
    if "textures" in what:
        write_png(ASSETS / "textures/item/dynamite.png", 16, 16, icon())
        print("textures: item/dynamite.png (16x16)")
    if "sounds" in what:
        for name, spec in SOUNDS.items():
            sound(name, spec)
        print("sounds:", ", ".join(f"{n}.ogg" for n in SOUNDS))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
