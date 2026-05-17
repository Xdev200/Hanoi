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
@Composable
fun GameBoard(
    state: GameState,
    palette: HanoiPalette,
    onPegTap: (PegId) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate a gentle float offset for the selected (source) disk stack
    val liftAnim: Float by animateFloatAsState(
        targetValue = if (state.selectedPeg != null) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "liftAnim"
    )

    Box(modifier = modifier.fillMaxWidth().height(360.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp)) {
            val pegCount = state.activePegs.size
            val lane = size.width / pegCount
            val baseY = size.height * 0.86f          // top of the base platform
            val diskHeight = (size.height * 0.072f).coerceIn(24f, 36f)
            val pegWidth = 16f
            val pegCapRadius = pegWidth * 1.1f

            // ── Base platform (3D box effect) ───────────────────────────────────
            draw3DBase(
                color = palette.board,
                left = 0f,
                top = baseY,
                width = size.width,
                height = 32f
            )

            // ── Pegs ────────────────────────────────────────────────────────────
            state.activePegs.forEachIndexed { index, peg ->
                val cx = lane * index + lane / 2f
                val pegTop = size.height * 0.10f
                val selectionState = when {
                    state.selectedPeg == peg -> SelectionState.Source
                    state.selectedPeg != null -> SelectionState.Destination
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

                // ── Disks on this peg ────────────────────────────────────────
                val disksOnPeg = state.pegs[peg].orEmpty()
                val isSourcePeg = state.selectedPeg == peg
                disksOnPeg.forEachIndexed { diskIndex, disk ->
                    val normalized = disk.size.toFloat() / state.level.diskCount
                    val maxWidth = lane * 0.88f
                    val minWidth = pegWidth * 3f
                    val diskWidth = minWidth + (maxWidth - minWidth) * normalized

                    // Lift the topmost disk on the source peg
                    val liftPixels = if (isSourcePeg && diskIndex == disksOnPeg.lastIndex)
                        liftAnim * diskHeight * 1.4f else 0f

                    val diskY = baseY - 4f - (diskIndex + 1) * diskHeight - liftPixels
                    val diskColor = palette.diskColors[(disk.id - 1) % palette.diskColors.size]

                    draw3DDisk(
                        cx = cx,
                        y = diskY,
                        width = diskWidth,
                        height = diskHeight - 3f,
                        color = diskColor
                    )
                }
            }
        }

        // Transparent tap targets layered over the canvas
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

@Composable
fun paletteForDark(isClassic: Boolean): HanoiPalette =
    if (isClassic) ClassicWoodPalette else LightHanoiPalette
