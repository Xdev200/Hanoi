package com.hanoi.binaryhanoi.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoi.binaryhanoi.domain.model.HanoiLevels
import com.hanoi.binaryhanoi.domain.model.Level
import com.hanoi.binaryhanoi.presentation.component.GameBoard
import com.hanoi.binaryhanoi.presentation.theme.ClassicWoodPalette
import com.hanoi.binaryhanoi.presentation.theme.HanoiPalette
import com.hanoi.binaryhanoi.presentation.theme.HanoiThemeMode
import com.hanoi.binaryhanoi.presentation.theme.LightHanoiPalette
import com.hanoi.binaryhanoi.presentation.viewmodel.AppUiState
import com.hanoi.binaryhanoi.presentation.viewmodel.GameViewModel
import com.hanoi.binaryhanoi.presentation.viewmodel.Screen

@Composable
fun HanoiApp(state: AppUiState, viewModel: GameViewModel) {
    val palette = if (state.themeMode == HanoiThemeMode.ClassicWood) ClassicWoodPalette else LightHanoiPalette
    Scaffold(
        topBar = { HanoiTopBar(state, viewModel, palette) },
        bottomBar = { HanoiNav(state.screen, viewModel) },
        containerColor = palette.background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(palette.background)
        ) {
            when (state.screen) {
                Screen.Home -> HomeScreen(viewModel, palette)
                Screen.Play -> PlayScreen(state, viewModel)
                Screen.Game -> GameScreen(state, viewModel, palette)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HanoiTopBar(state: AppUiState, viewModel: GameViewModel, palette: HanoiPalette) {
    TopAppBar(
        title = { Text("Hanoi") },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dark", color = palette.muted, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = state.themeMode == HanoiThemeMode.ClassicWood,
                    onCheckedChange = { viewModel.toggleTheme() }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = palette.background,
            titleContentColor = palette.text,
            actionIconContentColor = palette.text
        )
    )
}

@Composable
private fun HanoiNav(current: Screen, viewModel: GameViewModel) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NavItem(current == Screen.Home, Icons.Rounded.Home, "Home", Modifier.weight(1f)) { viewModel.show(Screen.Home) }
            NavItem(current == Screen.Play || current == Screen.Game, Icons.Rounded.PlayArrow, "Play", Modifier.weight(1f)) { viewModel.show(Screen.Play) }
        }
    }
}

@Composable
private fun NavItem(selected: Boolean, icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
            Spacer(Modifier.width(8.dp))
            Text(label, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HomeScreen(viewModel: GameViewModel, palette: HanoiPalette) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Learn the puzzle", color = palette.text, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("A compact guide to recursion and binary moves.", color = palette.muted)
        }
        item { InsightCard("Rule 1", "Move only the top disk from any peg.", palette) }
        item { InsightCard("Rule 2", "Never place a larger disk on a smaller disk.", palette) }
        item { InsightCard("Recursive idea", "Move n-1 disks aside, move the largest disk, then rebuild n-1 disks on top.", palette) }
        item { InsightCard("Binary insight", "Every move changes the binary counter. The low bits move often; higher bits move less often.", palette) }
        item {
            Button(onClick = viewModel::startTutorialAuto, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start tutorial")
            }
        }
    }
}

@Composable
private fun PlayScreen(state: AppUiState, viewModel: GameViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PlayerCard(state, viewModel) }
        items(HanoiLevels) { level ->
            LevelRow(level = level, completed = level.id in state.game.completedLevels, onClick = { viewModel.startLevel(level.id) })
        }
    }
}

@Composable
private fun PlayerCard(state: AppUiState, viewModel: GameViewModel) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Player", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = state.playerName,
                onValueChange = viewModel::updatePlayerName,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Name") }
            )
            Text("${state.game.completedLevels.size} of ${HanoiLevels.size} levels complete", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun LevelRow(level: Level, completed: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Text(level.id.toString(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(level.title, fontWeight = FontWeight.Bold)
                Text("${level.pegCount} pegs · ${level.diskCount} disks", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 13.sp)
            }
            if (completed) Icon(Icons.Rounded.CheckCircle, contentDescription = "Complete", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun GameScreen(state: AppUiState, viewModel: GameViewModel, palette: HanoiPalette) {
    val game = state.game
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Level ${game.level.id}", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("${game.level.diskCount} disks · ${game.level.pegCount} pegs", color = palette.muted)
                }
                Text("${game.moveCount}/${viewModel.optimalForCurrent()}", color = palette.success, fontWeight = FontWeight.Bold)
            }
        }
        item { GameBoard(state = game, palette = palette, onPegTap = viewModel::selectPeg) }
        item { BinaryCounter(game.moveCount, game.level.diskCount, palette) }
        item { ActionRow(state, viewModel) }
        item {
            AnimatedVisibility(game.isComplete) {
                InsightCard("Solved", "Move ${game.moveCount} is ${game.moveCount.toString(2)} in binary.", palette)
            }
        }
        item {
            AnimatedVisibility(game.showRecursion) {
                RecursionTree(game.level.diskCount, palette)
            }
        }
    }
}

@Composable
private fun BinaryCounter(moveCount: Int, diskCount: Int, palette: HanoiPalette) {
    Surface(shape = RoundedCornerShape(8.dp), color = palette.surface) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Binary counter", fontWeight = FontWeight.Bold, color = palette.text)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                moveCount.toString(2).padStart(diskCount, '0').takeLast(diskCount).forEach { bit ->
                    Box(Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(8.dp)).background(if (bit == '1') palette.accent else palette.board), contentAlignment = Alignment.Center) {
                        Text(bit.toString(), color = if (bit == '1') Color(0xFF10231F) else palette.text, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(state: AppUiState, viewModel: GameViewModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = viewModel::undo, modifier = Modifier.weight(1f)) { Icon(Icons.Rounded.Undo, contentDescription = "Undo") }
        IconButton(onClick = viewModel::hint, modifier = Modifier.weight(1f)) { Icon(Icons.Rounded.Lightbulb, contentDescription = "Hint") }
        IconButton(onClick = viewModel::reset, modifier = Modifier.weight(1f)) { Icon(Icons.Rounded.RestartAlt, contentDescription = "Reset") }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = viewModel::autoSolve, enabled = !state.game.solverRunning, modifier = Modifier.weight(1f)) {
            Icon(Icons.Rounded.AutoAwesome, null)
            Spacer(Modifier.width(8.dp))
            Text("Auto")
        }
        ElevatedButton(onClick = viewModel::toggleRecursion, modifier = Modifier.weight(1f)) {
            Icon(Icons.Rounded.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Tree")
        }
    }
}

@Composable
private fun InsightCard(title: String, body: String, palette: HanoiPalette) {
    Surface(shape = RoundedCornerShape(8.dp), color = palette.surface, tonalElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = palette.text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(body, color = palette.muted, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun RecursionTree(disks: Int, palette: HanoiPalette) {
    Surface(shape = RoundedCornerShape(8.dp), color = palette.surface) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Recursion tree", color = palette.text, fontWeight = FontWeight.Bold)
            Text("T($disks) = T(${disks - 1}) + 1 + T(${disks - 1})", color = palette.muted, textAlign = TextAlign.Center)
            LinearProgressIndicator(progress = { 0.72f }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp), color = palette.accent)
        }
    }
}
