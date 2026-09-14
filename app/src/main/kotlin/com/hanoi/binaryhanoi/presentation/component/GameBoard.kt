package com.hanoi.binaryhanoi.presentation.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hanoi.binaryhanoi.domain.model.Disk
import com.hanoi.binaryhanoi.domain.model.GameState
import com.hanoi.binaryhanoi.domain.model.PegId
import com.hanoi.binaryhanoi.presentation.theme.ClassicWoodPalette
import com.hanoi.binaryhanoi.presentation.theme.HanoiPalette
import com.hanoi.binaryhanoi.presentation.theme.LightHanoiPalette

/**
 * Renders a realistic 3D Tower of Hanoi board.
 *
 * Visual differentiation for peg selection states:
 *  - [SelectionState.Source]      → peg is being picked from (disk "lifted"); golden pulsing ring
 *  - [SelectionState.Destination] → peg is a valid drop target; teal dashed ring
 *  - [SelectionState.None]        → no highlight
 */
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@Composable
fun GameBoard(
    state: GameState,
    palette: HanoiPalette,
    onPegTap: (PegId) -> Unit,
    modifier: Modifier = Modifier,
    canvasHeight: Dp = 275.dp,
    flightDisk: Disk? = null,
    flightFrom: PegId? = null,
    flightTo: PegId? = null,
    flightProgress: Float = 0f,
    showHand: Boolean = false
) {
    val liftAnim: Float by animateFloatAsState(
        targetValue = if (state.selectedPeg != null) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "liftAnim"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(canvasHeight)) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp)) {
                val pegCount = state.activePegs.size
                val lane = size.width / pegCount
                val baseY = size.height - 24f
                val diskHeight = (size.height * 0.09f).coerceIn(22f, 32f)
                val pegWidth = 14f
                val pegCapRadius = pegWidth * 1.1f

                // Base platform
                draw3DBase(
                    color = palette.board,
                    left = 0f,
                    top = baseY,
                    width = size.width,
                    height = 24f
                )

                // Pegs
                state.activePegs.forEachIndexed { index, peg ->
                    val cx = lane * index + lane / 2f
                    val pegTop = size.height * 0.08f
                    val isHintFrom = state.hint?.from == peg || (state.isTutorialMode && state.tutorialStepInfo?.recommendedMove?.from == peg)
                    val isHintTo = state.hint?.to == peg || (state.isTutorialMode && state.tutorialStepInfo?.recommendedMove?.to == peg)
                    val selectionState = when {
                        state.selectedPeg == peg -> SelectionState.Source
                        state.selectedPeg != null -> SelectionState.Destination
                        isHintFrom -> SelectionState.Source
                        isHintTo -> SelectionState.Destination
                        else -> SelectionState.None
                    }

                    draw3DPeg(
                        cx = cx,
                        pegTop = pegTop,
                        baseY = baseY,
                        pegWidth = pegWidth,
                        capRadius = pegCapRadius,
                        pegColor = palette.peg,
                        selectionState = selectionState,
                        accentColor = palette.accent,
                        successColor = palette.success
                    )

                    // Disks on this peg (if top disk is in flight from this peg, don't draw it on the peg)
                    val rawDisks = state.pegs[peg].orEmpty()
                    val disksOnPeg = if (peg == flightFrom && flightDisk != null && rawDisks.isNotEmpty()) {
                        rawDisks.dropLast(1)
                    } else {
                        rawDisks
                    }

                    val isSourcePeg = state.selectedPeg == peg
                    disksOnPeg.forEachIndexed { diskIndex, disk ->
                        val normalized = disk.size.toFloat() / state.level.diskCount
                        val maxWidth = lane * 0.88f
                        val minWidth = pegWidth * 3f
                        val diskWidth = minWidth + (maxWidth - minWidth) * normalized

                        val liftPixels = when {
                            isSourcePeg && diskIndex == disksOnPeg.lastIndex -> liftAnim * diskHeight * 1.4f
                            isHintFrom && diskIndex == disksOnPeg.lastIndex && state.selectedPeg == null -> diskHeight * 0.42f
                            else -> 0f
                        }

                        val diskY = baseY - 2f - (diskIndex + 1) * diskHeight - liftPixels
                        val diskColor = if (state.level.mode == com.hanoi.binaryhanoi.domain.model.GameMode.Bicolor) {
                            if (disk.colorGroup == 0) {
                                when (disk.size) {
                                    1 -> Color(0xFFFF6E6E)
                                    2 -> Color(0xFFE53935)
                                    3 -> Color(0xFFC62828)
                                    else -> Color(0xFFB71C1C)
                                }
                            } else {
                                when (disk.size) {
                                    1 -> Color(0xFF4FC3F7)
                                    2 -> Color(0xFF1E88E5)
                                    3 -> Color(0xFF1565C0)
                                    else -> Color(0xFF0D47A1)
                                }
                            }
                        } else {
                            palette.diskColors[(disk.id - 1) % palette.diskColors.size]
                        }

                        draw3DDisk(
                            cx = cx,
                            y = diskY,
                            width = diskWidth,
                            height = diskHeight - 3f,
                            color = diskColor
                        )
                    }
                }

                // ── In-Flight Animated Disk & 3D Cartoon Hand (Tutorial & Solver) ────
                if (flightDisk != null && flightFrom != null && flightTo != null) {
                    val fromIdx = state.activePegs.indexOf(flightFrom)
                    val toIdx = state.activePegs.indexOf(flightTo)
                    if (fromIdx >= 0 && toIdx >= 0) {
                        val fromCx = lane * fromIdx + lane / 2f
                        val toCx = lane * toIdx + lane / 2f
                        val pegTop = size.height * 0.08f
                        val flyApexY = pegTop - diskHeight * 1.55f

                        val sourceStackCount = (state.pegs[flightFrom].orEmpty().size - 1).coerceAtLeast(0)
                        val sourceDiskY = baseY - 2f - (sourceStackCount + 1) * diskHeight
                        val destStackCount = state.pegs[flightTo].orEmpty().size
                        val destDiskY = baseY - 2f - (destStackCount + 1) * diskHeight

                        val p = flightProgress.coerceIn(0f, 1f)

                        val curX: Float
                        val curY: Float
                        val handX: Float
                        val handY: Float
                        val isGrasping: Boolean

                        when {
                            p < 0.16f -> {
                                // Phase 1 (0.00..0.16) — Hand glides down to hover above source disk
                                val t = p / 0.16f
                                val easeT = 1f - (1f - t) * (1f - t)
                                curX = fromCx
                                curY = sourceDiskY
                                handX = fromCx
                                handY = (sourceDiskY - 60f) + 60f * easeT
                                isGrasping = false
                            }
                            p < 0.44f -> {
                                // Phase 2 (0.16..0.44) — Hand grasps disk and lifts straight up
                                val t = (p - 0.16f) / 0.28f
                                val easeT = if (t < 0.5f) 2f * t * t else 1f - (-2f * t + 2f).let { it * it } / 2f
                                curX = fromCx
                                curY = sourceDiskY + (flyApexY - sourceDiskY) * easeT
                                handX = curX
                                handY = curY
                                isGrasping = true
                            }
                            p < 0.76f -> {
                                // Phase 3 (0.44..0.76) — Hand carries disk horizontally across in arc
                                val t = (p - 0.44f) / 0.32f
                                val easeT = if (t < 0.5f) 2f * t * t else 1f - (-2f * t + 2f).let { it * it } / 2f
                                curX = fromCx + (toCx - fromCx) * easeT
                                val arc = kotlin.math.sin(t * Math.PI.toFloat()) * (diskHeight * 0.85f)
                                curY = flyApexY - arc
                                handX = curX
                                handY = curY
                                isGrasping = true
                            }
                            p < 0.92f -> {
                                // Phase 4 (0.76..0.92) — Hand lowers disk down onto destination peg
                                val t = (p - 0.76f) / 0.16f
                                val easeT = t * t
                                curX = toCx
                                curY = flyApexY + (destDiskY - flyApexY) * easeT
                                handX = curX
                                handY = curY
                                isGrasping = true
                            }
                            else -> {
                                // Phase 5 (0.92..1.00) — Hand releases disk, disk settles, hand floats away
                                val t = (p - 0.92f) / 0.08f
                                curX = toCx
                                curY = destDiskY
                                handX = toCx
                                handY = destDiskY - 45f * t
                                isGrasping = false
                            }
                        }

                        val normalized = flightDisk.size.toFloat() / state.level.diskCount
                        val maxWidth = lane * 0.88f
                        val minWidth = pegWidth * 3f
                        val diskW = minWidth + (maxWidth - minWidth) * normalized
                        val diskColor = palette.diskColors[(flightDisk.id - 1) % palette.diskColors.size]

                        // Motion aura glow under the flying disk
                        drawCircle(
                            color = Color(0xFF2DD4BF).copy(alpha = 0.30f),
                            radius = diskW * 0.52f,
                            center = Offset(curX, curY + diskHeight * 0.5f)
                        )
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = 0.22f),
                            radius = diskW * 0.40f,
                            center = Offset(curX, curY + diskHeight * 0.5f)
                        )

                        // 3D disk body
                        draw3DDisk(
                            cx = curX,
                            y = curY,
                            width = diskW,
                            height = diskHeight - 3f,
                            color = diskColor
                        )

                        // Animated Cartoon Hand
                        if (showHand) {
                            drawCartoonHand(
                                cx = handX,
                                cy = handY,
                                isGrasping = isGrasping
                            )
                        }
                    }
                }
            }

            // Tap targets
            Row(Modifier.fillMaxSize()) {
                state.activePegs.forEach { peg ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { onPegTap(peg) },
                        contentAlignment = Alignment.BottomCenter
                    ) {}
                }
            }
        }

        // Stand / Pole labels row directly underneath the base
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isBicolor = state.level.mode == com.hanoi.binaryhanoi.domain.model.GameMode.Bicolor
            state.activePegs.forEachIndexed { index, peg ->
                val standNumber = index + 1
                val isTargetPeg = index == state.activePegs.lastIndex
                val isStartPeg = index == 0

                val roleTag: String
                val bgColor: Color
                val textColor: Color
                val borderColor: Color

                val isHintFrom = state.hint?.from == peg || (state.isTutorialMode && state.tutorialStepInfo?.recommendedMove?.from == peg)
                val isHintTo = state.hint?.to == peg || (state.isTutorialMode && state.tutorialStepInfo?.recommendedMove?.to == peg)

                if (isHintFrom) {
                    roleTag = "💡 PICK UP"
                    bgColor = Color(0x33F59E0B)
                    textColor = Color(0xFFF59E0B)
                    borderColor = Color(0xFFF59E0B)
                } else if (isHintTo) {
                    roleTag = "🎯 DROP HERE"
                    bgColor = Color(0x332DD4BF)
                    textColor = Color(0xFF2DD4BF)
                    borderColor = Color(0xFF2DD4BF)
                } else if (isBicolor) {
                    when (index) {
                        0 -> {
                            roleTag = "🟦 Blue Goal"
                            bgColor = Color(0xFF1E88E5).copy(alpha = 0.22f)
                            textColor = Color(0xFF42A5F5)
                            borderColor = Color(0xFF1E88E5).copy(alpha = 0.5f)
                        }
                        2 -> {
                            roleTag = "🟥 Red Goal"
                            bgColor = Color(0xFFE53935).copy(alpha = 0.22f)
                            textColor = Color(0xFFFF6E6E)
                            borderColor = Color(0xFFE53935).copy(alpha = 0.5f)
                        }
                        else -> {
                            roleTag = "Spare"
                            bgColor = palette.surface.copy(alpha = 0.85f)
                            textColor = palette.text
                            borderColor = palette.muted.copy(alpha = 0.3f)
                        }
                    }
                } else {
                    roleTag = when {
                        isTargetPeg -> "🎯 Target"
                        isStartPeg -> "Start"
                        else -> "Spare"
                    }
                    bgColor = if (isTargetPeg) Color(0x33F59E0B) else palette.surface.copy(alpha = 0.85f)
                    textColor = if (isTargetPeg) Color(0xFFF59E0B) else palette.text
                    borderColor = if (isTargetPeg) Color(0xFFF59E0B) else palette.muted.copy(alpha = 0.3f)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = bgColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Stand $standNumber",
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Text(
                            text = roleTag,
                            color = textColor,
                            fontSize = 10.sp,
                            fontWeight = if (isTargetPeg || (isBicolor && (index == 0 || index == 2))) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ── Private drawing helpers ──────────────────────────────────────────────────

private enum class SelectionState { None, Source, Destination }

/** Draws a 3D-looking base platform with a top face and a front face. */
private fun DrawScope.draw3DBase(color: Color, left: Float, top: Float, width: Float, height: Float) {
    val depth = 10f
    // Front face (lighter)
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Top bevel highlight
    drawRoundRect(
        color = Color.White.copy(alpha = 0.18f),
        topLeft = Offset(left, top),
        size = Size(width, 8f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Bottom shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(left, top + height - depth),
        size = Size(width, depth),
        cornerRadius = CornerRadius(0f, 8f)
    )
}

/**
 * Draws a cylindrical peg with rounded cap, gradient side, and optional selection ring.
 *
 * Selection visual language:
 *  - Source  → thick golden ring + subtle glow (you picked this peg's disk)
 *  - Destination → thin teal dashed-style ring (valid drop zone)
 */
private fun DrawScope.draw3DPeg(
    cx: Float,
    pegTop: Float,
    baseY: Float,
    pegWidth: Float,
    capRadius: Float,
    pegColor: Color,
    selectionState: SelectionState,
    accentColor: Color,
    successColor: Color
) {
    val pegHeight = baseY - pegTop
    val halfW = pegWidth / 2f

    // Shadow on base
    drawOval(
        color = Color.Black.copy(alpha = 0.15f),
        topLeft = Offset(cx - pegWidth * 1.6f, baseY - 5f),
        size = Size(pegWidth * 3.2f, 12f)
    )

    // Peg body — left-to-right gradient to simulate cylinder roundness
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                pegColor.copy(alpha = 0.55f),
                pegColor,
                pegColor.copy(alpha = 0.7f)
            ),
            startX = cx - halfW,
            endX = cx + halfW
        ),
        topLeft = Offset(cx - halfW, pegTop),
        size = Size(pegWidth, pegHeight)
    )

    // Specular highlight (thin vertical streak on left-centre)
    drawRect(
        color = Color.White.copy(alpha = 0.28f),
        topLeft = Offset(cx - halfW + 2f, pegTop + pegHeight * 0.05f),
        size = Size(halfW * 0.45f, pegHeight * 0.7f)
    )

    // Rounded cap at the top
    drawCircle(
        color = pegColor,
        radius = capRadius,
        center = Offset(cx, pegTop)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.30f),
        radius = capRadius * 0.65f,
        center = Offset(cx - capRadius * 0.2f, pegTop - capRadius * 0.2f)
    )

    // Selection indicator
    when (selectionState) {
        SelectionState.Source -> {
            // Thick golden ring — "this disk is lifted"
            drawCircle(
                color = successColor,
                radius = capRadius * 2.6f,
                center = Offset(cx, pegTop),
                style = Stroke(width = 5f)
            )
            // Soft outer glow
            drawCircle(
                color = successColor.copy(alpha = 0.18f),
                radius = capRadius * 3.4f,
                center = Offset(cx, pegTop)
            )
        }
        SelectionState.Destination -> {
            // Thinner teal ring — "you can drop here"
            drawCircle(
                color = accentColor,
                radius = capRadius * 2.2f,
                center = Offset(cx, pegTop),
                style = Stroke(width = 3f)
            )
            // Subtle fill
            drawCircle(
                color = accentColor.copy(alpha = 0.08f),
                radius = capRadius * 2.2f,
                center = Offset(cx, pegTop)
            )
        }
        SelectionState.None -> Unit
    }
}

