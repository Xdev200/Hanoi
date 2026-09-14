package com.hanoi.binaryhanoi.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hanoi.binaryhanoi.domain.model.GameMode
import com.hanoi.binaryhanoi.domain.model.Level

enum class SceneType { Classic, Strategic, DualTower, Tutorial }
enum class StickerType { Think, Freedom, Logic, Challenge }

// ─────────────────────────────────────────────────────────────────────────────
// 1. DYNAMIC 3D BOARD MINIATURE THUMBNAIL (For Level Cards)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LevelMiniBoard3D(
    level: Level,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val pegCount = level.pegCount
        val laneW = w / pegCount
        val baseH = h * 0.16f
        val baseY = h - baseH - 2f
        val pegH = h * 0.58f
        val pegTop = baseY - pegH
        val pegW = (w * 0.045f).coerceIn(4f, 7f)

        // 3D Base Platform (isometric perspective)
        val baseFrontColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        val baseTopColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
        val baseShadowColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.15f)

        // Drop shadow
        drawRoundRect(
            color = baseShadowColor,
            topLeft = Offset(2f, baseY + baseH * 0.4f),
            size = Size(w - 4f, baseH * 0.8f),
            cornerRadius = CornerRadius(baseH * 0.4f, baseH * 0.4f)
        )

        // Base front face
        drawRoundRect(
            color = baseFrontColor,
            topLeft = Offset(4f, baseY + 3f),
            size = Size(w - 8f, baseH),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Base top bevel
        drawRoundRect(
            color = baseTopColor,
            topLeft = Offset(4f, baseY),
            size = Size(w - 8f, baseH * 0.45f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Stand Pegs
        val pegColor1 = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
        val pegColor2 = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
        val pegBrush = Brush.horizontalGradient(
            colors = listOf(pegColor1, pegColor2, pegColor1)
        )

        for (i in 0 until pegCount) {
            val cx = laneW * i + laneW / 2f
            // Peg cylinder
            drawRoundRect(
                brush = pegBrush,
                topLeft = Offset(cx - pegW / 2f, pegTop),
                size = Size(pegW, pegH),
                cornerRadius = CornerRadius(pegW / 2f, pegW / 2f)
            )
            // Peg cap highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = pegW * 0.65f,
                center = Offset(cx, pegTop)
            )
        }

        // Disks Rendering
        val diskPalette = listOf(
            Color(0xFF38BDF8), // Sky
            Color(0xFF2DD4BF), // Teal
            Color(0xFF34D399), // Emerald
            Color(0xFFFBBF24), // Amber
            Color(0xFFFB923C), // Orange
            Color(0xFFF87171), // Rose
            Color(0xFFA78BFA), // Violet
            Color(0xFF818CF8)  // Indigo
        )

        val redPalette = listOf(
            Color(0xFFF87171),
            Color(0xFFEF4444),
            Color(0xFFDC2626),
            Color(0xFFB91C1C)
        )

        val bluePalette = listOf(
            Color(0xFF38BDF8),
            Color(0xFF0EA5E9),
            Color(0xFF0284C7),
            Color(0xFF0369A1)
        )

        val diskThickness = ((baseY - pegTop) * 0.85f / (level.diskCount.coerceAtLeast(3) + 1)).coerceIn(5f, 9f)

        if (level.mode == GameMode.Bicolor) {
            // Dual Tower: Stand 1 has Red disks, Stand 3 has Blue disks!
            val stand1Cx = laneW * 0 + laneW / 2f
            val stand3Cx = laneW * (pegCount - 1) + laneW / 2f

            for (d in 0 until level.diskCount) {
                val diskIndex = level.diskCount - 1 - d
                val normalizedSize = (diskIndex + 1).toFloat() / level.diskCount
                val diskWidth = (pegW * 2.8f) + (laneW * 0.80f - pegW * 2.8f) * normalizedSize
                val diskY = baseY - (d + 1) * diskThickness

                // Stand 1: Red disk
                val redColor = redPalette[diskIndex % redPalette.size]
                draw3DMiniDisk(stand1Cx, diskY, diskWidth, diskThickness, redColor)

                // Stand 3 / target: Blue disk
                val blueColor = bluePalette[diskIndex % bluePalette.size]
                draw3DMiniDisk(stand3Cx, diskY, diskWidth, diskThickness, blueColor)
            }
        } else {
            // Classic or Strategic: Disks stacked on Stand 1
            val stand1Cx = laneW * 0 + laneW / 2f
            for (d in 0 until level.diskCount) {
                val diskIndex = level.diskCount - 1 - d
                val normalizedSize = (diskIndex + 1).toFloat() / level.diskCount
                val diskWidth = (pegW * 2.6f) + (laneW * 0.84f - pegW * 2.6f) * normalizedSize
                val diskY = baseY - (d + 1) * diskThickness
                val color = diskPalette[diskIndex % diskPalette.size]

                draw3DMiniDisk(stand1Cx, diskY, diskWidth, diskThickness, color)
            }
        }
    }
}

