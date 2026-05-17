#!/usr/bin/env python3
"""
generate_screenshots.py
=======================
Automated Python pipeline to convert tall mobile screenshots (1080x2400, 9:20 ratio)
into Google Play Store-compliant 7-inch and 10-inch tablet screenshots (1350x2400, 9:16 ratio).

Implements a Staff-level visual composition:
1. Warm Walnut Base (#0C0705)
2. Amber Neon Ambient Glow (Left Padding Edge)
3. Teal Neon Ambient Glow (Right Padding Edge)
4. High-Fidelity Soft Ambient Occlusion Drop Shadow
5. Elegant 2px Bronze Framing Border (#4D3319)
6. Dynamic Programmatic Verification Suite
"""

import os
import sys
from PIL import Image, ImageDraw, ImageFilter, ImageOps

# Configuration Constants
SRC_DIR = "playstore"
DEST_7_DIR = os.path.join(SRC_DIR, "tablet_7")
DEST_10_DIR = os.path.join(SRC_DIR, "tablet_10")

# Dimension Specs
SRC_WIDTH = 1080
SRC_HEIGHT = 2400
TARGET_WIDTH = 1350
TARGET_HEIGHT = 2400 # 9:16 aspect ratio (1350 x 2400 px)
PADDING_X = (TARGET_WIDTH - SRC_WIDTH) // 2 # 135 pixels on each side

# Aesthetic Specs
COLOR_WALNUT_BASE = (12, 7, 5, 255)       # #0C0705
COLOR_AMBER_GLOW = (230, 81, 0, 90)       # #E65100 with ~35% opacity
COLOR_TEAL_GLOW = (0, 172, 193, 90)       # #00ACC1 with ~35% opacity
COLOR_BRONZE_BORDER = (77, 51, 25)        # #4D3319
BORDER_WIDTH = 2
SHADOW_OPACITY = 160                      # ~63% black shadow
SHADOW_BLUR_RADIUS = 30