/**
 * Draws a single 3D disk — rounded rect body with top highlight, bottom shadow, and
 * a subtle bevel on the left edge to simulate depth/thickness.
 */
private fun DrawScope.draw3DDisk(cx: Float, y: Float, width: Float, height: Float, color: Color) {
    val left = cx - width / 2f
    val cornerR = height / 2f   // fully rounded ends

    // Drop shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.22f),
        topLeft = Offset(left + 4f, y + height * 0.55f),
        size = Size(width, height * 0.55f),
        cornerRadius = CornerRadius(cornerR, cornerR)
    )

    // Disk body
    drawRoundRect(
        color = color,
        topLeft = Offset(left, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerR, cornerR)
    )

    // Highlight stripe along the top (simulates curved top surface catching light)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = Offset(left + cornerR, y + 3f),
        size = Size(width - cornerR * 2f, height * 0.28f),
        cornerRadius = CornerRadius(cornerR * 0.5f, cornerR * 0.5f)
    )

    // Bottom rim shadow (darker edge)
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.18f),
        topLeft = Offset(left + 2f, y + height * 0.72f),
        size = Size(width - 4f, height * 0.26f),
        cornerRadius = CornerRadius(cornerR, cornerR)
    )

    // Thin outline for crispness
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(left, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerR, cornerR),
        style = Stroke(width = 1.5f)
    )
}

