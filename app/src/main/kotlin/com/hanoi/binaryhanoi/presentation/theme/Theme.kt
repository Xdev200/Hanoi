package com.hanoi.binaryhanoi.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class HanoiThemeMode { ClassicWood, Light }

data class HanoiPalette(
    val background: Color,
    val surface: Color,
    val board: Color,
    val peg: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val success: Color,
    val diskColors: List<Color>
)

// Professional, sleek midnight obsidian dark palette for 10-14 year olds
val ClassicWoodPalette = HanoiPalette(
    background = Color(0xFF0B0F19), // Deep midnight obsidian (replaces muddy brown)
    surface = Color(0xFF131B2E),    // Sleek elevated midnight slate card surface
    board = Color(0xFF1E293B),      // Chamfered graphite/slate platform
    peg = Color(0xFF94A3B8),        // Brushed titanium metallic pegs
    text = Color(0xFFF8FAFC),       // Crisp cool white text
    muted = Color(0xFF94A3B8),      // Clean slate muted text
    accent = Color(0xFF22D3EE),     // Electric cyber cyan (engaging for young teens)
    success = Color(0xFFFBBF24),    // Sunburst golden amber
    diskColors = listOf(
        Color(0xFFFF4365), // Electric Coral Red
        Color(0xFF38BDF8), // Cyber Sky Blue
        Color(0xFF10B981), // Neon Emerald Green
        Color(0xFFFBBF24), // Solar Gold
        Color(0xFFA855F7), // Hyper Violet
        Color(0xFFFB7185), // Neon Pink
        Color(0xFF2DD4BF), // Tech Mint Teal
        Color(0xFFFB923C)  // Radiant Orange
    )
)

// Clean, professional, minimalist modern studio light palette for 10-14 year olds
val LightHanoiPalette = HanoiPalette(
    background = Color(0xFFF8FAFC), // Ultra-clean, crisp slate-50 canvas (replaces pale washed white)
    surface = Color(0xFFFFFFFF),    // Crisp pure white cards
    board = Color(0xFFE2E8F0),      // Sleek aluminum/platinum platform
    peg = Color(0xFF475569),        // Matte gunmetal slate pegs
    text = Color(0xFF0F172A),       // Rich midnight charcoal text
    muted = Color(0xFF64748B),      // Slate-500 secondary text
    accent = Color(0xFF0284C7),     // Electric ocean/sky blue
    success = Color(0xFFF59E0B),    // Bright amber gold
    diskColors = listOf(
        Color(0xFFEF4444), // Vibrant Ruby Red
        Color(0xFF0284C7), // Vibrant Ocean Blue
        Color(0xFF059669), // Emerald Green
        Color(0xFFD97706), // Warm Amber
        Color(0xFF7C3AED), // Deep Violet
        Color(0xFFDB2777), // Vivid Magenta
        Color(0xFF0D9488), // Deep Teal
        Color(0xFFEA580C)  // Vibrant Tangerine
    )
)

@Composable
fun HanoiTheme(mode: HanoiThemeMode, content: @Composable () -> Unit) {
    val scheme = if (mode == HanoiThemeMode.ClassicWood) {
        darkColorScheme(
            primary = ClassicWoodPalette.accent,
            secondary = ClassicWoodPalette.success,
            background = ClassicWoodPalette.background,
            surface = ClassicWoodPalette.surface,
            onPrimary = Color(0xFF10231F),
            onBackground = ClassicWoodPalette.text,
            onSurface = ClassicWoodPalette.text
        )
    } else {
        lightColorScheme(
            primary = LightHanoiPalette.accent,
            secondary = LightHanoiPalette.success,
            background = LightHanoiPalette.background,
            surface = LightHanoiPalette.surface,
            onPrimary = Color.White,
            onBackground = LightHanoiPalette.text,
            onSurface = LightHanoiPalette.text
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