private fun DrawScope.draw3DMiniDisk(cx: Float, y: Float, width: Float, height: Float, color: Color) {
    val corner = height * 0.45f

    // Drop shadow under disk
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(cx - width / 2f, y + 1.5f),
        size = Size(width, height),
        cornerRadius = CornerRadius(corner, corner)
    )

    // Main 3D disk body
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - width / 2f, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(corner, corner)
    )

    // Specular top highlight line
    drawRoundRect(
        color = Color.White.copy(alpha = 0.42f),
        topLeft = Offset(cx - width * 0.4f, y + 1f),
        size = Size(width * 0.8f, height * 0.35f),
        cornerRadius = CornerRadius(corner * 0.5f, corner * 0.5f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. 3D ROBOT AVATAR (For Classic Mode)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AvatarRobot3D(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.minDimension
        val c = Offset(s / 2f, s / 2f)

        // Background Circle with glowing cyan/teal gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)),
                center = c,
                radius = s * 0.5f
            ),
            radius = s * 0.48f,
            center = c
        )

        // Outer rim
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = s * 0.48f,
            center = c,
            style = Stroke(width = s * 0.04f)
        )

        // Robot Head
        val headW = s * 0.54f
        val headH = s * 0.46f
        val headX = (s - headW) / 2f
        val headY = s * 0.28f

        // Head shadow
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.3f),
            topLeft = Offset(headX, headY + s * 0.04f),
            size = Size(headW, headH),
            cornerRadius = CornerRadius(s * 0.14f, s * 0.14f)
        )

        // Head chassis
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
                startY = headY,
                endY = headY + headH
            ),
            topLeft = Offset(headX, headY),
            size = Size(headW, headH),
            cornerRadius = CornerRadius(s * 0.14f, s * 0.14f)
        )

        // Top Antenna
        drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(s / 2f, headY),
            end = Offset(s / 2f, headY - s * 0.12f),
            strokeWidth = s * 0.04f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFF2DD4BF),
            radius = s * 0.05f,
            center = Offset(s / 2f, headY - s * 0.12f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = s * 0.02f,
            center = Offset(s / 2f - s * 0.015f, headY - s * 0.135f)
        )

        // Visor (dark glossy curved rect)
        val visorW = headW * 0.76f
        val visorH = headH * 0.44f
        val visorX = (s - visorW) / 2f
        val visorY = headY + s * 0.07f

        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(visorX, visorY),
            size = Size(visorW, visorH),
            cornerRadius = CornerRadius(s * 0.08f, s * 0.08f)
        )

        // Glowing Blue Eyes
        val eyeW = visorW * 0.26f
        val eyeH = visorH * 0.42f
        val eyeY = visorY + (visorH - eyeH) / 2f

        // Left Eye
        drawRoundRect(
            color = Color(0xFF2DD4BF),
            topLeft = Offset(visorX + visorW * 0.16f, eyeY),
            size = Size(eyeW, eyeH),
            cornerRadius = CornerRadius(s * 0.03f, s * 0.03f)
        )
        // Right Eye
        drawRoundRect(
            color = Color(0xFF2DD4BF),
            topLeft = Offset(visorX + visorW * 0.58f, eyeY),
            size = Size(eyeW, eyeH),
            cornerRadius = CornerRadius(s * 0.03f, s * 0.03f)
        )

        // Mouth Grill dots
        val mouthY = headY + headH * 0.78f
        for (m in -2..2) {
            drawCircle(
                color = Color(0xFF64748B),
                radius = s * 0.018f,
                center = Offset(s / 2f + m * s * 0.045f, mouthY)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. 3D USER AVATAR (For 4 Stands & Dual Tower)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AvatarUser3D(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.minDimension
        val c = Offset(s / 2f, s / 2f)

        // Background Circle with indigo-pink gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                center = c,
                radius = s * 0.5f
            ),
            radius = s * 0.48f,
            center = c
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = s * 0.48f,
            center = c,
            style = Stroke(width = s * 0.04f)
        )

        // Headphone Headband
        drawCircle(
            color = Color(0xFF1E293B),
            radius = s * 0.32f,
            center = Offset(s / 2f, s * 0.44f),
            style = Stroke(width = s * 0.05f)
        )

        // Face
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFED7AA), Color(0xFFFDBA74)),
                startY = s * 0.24f,
                endY = s * 0.72f
            ),
            radius = s * 0.24f,
            center = Offset(s / 2f, s * 0.46f)
        )

        // Hair (trendy bangs)
        val hairPath = Path().apply {
            moveTo(s * 0.26f, s * 0.42f)
            cubicTo(s * 0.26f, s * 0.22f, s * 0.74f, s * 0.22f, s * 0.74f, s * 0.42f)
            lineTo(s * 0.65f, s * 0.38f)
            lineTo(s * 0.50f, s * 0.42f)
            lineTo(s * 0.35f, s * 0.36f)
            close()
        }
        drawPath(hairPath, Color(0xFF334155))

        // Eyes
        drawCircle(color = Color(0xFF1E293B), radius = s * 0.035f, center = Offset(s * 0.42f, s * 0.46f))
        drawCircle(color = Color(0xFF1E293B), radius = s * 0.035f, center = Offset(s * 0.58f, s * 0.46f))
        drawCircle(color = Color.White, radius = s * 0.012f, center = Offset(s * 0.41f, s * 0.45f))
        drawCircle(color = Color.White, radius = s * 0.012f, center = Offset(s * 0.57f, s * 0.45f))

        // Smile
        drawArc(
            color = Color(0xFFE11D48),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(s * 0.44f, s * 0.52f),
            size = Size(s * 0.12f, s * 0.08f),
            style = Stroke(width = s * 0.025f, cap = StrokeCap.Round)
        )

        // Headphone Earcups (Glowing cyan & magenta)
        drawRoundRect(
            color = Color(0xFF2DD4BF),
            topLeft = Offset(s * 0.16f, s * 0.36f),
            size = Size(s * 0.10f, s * 0.20f),
            cornerRadius = CornerRadius(s * 0.04f, s * 0.04f)
        )
        drawRoundRect(
            color = Color(0xFF2DD4BF),
            topLeft = Offset(s * 0.74f, s * 0.36f),
            size = Size(s * 0.10f, s * 0.20f),
            cornerRadius = CornerRadius(s * 0.04f, s * 0.04f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. 3D STICKER BADGES (Think, Freedom, Logic, Challenge)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StickerBadge3D(
    type: StickerType,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (type) {
            StickerType.Think -> {
                // Bulb with gears / rays
                val cx = w * 0.5f
                val cy = h * 0.46f
                val r = w * 0.28f

                // Outer sticker white outline
                drawCircle(color = if (isDark) Color(0xFF1E293B) else Color.White, radius = r * 1.3f, center = Offset(cx, cy))
                drawCircle(
                    color = if (isDark) Color(0x44CBD5E1) else Color(0x33000000),
                    radius = r * 1.3f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )

                // Glowing yellow bulb body
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFDE047), Color(0xFFEAB308)),
                        center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                        radius = r
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )

                // Bulb base
                drawRoundRect(
                    color = Color(0xFF94A3B8),
                    topLeft = Offset(cx - r * 0.35f, cy + r * 0.8f),
                    size = Size(r * 0.7f, r * 0.4f),
                    cornerRadius = CornerRadius(3f, 3f)
                )

                // Sparkle rays
                for (angle in listOf(-60f, -30f, 0f, 30f, 60f)) {
                    val rad = Math.toRadians((angle - 90).toDouble())
                    val sx = cx + (r * 1.35f * Math.cos(rad)).toFloat()
                    val sy = cy + (r * 1.35f * Math.sin(rad)).toFloat()
                    val ex = cx + (r * 1.65f * Math.cos(rad)).toFloat()
                    val ey = cy + (r * 1.65f * Math.sin(rad)).toFloat()
                    drawLine(color = Color(0xFFF59E0B), start = Offset(sx, sy), end = Offset(ex, ey), strokeWidth = 3f, cap = StrokeCap.Round)
                }
            }

            StickerType.Freedom -> {
                // Paper plane with rainbow stream
                val cx = w * 0.5f
                val cy = h * 0.5f

                // Badge border
                drawRoundRect(
                    color = if (isDark) Color(0xFF1E293B) else Color.White,
                    topLeft = Offset(4f, 4f),
                    size = Size(w - 8f, h - 8f),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Jet stream trail
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF2DD4BF), Color(0xFF3B82F6))),
                    start = Offset(w * 0.18f, h * 0.72f),
                    end = Offset(w * 0.52f, h * 0.48f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )

                // Paper plane polygon
                val plane = Path().apply {
                    moveTo(w * 0.84f, h * 0.24f)
                    lineTo(w * 0.32f, h * 0.52f)
                    lineTo(w * 0.52f, h * 0.62f)
                    close()
                }
                drawPath(plane, Color(0xFF0EA5E9))

                val wing = Path().apply {
                    moveTo(w * 0.84f, h * 0.24f)
                    lineTo(w * 0.52f, h * 0.62f)
                    lineTo(w * 0.64f, h * 0.46f)
                    close()
                }
                drawPath(wing, Color(0xFF38BDF8))
            }

            StickerType.Logic -> {
                // Interlocking neon gears
                val cx = w * 0.48f
                val cy = h * 0.50f
                val r = w * 0.28f

                drawRoundRect(
                    color = if (isDark) Color(0xFF0F172A) else Color.White,
                    topLeft = Offset(4f, 4f),
                    size = Size(w - 8f, h - 8f),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Gear 1 (cyan)
                drawCircle(color = Color(0xFF2DD4BF), radius = r, center = Offset(cx - 6f, cy - 4f), style = Stroke(width = 6f))
                drawCircle(color = Color(0xFF2DD4BF), radius = r * 0.35f, center = Offset(cx - 6f, cy - 4f))

                // Gear 2 (amber)
                drawCircle(color = Color(0xFFF59E0B), radius = r * 0.75f, center = Offset(cx + r * 0.75f, cy + r * 0.6f), style = Stroke(width = 5f))
            }

            StickerType.Challenge -> {
                // Golden star trophy with ribbon
                val cx = w * 0.5f
                val cy = h * 0.46f
                val r = w * 0.30f

                drawCircle(
                    color = if (isDark) Color(0xFF1E293B) else Color.White,
                    radius = r * 1.25f,
                    center = Offset(cx, cy)
                )

                // 5-Point Star
                val starPath = Path().apply {
                    val points = 5
                    val outerR = r
                    val innerR = r * 0.45f
                    for (i in 0 until points * 2) {
                        val currR = if (i % 2 == 0) outerR else innerR
                        val angle = Math.toRadians((i * 36 - 90).toDouble())
                        val x = cx + (currR * Math.cos(angle)).toFloat()
                        val y = cy + (currR * Math.sin(angle)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }

                drawPath(
                    starPath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B)),
                        center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                        radius = r
                    )
                )

                // Ribbons under star
                val ribbonLeft = Path().apply {
                    moveTo(cx - r * 0.2f, cy + r * 0.6f)
                    lineTo(cx - r * 0.6f, cy + r * 1.3f)
                    lineTo(cx - r * 0.3f, cy + r * 1.1f)
                    lineTo(cx, cy + r * 0.8f)
                    close()
                }
                drawPath(ribbonLeft, Color(0xFFEF4444))

                val ribbonRight = Path().apply {
                    moveTo(cx + r * 0.2f, cy + r * 0.6f)
                    lineTo(cx + r * 0.6f, cy + r * 1.3f)
                    lineTo(cx + r * 0.3f, cy + r * 1.1f)
                    lineTo(cx, cy + r * 0.8f)
                    close()
                }
                drawPath(ribbonRight, Color(0xFFDC2626))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. 3D DESK HERO SCENE (Dynamic vector 3D illustrated desk banner)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DeskHeroScene3D(
    sceneType: SceneType,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Background desk room atmosphere
        val bgColors = when {
            isDark -> listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
            sceneType == SceneType.DualTower -> listOf(Color(0xFFF0FDF4), Color(0xFFE0F2FE), Color(0xFFFCE7F3))
            else -> listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
        }

        drawRect(brush = Brush.verticalGradient(bgColors))

        // Ambient desk surface (isometric perspective plane)
        val deskTopY = h * 0.42f
        val deskColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        val deskEdgeColor = if (isDark) Color(0xFF0F172A) else Color(0xFFCBD5E1)

        val deskPath = Path().apply {
            moveTo(0f, deskTopY)
            lineTo(w, deskTopY - h * 0.05f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(deskPath, deskColor)

        // Desk front bevel line
        drawLine(
            color = deskEdgeColor,
            start = Offset(0f, deskTopY),
            end = Offset(w, deskTopY - h * 0.05f),
            strokeWidth = 3f
        )

        when (sceneType) {
            SceneType.Classic -> {
                // Warm ambient light cone from left desk lamp
                val lightColor = if (isDark) Color(0xFF2DD4BF).copy(alpha = 0.15f) else Color(0xFFFDE047).copy(alpha = 0.22f)
                val lampCone = Path().apply {
                    moveTo(w * 0.12f, h * 0.15f)
                    lineTo(w * 0.45f, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(lampCone, lightColor)

                // 3D Desk Lamp
                drawLine(
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF475569),
                    start = Offset(w * 0.08f, deskTopY + h * 0.10f),
                    end = Offset(w * 0.12f, h * 0.18f),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
                // Lamp shade
                drawArc(
                    color = if (isDark) Color(0xFF2DD4BF) else Color(0xFFF59E0B),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.06f, h * 0.10f),
                    size = Size(w * 0.12f, h * 0.16f)
                )

                // Center: 3D Hanoi Board with colorful disks
                drawSceneMiniHanoi(
                    cx = w * 0.54f,
                    baseY = h * 0.78f,
                    width = w * 0.36f,
                    height = h * 0.42f,
                    isDark = isDark,
                    stands = 3
                )

                // Right: Stack of colorful books
                drawSceneBooks(w * 0.84f, h * 0.76f, w * 0.12f, h * 0.20f, isDark)
            }

            SceneType.Strategic -> {
                // Blueprint grid lines
                val gridColor = if (isDark) Color(0xFF0284C7).copy(alpha = 0.12f) else Color(0xFF0284C7).copy(alpha = 0.08f)
                for (x in 0..10) {
                    val lx = w * (x / 10f)
                    drawLine(gridColor, Offset(lx, 0f), Offset(lx, h), 1.5f)
                }
                for (y in 0..6) {
                    val ly = h * (y / 6f)
                    drawLine(gridColor, Offset(0f, ly), Offset(w, ly), 1.5f)
                }

                // 4-Pillars schematic board in center
                drawSceneMiniHanoi(
                    cx = w * 0.50f,
                    baseY = h * 0.80f,
                    width = w * 0.46f,
                    height = h * 0.44f,
                    isDark = isDark,
                    stands = 4
                )

                // Tech digital clock widget on left
                drawRoundRect(
                    color = if (isDark) Color(0xFF1E293B) else Color.White,
                    topLeft = Offset(w * 0.08f, deskTopY + h * 0.06f),
                    size = Size(w * 0.18f, h * 0.22f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Clock stand
                drawLine(
                    color = Color(0xFF2DD4BF),
                    start = Offset(w * 0.12f, deskTopY + h * 0.17f),
                    end = Offset(w * 0.22f, deskTopY + h * 0.17f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            SceneType.DualTower -> {
                // Dual tone split ambient glow: Red left, Blue right
                val redGlow = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w * 0.5f, 0f)
                    lineTo(w * 0.35f, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(redGlow, Color(0xFFEF4444).copy(alpha = if (isDark) 0.12f else 0.08f))

                val blueGlow = Path().apply {
                    moveTo(w * 0.5f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h)
                    lineTo(w * 0.35f, h)
                    close()
                }
                drawPath(blueGlow, Color(0xFF0EA5E9).copy(alpha = if (isDark) 0.12f else 0.08f))

                // Left Tower (Red)
                drawSingleTower(cx = w * 0.32f, baseY = h * 0.82f, width = w * 0.22f, height = h * 0.40f, isRed = true, isDark = isDark)

                // Swap Curved Arrows in between
                val arrowPath = Path().apply {
                    moveTo(w * 0.44f, h * 0.54f)
                    cubicTo(w * 0.50f, h * 0.46f, w * 0.52f, h * 0.46f, w * 0.56f, h * 0.54f)
                }
                drawPath(arrowPath, Color(0xFFF59E0B), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

                // Right Tower (Blue)
                drawSingleTower(cx = w * 0.68f, baseY = h * 0.82f, width = w * 0.22f, height = h * 0.40f, isRed = false, isDark = isDark)
            }

            SceneType.Tutorial -> {
                // Warm study ambient lamp
                val lampCone = Path().apply {
                    moveTo(w * 0.50f, 0f)
                    lineTo(w * 0.90f, h)
                    lineTo(w * 0.10f, h)
                    close()
                }
                drawPath(lampCone, Color(0xFFFDE047).copy(alpha = if (isDark) 0.10f else 0.18f))

                // Left: Open Guidebook with bookmarks
                drawOpenBook(w * 0.12f, deskTopY + h * 0.06f, w * 0.26f, h * 0.28f, isDark)

                // Right Center: Mini Board with Golden Target Flag on Stand 3
                drawSceneMiniHanoi(
                    cx = w * 0.62f,
                    baseY = h * 0.80f,
                    width = w * 0.38f,
                    height = h * 0.42f,
                    isDark = isDark,
                    stands = 3
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Scene Drawing Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawSceneMiniHanoi(cx: Float, baseY: Float, width: Float, height: Float, isDark: Boolean, stands: Int) {
    val left = cx - width / 2f
    val baseH = height * 0.18f

    // Base
    drawRoundRect(
        color = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
        topLeft = Offset(left, baseY - baseH),
        size = Size(width, baseH),
        cornerRadius = CornerRadius(6f, 6f)
    )

    val lane = width / stands
    val pegW = 5f
    val pegH = height * 0.70f
    val pegTop = baseY - baseH - pegH

    for (s in 0 until stands) {
        val px = left + lane * s + lane / 2f
        drawRoundRect(
            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
            topLeft = Offset(px - pegW / 2f, pegTop),
            size = Size(pegW, pegH),
            cornerRadius = CornerRadius(pegW / 2f, pegW / 2f)
        )
    }

    // Stack on Stand 1
    val s1x = left + lane * 0 + lane / 2f
    val colors = listOf(Color(0xFF38BDF8), Color(0xFF2DD4BF), Color(0xFFFBBF24), Color(0xFFFB923C), Color(0xFFF87171))
    val diskH = (pegH * 0.75f) / 4f

    for (d in 0 until 4) {
        val dw = (pegW * 3.2f) + (lane * 0.85f - pegW * 3.2f) * ((4 - d) / 4f)
        val dy = (baseY - baseH) - (d + 1) * diskH
        drawRoundRect(
            color = colors[d % colors.size],
            topLeft = Offset(s1x - dw / 2f, dy),
            size = Size(dw, diskH - 2f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

private fun DrawScope.drawSingleTower(cx: Float, baseY: Float, width: Float, height: Float, isRed: Boolean, isDark: Boolean) {
    val baseH = height * 0.18f
    val left = cx - width / 2f

    drawRoundRect(
        color = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
        topLeft = Offset(left, baseY - baseH),
        size = Size(width, baseH),
        cornerRadius = CornerRadius(6f, 6f)
    )

    val pegW = 5f
    val pegH = height * 0.70f
    val pegTop = baseY - baseH - pegH

    drawRoundRect(
        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
        topLeft = Offset(cx - pegW / 2f, pegTop),
        size = Size(pegW, pegH),
        cornerRadius = CornerRadius(pegW / 2f, pegW / 2f)
    )

    val colors = if (isRed) {
        listOf(Color(0xFFF87171), Color(0xFFEF4444), Color(0xFFDC2626))
    } else {
        listOf(Color(0xFF38BDF8), Color(0xFF0EA5E9), Color(0xFF0284C7))
    }

    val diskH = (pegH * 0.65f) / 3f
    for (d in 0 until 3) {
        val dw = (pegW * 2.8f) + (width * 0.85f - pegW * 2.8f) * ((3 - d) / 3f)
        val dy = (baseY - baseH) - (d + 1) * diskH
        drawRoundRect(
            color = colors[d % colors.size],
            topLeft = Offset(cx - dw / 2f, dy),
            size = Size(dw, diskH - 2f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

private fun DrawScope.drawSceneBooks(x: Float, y: Float, width: Float, height: Float, isDark: Boolean) {
    val bookColors = listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B))
    val bookH = height / 3f

    for (b in 0 until 3) {
        val by = y - (b + 1) * bookH
        drawRoundRect(
            color = bookColors[b],
            topLeft = Offset(x, by),
            size = Size(width * (1f - b * 0.08f), bookH - 2f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

private fun DrawScope.drawOpenBook(x: Float, y: Float, width: Float, height: Float, isDark: Boolean) {
    val spine = x + width / 2f

    // Left Page
    val leftPage = Path().apply {
        moveTo(spine, y + height * 0.1f)
        lineTo(x, y)
        lineTo(x, y + height)
        lineTo(spine, y + height * 0.9f)
        close()
    }
    drawPath(leftPage, if (isDark) Color(0xFF334155) else Color(0xFFF8FAFC))

    // Right Page
    val rightPage = Path().apply {
        moveTo(spine, y + height * 0.1f)
        lineTo(x + width, y)
        lineTo(x + width, y + height)
        lineTo(spine, y + height * 0.9f)
        close()
    }
    drawPath(rightPage, if (isDark) Color(0xFF475569) else Color(0xFFFFFFFF))
}