/**
 * Draws a clean cartoonish robotic/gloved hand performing the physical pick & drop of disks.
 * - [isGrasping] = true when fingers curl to grip the disk rim during lift and flight.
 * - [isGrasping] = false when hand is open/reaching down or releasing.
 */
private fun DrawScope.drawCartoonHand(
    cx: Float,
    cy: Float,
    isGrasping: Boolean
) {
    val handColor = Color(0xFFFFD54F) // Warm gold cartoon skin
    val shadowColor = Color(0xFFFFA000)
    val sleeveColor = Color(0xFF0284C7) // Blue sleeve
    val cuffColor = Color(0xFFFFFFFF)  // White cartoon cuff

    // Sleeve coming from above
    drawRoundRect(
        color = sleeveColor,
        topLeft = Offset(cx - 10f, cy - 44f),
        size = Size(20f, 20f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Glove cuff
    drawRoundRect(
        color = cuffColor,
        topLeft = Offset(cx - 13f, cy - 26f),
        size = Size(26f, 9f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    if (isGrasping) {
        // Closed / Grasping palm
        drawCircle(
            color = shadowColor,
            radius = 13f,
            center = Offset(cx, cy - 13f)
        )
        drawCircle(
            color = handColor,
            radius = 12f,
            center = Offset(cx, cy - 14f)
        )
        // 3 grasping finger knuckles gripping top of disk
        for (i in -1..1) {
            drawRoundRect(
                color = shadowColor,
                topLeft = Offset(cx + i * 8f - 4f, cy - 6f),
                size = Size(8f, 9f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = handColor,
                topLeft = Offset(cx + i * 8f - 4f, cy - 7f),
                size = Size(8f, 9f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    } else {
        // Open reaching palm
        drawCircle(
            color = shadowColor,
            radius = 12f,
            center = Offset(cx, cy - 15f)
        )
        drawCircle(
            color = handColor,
            radius = 11f,
            center = Offset(cx, cy - 16f)
        )
        // Reaching fingers
        for (i in -1..1) {
            val fLen = if (i == 0) 14f else 11f
            drawRoundRect(
                color = shadowColor,
                topLeft = Offset(cx + i * 7f - 3f, cy - 9f),
                size = Size(6f, fLen),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = handColor,
                topLeft = Offset(cx + i * 7f - 3f, cy - 10f),
                size = Size(6f, fLen),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}

@Composable
fun paletteForDark(isClassic: Boolean): HanoiPalette =
    if (isClassic) ClassicWoodPalette else LightHanoiPalette

