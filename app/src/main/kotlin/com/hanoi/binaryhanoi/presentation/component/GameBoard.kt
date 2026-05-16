package com.hanoi.binaryhanoi.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hanoi.binaryhanoi.domain.model.GameState
import com.hanoi.binaryhanoi.domain.model.PegId
import com.hanoi.binaryhanoi.presentation.theme.ClassicWoodPalette
import com.hanoi.binaryhanoi.presentation.theme.HanoiPalette
import com.hanoi.binaryhanoi.presentation.theme.LightHanoiPalette

@Composable
fun GameBoard(
    state: GameState,
    palette: HanoiPalette,
    onPegTap: (PegId) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().height(330.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val pegCount = state.level.pegCount
            val lane = size.width / pegCount
            val baseY = size.height * 0.84f
            val diskHeight = (size.height * 0.075f).coerceIn(22f, 34f)

            drawRoundRect(
                color = palette.board,
                topLeft = Offset(0f, baseY),
                size = Size(size.width, 28f),
                cornerRadius = CornerRadius(14f, 14f)
            )

            state.activePegs.forEachIndexed { index, peg ->
                val x = lane * index + lane / 2f
                val selected = state.selectedPeg == peg
                val hinted = state.hint?.from == peg || state.hint?.to == peg

                drawRoundRect(
                    color = palette.peg,
                    topLeft = Offset(x - 7f, size.height * 0.16f),
                    size = Size(14f, baseY - size.height * 0.16f),
                    cornerRadius = CornerRadius(7f, 7f)
                )

                if (selected || hinted) {
                    drawCircle(
                        color = if (selected) palette.accent else palette.success,
                        radius = lane * 0.34f,
                        center = Offset(x, baseY - 28f),
                        style = Stroke(width = 5f)
                    )
                }

                state.pegs[peg].orEmpty().forEachIndexed { diskIndex, disk ->
                    val normalized = disk.size.toFloat() / state.level.diskCount
                    val width = (lane * (0.34f + normalized * 0.56f)).coerceAtMost(lane - 12f)
                    val y = baseY - 6f - (diskIndex + 1) * diskHeight
                    drawRoundRect(
                        color = palette.diskColors[(disk.id - 1) % palette.diskColors.size],
                        topLeft = Offset(x - width / 2f, y),
                        size = Size(width, diskHeight - 4f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.22f),
                        topLeft = Offset(x - width / 2f + 8f, y + 5f),
                        size = Size(width - 16f, 4f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
        }

        Row(Modifier.fillMaxSize()) {
            state.activePegs.forEach { peg ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                        .clickable { onPegTap(peg) },
                    contentAlignment = Alignment.BottomCenter
                ) {}
            }
        }
    }
}

@Composable
fun paletteForDark(isClassic: Boolean): HanoiPalette = if (isClassic) ClassicWoodPalette else LightHanoiPalette
