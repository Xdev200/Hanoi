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

val ClassicWoodPalette = HanoiPalette(
    background = Color(0xFF241711),
    surface = Color(0xFF352319),
    board = Color(0xFF5D3822),
    peg = Color(0xFFC48B5A),
    text = Color(0xFFFFF4DF),
    muted = Color(0xFFD9BA8A),
    accent = Color(0xFF76D1BD),
    success = Color(0xFFFFC85B),
    diskColors = listOf(Color(0xFFE86F51), Color(0xFF4F8FC0), Color(0xFF6FD0BC), Color(0xFFF5D7A1), Color(0xFFB6763E), Color(0xFF8FBF5B), Color(0xFFCE7BB0), Color(0xFFFFA85D))
)

val LightHanoiPalette = HanoiPalette(
    background = Color(0xFFF8FAF9),
    surface = Color.White,
    board = Color(0xFFE8EFEA),
    peg = Color(0xFF52615B),
    text = Color(0xFF1E2A26),
    muted = Color(0xFF687771),
    accent = Color(0xFF1D8F7E),
    success = Color(0xFFC98614),
    diskColors = listOf(Color(0xFFD95C49), Color(0xFF316FA8), Color(0xFF27A68F), Color(0xFFE5B85F), Color(0xFF8C6848), Color(0xFF6F9E43), Color(0xFFB15592), Color(0xFFDD8434))
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
