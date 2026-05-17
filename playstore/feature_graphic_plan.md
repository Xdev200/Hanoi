# Architectural Plan & Design Specification: Hanoi Feature Graphic

## STEP 1 — Requirement Analysis
1. **Problem Definition**: The Google Play Store requires a "Feature Graphic" to showcase the application on store listings, recommendation carousels, and search screens. If missing or poorly designed, app conversions will drastically suffer.
2. **Scope**: Create a premium-grade visual asset representing the core essence of the "Hanoi" puzzle game.
3. **Functional Requirements**:
   - **Dimensions**: Exactly **1024 px** (width) by **500 px** (height).
   - **Format**: Lossless **PNG** (or high-quality JPEG) up to **15 MB**.
   - **Visuals**: Highlight Hanoi's three pillars: **Tower Puzzle (3D wood/neon pegs)**, **Mathematical Recursion (elegant grids)**, and **Premium Branding**.
4. **Non-Functional Requirements**:
   - **Scalability**: High contrast details that remain legible on small mobile screens as well as large tablets/desktop browsers.
   - **Aesthetics**: Glassmorphism, neon glows, and rich warm wood textures to fit a "modern wooden educational toy" theme.
5. **Technical Constraints**: Absolute pixel alignment (1024x500). Deviation will cause Play Store upload failure.

---

## STEP 2 — System Architecture Design (Visual Composition Layers)
The graphic composition is engineered in modular layers:
```
┌─────────────────────────────────────────────────────────────┐
│ LAYER 5: Brand Text & Subtitle ("Hanoi: Art of Recursion")  │
├─────────────────────────────────────────────────────────────┤
│ LAYER 4: Hero Element (3D Wooden Pegs + Floating Neon Disks)│
├─────────────────────────────────────────────────────────────┤
│ LAYER 3: Binary Counter / Recursion Tree Overlay            │
├─────────────────────────────────────────────────────────────┤
│ LAYER 2: Ambient Glowing Light Blooms                       │
├─────────────────────────────────────────────────────────────┤
│ LAYER 1: Background Base (Ultra-Premium Dark Wood Grain)    │
└─────────────────────────────────────────────────────────────┘
```
*   **Composition Strategy**: A clean **Split Screen Layout**.
    *   **Left 50%**: Crisp branding, subtitle, and recursion symbols to draw reading focus.
    *   **Right 50%**: A gorgeous close-up 3D perspective render of the puzzle in action.

---

## STEP 3 — Technology & Stack Validation
- **Engine**: Generative AI Image Synthesis engine (`generate_image`) with rich 3D photorealistic prompt variables.
- **Output Format**: **PNG**. 
  - *Justification*: PNG preserves fine binary line overlays, glowing disk edges, and text contrast without the "halo artifacts" or blocky pixelation common in highly compressed JPEG files.
- **Alternatives**: JPEG (rejected due to compression losses), SVG (rejected because Play Store doesn't support vector uploads for feature graphics).

---

## STEP 4 — Design Patterns
- **Rule of Thirds**: Major focus points (the tallest peg and the bold "Hanoi" text) are aligned along the vertical grid lines at 33% and 66% width.
- **High-Contrast Value Contrast**: Deep dark wood backdrop contrasts sharply with bright glowing neon disks, driving immediate user curiosity.
- **Gestalt Law of Continuity**: Semi-curved binary line rings around the pegs lead the user's eye naturally from the title text directly to the puzzle board.

---

## STEP 5 — Code Structure Planning & Output Target
- **Destination Path**: `playstore/feature_graphic.png`
- **Backup Vector reference**: `playstore/feature_graphic.svg`

---

## STEP 6 — Reusable Component Strategy
- **Palette Sharing**: Color palettes map directly to the app's Jetpack Compose theme values:
  - Background: Obsidian/Dark Walnut Wood (`#0F0A08`)
  - Peg Accent: Warm Golden Glow (`#E2B383`)
  - Disk 1 (Bottom): Deep Magenta (`#FF007F`)
  - Disk 2 (Middle): Vibrant Amber (`#FF9F1C`)
  - Disk 3 (Top): Cyberpunk Teal (`#00F5D4`)

---

## STEP 7 — UI/UX Design Guide
- **Typography**: Sleek, geometric sans-serif (similar to **Outfit** or **Inter**), high-weight for Title, medium-weight for Subtitle.
- **Layout Margins**: 10% safe zone margins around all edges to prevent layout cutoff in different Play Store aspect ratio crops.

---

## STEP 8 — UI Screen Design (Wireframe Blueprint)
```
┌──────────────────────────────────────────────────────────────┐
│  [ Safe Zone Margin 10% ]                                    │
│                                           (Glowing Pegs)     │
│    HANOI                                     ║  ║  ║         │
│    The Art of Recursion                     (══╦══)          │
│                                            (═══╬═══)         │
│    [Learn. Solve. Master]                 (════╬════)        │
│                                           ───────────        │
│                                           [Wood Base]        │
└──────────────────────────────────────────────────────────────┘
```

---

## STEP 9 — Metadata Specification
- **Width**: `1024 px`
- **Height**: `500 px`
- **Aspect Ratio**: `128:62.5` (exactly `2.048`)
- **DPI**: `72` minimum (rendered at `150` for crisp display)
- **Target File Size**: `< 5 MB` (Play Store limit is 15 MB)

---

## STEP 10 — Testing Strategy & Visual QA
We will run custom validation rules:
1. **Aspect Ratio Check**: Programmatic confirmation of exact width and height.
2. **Contrast Inspection**: Guarantee readable text levels against the background.
3. **Size constraint check**: Confirm `< 15MB` file size.

---

## STEP 11 — Implementation Milestones
1. **Milestone 1**: Generate the graphic via AI prompt with exact 1024x500 configurations.
2. **Milestone 2**: Perform image file analysis.
3. **Milestone 3**: Finalize output placement.
