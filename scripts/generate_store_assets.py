#!/usr/bin/env python3
"""Generate Play Store graphic assets straight from the app's own brand source.

Rasterizes the exact <path> fillColor/strokeColor/pathData attributes from
app/src/main/res/drawable/ic_launcher_foreground.xml and the splash_background color from
app/src/main/res/values/colors.xml, at high resolution before downsampling with LANCZOS -
PIL's polygon/line primitives are aliased on their own, so this is what keeps the campfire
mark's curves smooth instead of jagged. Re-run this after the launcher icon or brand colors
change (needs Pillow and svgpathtools: pip install pillow svgpathtools).

Outputs:
  fastlane/metadata/android/en-US/images/icon.png            (512x512 hi-res icon)
  fastlane/metadata/android/en-US/images/featureGraphic.png  (1024x500 feature graphic)
"""

from __future__ import annotations

import xml.etree.ElementTree as ET
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont
from svgpathtools import parse_path

REPO_ROOT = Path(__file__).resolve().parent.parent
RES_DIR = REPO_ROOT / "app" / "src" / "main" / "res"
FOREGROUND_XML = RES_DIR / "drawable" / "ic_launcher_foreground.xml"
COLORS_XML = RES_DIR / "values" / "colors.xml"
OUT_DIR = REPO_ROOT / "fastlane" / "metadata" / "android" / "en-US" / "images"

ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
SUPERSAMPLE = 8  # render at 8x, then LANCZOS down - smooths every curve/arc
SAMPLES_PER_PATH = 300  # points sampled along each path's parametric curve


def hex_to_rgba(hex_color: str, alpha: float = 1.0) -> tuple[int, int, int, int]:
    hex_color = hex_color.lstrip("#")
    r, g, b = int(hex_color[0:2], 16), int(hex_color[2:4], 16), int(hex_color[4:6], 16)
    return (r, g, b, round(alpha * 255))


def load_background_color() -> str:
    root = ET.parse(COLORS_XML).getroot()
    for color in root.findall("color"):
        if color.get("name") == "splash_background":
            return color.text.strip()
    raise RuntimeError(f"splash_background not found in {COLORS_XML}")


def load_foreground_paths() -> list[dict]:
    root = ET.parse(FOREGROUND_XML).getroot()
    viewport_w = float(root.get(f"{ANDROID_NS}viewportWidth"))
    viewport_h = float(root.get(f"{ANDROID_NS}viewportHeight"))
    paths = []
    for path in root.findall("path"):

        def attr(name: str) -> str | None:
            return path.get(f"{ANDROID_NS}{name}")

        paths.append(
            {
                "d": attr("pathData"),
                "fillColor": attr("fillColor"),
                "fillAlpha": float(attr("fillAlpha") or 1.0),
                "strokeColor": attr("strokeColor"),
                "strokeWidth": float(attr("strokeWidth") or 0),
            }
        )
    return paths, viewport_w, viewport_h


def sample_points(d: str, scale: float) -> list[tuple[float, float]]:
    parsed = parse_path(d)
    return [
        (parsed.point(t / SAMPLES_PER_PATH).real * scale, parsed.point(t / SAMPLES_PER_PATH).imag * scale)
        for t in range(SAMPLES_PER_PATH + 1)
    ]


def draw_brand_mark(canvas: Image.Image, origin: tuple[float, float], mark_scale: float) -> None:
    """Draws the campfire mark's foreground paths onto `canvas`, viewport-unit (0,0) at `origin`."""
    paths, _, _ = load_foreground_paths()
    ox, oy = origin
    for spec in paths:
        points = [(ox + x * mark_scale, oy + y * mark_scale) for x, y in sample_points(spec["d"], 1.0)]
        if spec["fillColor"]:
            layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
            ImageDraw.Draw(layer).polygon(points, fill=hex_to_rgba(spec["fillColor"], spec["fillAlpha"]))
            canvas.alpha_composite(layer)
        elif spec["strokeColor"]:
            # Stamped with overlapping filled circles rather than ImageDraw.line(): a thick
            # polyline's per-segment joints leave gaps at sharp turns when the stroke width is
            # larger than the segment length (true here - a 7-unit-wide stroke over ~16 vertices
            # per lobe), which showed up as background bleeding through as speckles. Circles need
            # no joint logic, so dense stamping (sample spacing well under the radius) guarantees
            # full, smooth coverage - equivalent to a round-cap/round-join stroke.
            radius = spec["strokeWidth"] * mark_scale / 2
            layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
            draw = ImageDraw.Draw(layer)
            color = hex_to_rgba(spec["strokeColor"])
            for x, y in points:
                draw.ellipse([x - radius, y - radius, x + radius, y + radius], fill=color)
            canvas.alpha_composite(layer)