def create_ambient_background():
    """Generates a premium 1350x2400 background canvas with walnut base and neon glows."""
    # 1. Base Canvas
    bg = Image.new("RGBA", (TARGET_WIDTH, TARGET_HEIGHT), COLOR_WALNUT_BASE)
    
    # 2. Glow Layer for soft light leaks
    glow_layer = Image.new("RGBA", (TARGET_WIDTH, TARGET_HEIGHT), (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow_layer)
    
    # Left amber neon glow ellipse (highly stretched vertically, positioned on left edge)
    # Bounds: x1=-200, y1=100, x2=250, y2=2300
    draw.ellipse([-200, 100, 250, 2300], fill=COLOR_AMBER_GLOW)
    
    # Right teal neon glow ellipse (highly stretched vertically, positioned on right edge)
    # Bounds: x1=1100, y1=100, x2=1550, y2=2300
    draw.ellipse([1100, 100, 1550, 2300], fill=COLOR_TEAL_GLOW)
    
    # Apply powerful Gaussian blur to blend the light leaks smoothly
    glow_blurred = glow_layer.filter(ImageFilter.GaussianBlur(radius=150))
    
    # Composite glow onto walnut base
    composite = Image.alpha_composite(bg, glow_blurred)
    return composite

def add_decorations(screenshot):
    """Applies a high-fidelity drop-shadow and crisp bronze border around the screenshot."""
    # Ensure source image is RGBA
    if screenshot.mode != "RGBA":
        screenshot = screenshot.convert("RGBA")
        
    # 1. Generate shadow canvas (1350x2400)
    shadow_canvas = Image.new("RGBA", (TARGET_WIDTH, TARGET_HEIGHT), (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow_canvas)
    
    # Draw solid black rectangle where the screenshot will sit, with shadow opacity
    shadow_draw.rectangle(
        [PADDING_X, 0, PADDING_X + SRC_WIDTH, TARGET_HEIGHT],
        fill=(0, 0, 0, SHADOW_OPACITY)
    )
    
    # Blur the shadow canvas to create a luxurious ambient drop shadow
    shadow_blurred = shadow_canvas.filter(ImageFilter.GaussianBlur(radius=SHADOW_BLUR_RADIUS))
    
    # 2. Apply framing border to screenshot
    decorated_screenshot = screenshot.copy()
    draw_border = ImageDraw.Draw(decorated_screenshot)
    
    # Draw thin bronze border around the outer edge of the screenshot
    # Drawing coordinate box: (0, 0, SRC_WIDTH-1, SRC_HEIGHT-1)
    for i in range(BORDER_WIDTH):
        draw_border.rectangle(
            [i, i, SRC_WIDTH - 1 - i, SRC_HEIGHT - 1 - i],
            outline=COLOR_BRONZE_BORDER
        )
        
    return shadow_blurred, decorated_screenshot

def process_screenshot(src_path):
    """Composites background, shadow, and decorated screenshot into target 1350x2400 image."""
    print(f"Processing source file: {os.path.basename(src_path)}...")
    
    # Load screenshot
    with Image.open(src_path) as screenshot:
        # Generate components
        bg = create_ambient_background()
        shadow, decorated_scr = add_decorations(screenshot)
        
        # Composite Shadow onto Background
        bg_with_shadow = Image.alpha_composite(bg, shadow)
        
        # Paste Screenshot on top (centered at PADDING_X, 0)
        final_image = bg_with_shadow.copy()
        final_image.paste(decorated_scr, (PADDING_X, 0), decorated_scr)
        
        return final_image

def validate_output(file_path):
    """Runs a strict diagnostic suite on the output file to guarantee Play Store compatibility."""
    diagnostics = {"passed": True, "errors": []}
    
    # 1. Verify existence
    if not os.path.exists(file_path):
        diagnostics["passed"] = False
        diagnostics["errors"].append("File does not exist.")
        return diagnostics
        
    # 2. Check size limits (under 8 MB)
    size_bytes = os.path.getsize(file_path)
    size_mb = size_bytes / (1024 * 1024)
    if size_bytes > 8 * 1024 * 1024:
        diagnostics["passed"] = False
        diagnostics["errors"].append(f"File size exceeds 8 MB limit: {size_mb:.2f} MB")
        
    # 3. Open image to test header and dimensions
    try:
        with Image.open(file_path) as img:
            w, h = img.size
            # Dimension checks
            if w != TARGET_WIDTH or h != TARGET_HEIGHT:
                diagnostics["passed"] = False
                diagnostics["errors"].append(f"Invalid dimensions: {w}x{h} px (expected {TARGET_WIDTH}x{TARGET_HEIGHT})")
                
            # Aspect ratio check (expected 9:16)
            ratio = w / h
            expected_ratio = 9.0 / 16.0
            if abs(ratio - expected_ratio) > 1e-5:
                diagnostics["passed"] = False
                diagnostics["errors"].append(f"Invalid aspect ratio: {w}:{h} (expected 9:16)")
                
            # Format checks
            if img.format != "PNG":
                diagnostics["passed"] = False
                diagnostics["errors"].append(f"Invalid format: {img.format} (expected PNG)")
                
            # Image mode check (should be RGB or RGBA)
            if img.mode not in ["RGB", "RGBA"]:
                diagnostics["passed"] = False
                diagnostics["errors"].append(f"Invalid color mode: {img.mode} (expected RGB or RGBA)")
                
    except Exception as e:
        diagnostics["passed"] = False
        diagnostics["errors"].append(f"Failed to open or decode image: {str(e)}")
        
    return diagnostics, size_bytes

def main():
    print("=" * 60)
    print("TOWER OF HANOI TABLET SCREENSHOTS COMPOSITING PIPELINE")
    print("=" * 60)
    
    # Create target directories
    os.makedirs(DEST_7_DIR, exist_ok=True)
    os.makedirs(DEST_10_DIR, exist_ok=True)
    
    # List screenshots in playstore/
    src_files = [
        f for f in os.listdir(SRC_DIR)
        if f.startswith("Screenshot_") and f.endswith(".png")
    ]
    
    if not src_files:
        print("[-] Error: No source mobile screenshots (Screenshot_*.png) found in playstore/.")
        sys.exit(1)
        
    print(f"Found {len(src_files)} screenshots to process: {sorted(src_files)}")
    
    success_count = 0
    results_summary = []
    
    for filename in sorted(src_files):
        src_path = os.path.join(SRC_DIR, filename)
        
        try:
            # Run composition
            final_img = process_screenshot(src_path)
            
            # Save paths
            out_7_path = os.path.join(DEST_7_DIR, filename)
            out_10_path = os.path.join(DEST_10_DIR, filename)
            
            # Save optimized PNGs (Optimize parameters compress lossless file sizes efficiently)
            final_img.save(out_7_path, "PNG", optimize=True)
            final_img.save(out_10_path, "PNG", optimize=True)
            
            # Validate output
            v_7, size_7 = validate_output(out_7_path)
            v_10, size_10 = validate_output(out_10_path)
            
            status_7 = "PASSED" if v_7["passed"] else f"FAILED: {v_7['errors']}"
            status_10 = "PASSED" if v_10["passed"] else f"FAILED: {v_10['errors']}"
            
            print(f"[+] {filename} -> tablet_7/  ({size_7/(1024):.1f} KB): {status_7}")
            print(f"[+] {filename} -> tablet_10/ ({size_10/(1024):.1f} KB): {status_10}")
            
            if v_7["passed"] and v_10["passed"]:
                success_count += 1
                
            results_summary.append({
                "filename": filename,
                "size_7": size_7,
                "size_10": size_10,
                "v_7": v_7,
                "v_10": v_10
            })
            
        except Exception as e:
            print(f"[-] Failed to process {filename}: {str(e)}")
            
    print("\n" + "=" * 60)
    print("PIPELINE SUMMARY STATUS")
    print("=" * 60)
    print(f"Successfully processed and validated: {success_count}/{len(src_files)} assets.")
    
    if success_count == len(src_files):
        print("[SUCCESS] All tablet screenshots generated are 100% compliant with Play Store rules!")
        sys.exit(0)
    else:
        print("[WARNING] One or more assets failed verification. Please review diagnostic log.")
        sys.exit(1)

if __name__ == "__main__":
    main()