def generate_icon() -> None:
    background = load_background_color()
    _, viewport_w, viewport_h = load_foreground_paths()
    size = round(max(viewport_w, viewport_h) * SUPERSAMPLE)

    canvas = Image.new("RGBA", (size, size), hex_to_rgba(background))
    draw_brand_mark(canvas, origin=(0, 0), mark_scale=SUPERSAMPLE)

    final = canvas.resize((512, 512), Image.LANCZOS).convert("RGB")
    out_path = OUT_DIR / "icon.png"
    final.save(out_path)
    print(f"wrote {out_path}")


def load_font(name: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(f"/System/Library/Fonts/Supplemental/{name}.ttf", size)


def draw_deck_cards(canvas: Image.Image, center: tuple[float, float], scale: float) -> None:
    """Three fanned rounded-rect "question cards", echoing the app's swipeable deck."""
    cx, cy = center
    card_w, card_h = 260 * scale, 340 * scale
    cards = [
        {"angle": -10, "offset": (-55, 20), "fill": "#FFDDB5", "text": "What's a memory\nthat still makes\nyou smile?"},
        {"angle": 6, "offset": (50, 10), "fill": "#FFE8C9", "text": "What are you\nlooking forward\nto this year?"},
        {"angle": -2, "offset": (0, -10), "fill": "#FFFFFF", "text": "What's something\nyou're proud of\ntoday?"},
    ]
    font_body = load_font("Arial", round(30 * scale))
    for card in cards:
        layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer)
        x, y = cx + card["offset"][0] * scale, cy + card["offset"][1] * scale
        box = [x - card_w / 2, y - card_h / 2, x + card_w / 2, y + card_h / 2]
        draw.rounded_rectangle(box, radius=28 * scale, fill=card["fill"], outline="#BF6900", width=round(3 * scale))
        rotated = layer.rotate(card["angle"], center=(x, y), resample=Image.BICUBIC)
        canvas.alpha_composite(rotated)

    # Text drawn upright on the topmost (last) card only, after rotation of the backing cards.
    top = cards[-1]
    x, y = cx + top["offset"][0] * scale, cy + top["offset"][1] * scale
    text_layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(text_layer)
    draw.multiline_text(
        (x, y), top["text"], font=font_body, fill="#6F5B40", align="center", anchor="mm", spacing=10 * scale
    )
    canvas.alpha_composite(text_layer)


def generate_feature_graphic() -> None:
    width, height = 1024, 500
    background = load_background_color()
    canvas = Image.new("RGBA", (width * SUPERSAMPLE, height * SUPERSAMPLE), hex_to_rgba(background))
    s = SUPERSAMPLE

    mark_size = 120
    text_x = 64 * s
    draw_brand_mark(canvas, origin=(text_x, 60 * s), mark_scale=(mark_size / 108) * s)

    title_font = load_font("Arial Bold", 50 * s)
    tagline_font = load_font("Arial", 26 * s)
    text_layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(text_layer)
    draw.text((text_x, 300 * s), "Family Moments", font=title_font, fill="#BF6900")
    draw.text((text_x, 372 * s), "Spark deeper conversations", font=tagline_font, fill="#6F5B40")
    draw.text((text_x, 408 * s), "with the people you love", font=tagline_font, fill="#6F5B40")
    canvas.alpha_composite(text_layer)

    draw_deck_cards(canvas, center=(760 * s, height / 2 * s), scale=s)

    final = canvas.resize((width, height), Image.LANCZOS).convert("RGB")
    out_path = OUT_DIR / "featureGraphic.png"
    final.save(out_path)
    print(f"wrote {out_path}")


if __name__ == "__main__":
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    generate_icon()
    generate_feature_graphic()
