package com.hanoi.binaryhanoi.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hanoi.binaryhanoi.R
import com.hanoi.binaryhanoi.domain.model.Disk
import com.hanoi.binaryhanoi.domain.model.HanoiLevels
import com.hanoi.binaryhanoi.domain.model.Level
import com.hanoi.binaryhanoi.domain.model.Move
import com.hanoi.binaryhanoi.presentation.component.GameBoard
import com.hanoi.binaryhanoi.presentation.component.AvatarRobot3D
import com.hanoi.binaryhanoi.presentation.component.AvatarUser3D
import com.hanoi.binaryhanoi.presentation.component.DeskHeroScene3D
import com.hanoi.binaryhanoi.presentation.component.LevelMiniBoard3D
import com.hanoi.binaryhanoi.presentation.component.SceneType
import com.hanoi.binaryhanoi.presentation.component.StickerBadge3D
import com.hanoi.binaryhanoi.presentation.component.StickerType
import com.hanoi.binaryhanoi.presentation.theme.ClassicWoodPalette
import com.hanoi.binaryhanoi.presentation.theme.HanoiPalette
import com.hanoi.binaryhanoi.presentation.theme.HanoiThemeMode
import com.hanoi.binaryhanoi.presentation.theme.LightHanoiPalette
import com.hanoi.binaryhanoi.presentation.viewmodel.AppUiState
import com.hanoi.binaryhanoi.presentation.viewmodel.GameViewModel
import com.hanoi.binaryhanoi.presentation.viewmodel.Screen
import com.hanoi.binaryhanoi.presentation.viewmodel.TutorialPhase
import com.hanoi.binaryhanoi.presentation.viewmodel.tutorialMoveSteps

@Composable
fun HanoiApp(state: AppUiState, viewModel: GameViewModel) {
    val palette = if (state.themeMode == HanoiThemeMode.ClassicWood) ClassicWoodPalette else LightHanoiPalette
    val isDark = state.themeMode == HanoiThemeMode.ClassicWood

    // Only intercept device back when actively playing a normal game level (to go back to Levels list)
    // Launch screen (Home), Levels screen (Play), and Tutorial screen are one-way (back exits app)
    BackHandler(enabled = state.screen == Screen.Game && !state.game.isTutorialMode) {
        viewModel.goBack()
    }

    Scaffold(
        containerColor = palette.background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(palette.background)
        ) {
            when (state.screen) {
                Screen.Home -> HomeScreen(state, viewModel, palette)
                Screen.Play -> PlayScreen(state, viewModel, palette)
                Screen.Game -> if (state.game.isTutorialMode) {
                    TutorialScreen(state, viewModel, palette)
                } else {
                    GameScreen(state, viewModel, palette)
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(state: AppUiState, viewModel: GameViewModel, palette: HanoiPalette) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    val isDark = state.themeMode == HanoiThemeMode.ClassicWood
    val bgRes = if (isDark) R.drawable.bg_home_dark else R.drawable.bg_home_light

    // 3D Micro-Animations for floating icons and pulsating CTA
    val infiniteTransition = rememberInfiniteTransition(label = "3D Animations")

    val floatAnim1 by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )
    val floatAnim2 by infiniteTransition.animateFloat(
        initialValue = 3.5f,
        targetValue = -3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )
    val floatAnim3 by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float3"
    )

    val ctaScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.026f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ctaScale"
    )

    if (showSettingsDialog) {
        SettingsDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full screen 3D Rendered Desk Background
        Image(
            painter = painterResource(id = bgRes),
            contentDescription = "Tower of Hanoi 3D Desk Scene",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay for smooth readability & focus
        val overlayBrush = if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xCC060A12),
                    Color(0x55060A12),
                    Color(0x1A000000),
                    Color(0x88060A12),
                    Color(0xF0060A12)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xC0FFFFFF),
                    Color(0x33FFFFFF),
                    Color(0x10FFFFFF),
                    Color(0x66FFFFFF),
                    Color(0xD9FFFFFF)
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayBrush)
        )

        // Fixed non-scrollable content — everything fits in one viewport
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP HEADER: Hanoi Logo + Title + Theme Switch Capsule + Settings Gear
            HomeHeader(
                isDark = isDark,
                onToggleTheme = { viewModel.toggleTheme() },
                onOpenSettings = { showSettingsDialog = true }
            )

            Spacer(Modifier.height(8.dp))

            // HERO LABELS & HEADINGS
            Text(
                text = "C L A S S I C   P U Z Z L E",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Tower of",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Text(
                text = "Hanoi",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF59E0B)
            )

            Spacer(Modifier.height(4.dp))

            // Golden accent bar
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFF59E0B))
            )

            // SPACING THAT SHOWCASES THE 3D WOODEN HANOI STAND ON THE DESK
            Spacer(Modifier.height(100.dp))

            // COMPACT RULES — 3 simple text rows replacing the heavy cards
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xDD131B2E) else Color(0xEEFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0x332DD4BF) else Color(0xFFE2E8F0)),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeCompactRuleRow(
                        emoji = "☝️",
                        label = "Rule 1",
                        text = "Move only 1 top disk at a time from any stand.",
                        isDark = isDark
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (isDark) Color(0x22FFFFFF) else Color(0x18000000))
                    )
                    HomeCompactRuleRow(
                        emoji = "🚫",
                        label = "Rule 2",
                        text = "Never place a larger disk on top of a smaller one.",
                        isDark = isDark
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (isDark) Color(0x22FFFFFF) else Color(0x18000000))
                    )
                    HomeCompactRuleRow(
                        emoji = "🏆",
                        label = "Optimal",
                        text = "3 disks = 7 moves minimum. Fewer moves = more stars!",
                        isDark = isDark,
                        highlight = true
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── CTA: Game Menu for returning players, Tutorial CTA for first-timers ─
            if (state.tutorialSeen) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 22.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ▶ Play Levels — primary action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 14.dp,
                                shape = RoundedCornerShape(30.dp),
                                ambientColor = Color(0xFF2DD4BF),
                                spotColor = Color(0xFF2DD4BF)
                            )
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF4EE3CE))
                            .clickable { viewModel.show(Screen.Play) }
                            .padding(vertical = 15.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Play Levels",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Watch tutorial again — secondary link
                    TextButton(onClick = { viewModel.startTutorialInteractive() }) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Watch Tutorial Again",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            } else {
                // ── First-time Tutorial CTA ───────────────────────────────────
                Box(
                    modifier = Modifier
                        .padding(horizontal = 22.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = ctaScale
                            scaleY = ctaScale
                        }
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(30.dp),
                            ambientColor = Color(0xFF2DD4BF),
                            spotColor = Color(0xFF2DD4BF)
                        )
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFF4EE3CE))
                        .clickable { viewModel.startTutorialInteractive() }
                        .padding(vertical = 15.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Start Interactive Tutorial",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // INSPIRATIONAL TAGLINE
            Text(
                text = "“Small moves lead to big thinking.”",
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
            )

            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFF59E0B))
            )
        }
    }
}

@Composable
private fun HomeHeader(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 3D Hanoi mini logo + Title & Subtitle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.hanoi_mini_3d),
                contentDescription = "Hanoi Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Hanoi",
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "Think • Move • Solve",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
        }

        // Right: Theme Toggle Capsule + Settings Gear
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme Switch Pill
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (isDark) Color(0xCC1E293B) else Color(0xEEFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x33000000)),
                shadowElevation = 4.dp,
                modifier = Modifier.clickable { onToggleTheme() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        contentDescription = if (isDark) "Dark Mode" else "Light Mode",
                        tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isDark) "Dark" else "Light",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Spacer(Modifier.width(8.dp))
                    // Mini Switch Capsule
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2DD4BF))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(if (isDark) Alignment.CenterEnd else Alignment.CenterStart)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }

            // Settings Gear Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xCC1E293B) else Color(0xEEFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x33000000)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onOpenSettings() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCompactRuleRow(
    emoji: String,
    label: String,
    text: String,
    isDark: Boolean,
    highlight: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(emoji, fontSize = 18.sp, modifier = Modifier.width(30.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Color(0xFFF59E0B)
                        else if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
            )
            Text(
                text = text,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun HanoiNavPill(
    current: Screen,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onTutorialClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    val homeSelected = current == Screen.Home
    val playSelected = current == Screen.Play

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = if (isDark) Color(0xF2111C30) else Color(0xF8FFFFFF),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0x332DD4BF) else Color(0x1F000000)
        ),
        shadowElevation = 12.dp,
        modifier = modifier
            .width(320.dp)
            .height(54.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                selected = homeSelected,
                icon = Icons.Rounded.Home,
                label = "Home",
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = onHomeClick
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(18.dp)
                    .background(if (isDark) Color(0x22FFFFFF) else Color(0x18000000))
            )

            BottomNavItem(
                selected = false,
                icon = Icons.Rounded.AutoAwesome,
                label = "Tutorial",
                isDark = isDark,
                modifier = Modifier.weight(1.1f),
                onClick = onTutorialClick
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(18.dp)
                    .background(if (isDark) Color(0x22FFFFFF) else Color(0x18000000))
            )

            BottomNavItem(
                selected = playSelected,
                icon = Icons.Rounded.PlayArrow,
                label = "Play",
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = onPlayClick
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val activeBg = if (isDark) Color(0x332DD4BF) else Color(0x2614B8A6)
    val activeBorder = if (isDark) Color(0x552DD4BF) else Color(0x4014B8A6)
    val activeTint = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
    val inactiveTint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val animatedBg by animateColorAsState(
        targetValue = if (selected) activeBg else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "navBg"
    )
    val animatedTint by animateColorAsState(
        targetValue = if (selected) activeTint else inactiveTint,
        animationSpec = tween(durationMillis = 200),
        label = "navTint"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(animatedBg)
            .then(
                if (selected) Modifier.border(BorderStroke(1.dp, activeBorder), RoundedCornerShape(24.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = animatedTint,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SettingsDialog(
    state: AppUiState,
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf(state.playerName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Game Settings", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (state.audioEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Sound Effects", fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = state.audioEnabled,
                        onCheckedChange = { viewModel.toggleAudio() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Vibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Haptic Feedback", fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = state.hapticsEnabled,
                        onCheckedChange = { viewModel.toggleHaptics() }
                    )
                }

                Column {
                    Text(
                        "Auto-Solver Speed: ${state.solverSpeedMs} ms",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = state.solverSpeedMs.toFloat(),
                        onValueChange = { viewModel.setSolverSpeed(it) },
                        valueRange = 120f..1500f
                    )
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        viewModel.updatePlayerName(it)
                    },
                    label = { Text("Player Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

private fun getLevelThumbnailRes(levelId: Int, isDark: Boolean): Int {
    return when (levelId) {
        1 -> if (isDark) R.drawable.thumb_lvl_1_dark else R.drawable.thumb_lvl_1_light
        2 -> if (isDark) R.drawable.thumb_lvl_2_dark else R.drawable.thumb_lvl_2_light
        3 -> if (isDark) R.drawable.thumb_lvl_3_dark else R.drawable.thumb_lvl_3_light
        4 -> if (isDark) R.drawable.thumb_lvl_4_dark else R.drawable.thumb_lvl_4_light
        5 -> if (isDark) R.drawable.thumb_lvl_5_dark else R.drawable.thumb_lvl_5_light
        6 -> if (isDark) R.drawable.thumb_lvl_6_dark else R.drawable.thumb_lvl_6_light
        7 -> if (isDark) R.drawable.thumb_lvl_7_dark else R.drawable.thumb_lvl_7_light
        8 -> if (isDark) R.drawable.thumb_lvl_8_dark else R.drawable.thumb_lvl_8_light
        9 -> if (isDark) R.drawable.thumb_lvl_9_dark else R.drawable.thumb_lvl_9_light
        10 -> if (isDark) R.drawable.thumb_lvl_10_dark else R.drawable.thumb_lvl_10_light
        11 -> if (isDark) R.drawable.thumb_lvl_11_dark else R.drawable.thumb_lvl_11_light
        12 -> if (isDark) R.drawable.thumb_lvl_12_dark else R.drawable.thumb_lvl_12_light
        13 -> if (isDark) R.drawable.thumb_lvl_13_dark else R.drawable.thumb_lvl_13_light
        14 -> if (isDark) R.drawable.thumb_lvl_14_dark else R.drawable.thumb_lvl_14_light
        else -> if (isDark) R.drawable.thumb_lvl_1_dark else R.drawable.thumb_lvl_1_light
    }
}

private fun getLevelDisplayName(level: Level): String {
    return when (level.id) {
        1 -> "Warm Up"
        2 -> "Getting Started"
        3 -> "Classic 3-Peg"
        4 -> "Mastering Moves"
        5 -> "Grandmaster"
        6 -> "Frame-Stewart Easy"
        7 -> "Strategic 4-Peg"
        8 -> "The Quad Rush"
        9 -> "Four Pillars"
        10 -> "Strategic Master"
        11 -> "Dual Starter"
        12 -> "Bicolor Intermediate"
        13 -> "Dual Towers Pro"
        14 -> "Bicolor Master"
        else -> level.title
    }
}

private fun getLevelBadgeColor(levelId: Int): Color {
    return when (levelId) {
        1, 6, 11 -> Color(0xFF3B82F6) // Blue
        2, 7, 12 -> Color(0xFF10B981) // Mint
        3, 8, 13 -> Color(0xFFF97316) // Orange
        4, 9, 14 -> Color(0xFF8B5CF6) // Lilac / Purple
        5, 10 -> Color(0xFFEF4444) // Rose / Red
        else -> Color(0xFF3B82F6)
    }
}

@Composable
private fun PlayerNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Player Profile", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Player Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(nameInput) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LevelsHeader(
    playerName: String,
    onEditPlayerName: () -> Unit,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 3D Hanoi mini logo + Title "Hanoi" + Editable Player Name below it (no back button)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.hanoi_mini_3d),
                contentDescription = "Hanoi Logo",
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(10.dp))

            Column {
                Text(
                    text = "Hanoi",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    letterSpacing = 0.5.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onEditPlayerName() }
                        .padding(vertical = 1.dp)
                ) {
                    Text(
                        text = if (playerName.isNotBlank()) playerName else "Player",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2DD4BF)
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit Player Name",
                        tint = Color(0xFF2DD4BF),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Right: Theme Toggle Capsule + Settings Gear
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (isDark) Color(0xCC1E293B) else Color(0xEEFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x33000000)),
                shadowElevation = 4.dp,
                modifier = Modifier.clickable { onToggleTheme() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        contentDescription = if (isDark) "Dark Mode" else "Light Mode",
                        tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isDark) "Dark" else "Light",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2DD4BF))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(if (isDark) Alignment.CenterEnd else Alignment.CenterStart)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xCC1E293B) else Color(0xEEFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x33000000)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onOpenSettings() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelsHeroBanner(activeTab: Int, isDark: Boolean) {
    val sceneType = when (activeTab) {
        0 -> SceneType.Classic
        1 -> SceneType.Strategic
        else -> SceneType.DualTower
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(138.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isDark) Color.Black else Color(0x28000000),
                spotColor = if (isDark) Color.Black else Color(0x28000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .border(
                BorderStroke(
                    1.dp,
                    if (isDark) Color(0x3394A3B8) else Color(0x26000000)
                ),
                RoundedCornerShape(18.dp)
            )
    ) {
        DeskHeroScene3D(
            sceneType = sceneType,
            isDark = isDark,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PlayerProfileCard(
    state: AppUiState,
    activeTab: Int,
    isDark: Boolean,
    onEditName: () -> Unit
) {
    val cardBg = if (isDark) Color(0xF2131B2E) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x3338BDF8) else Color(0xFFE2E8F0)

    val stickerType = when (activeTab) {
        0 -> StickerType.Think
        1 -> if (isDark) StickerType.Logic else StickerType.Freedom
        else -> StickerType.Challenge
    }

    val totalEarned = state.game.levelStats.values.sumOf { it.stars }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isDark) Color.Black else Color(0x26000000),
                spotColor = if (isDark) Color.Black else Color(0x26000000)
            ),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Generated Avatar with small edit badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clickable { onEditName() }
            ) {
                if (activeTab == 0) {
                    AvatarRobot3D(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.TopStart)
                    )
                } else {
                    AvatarUser3D(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.TopStart)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF2DD4BF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✏️",
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Player details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEditName() }
            ) {
                Text(
                    text = "Player Name",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.playerName.isNotBlank()) state.playerName else "Player",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "✎",
                        fontSize = 12.sp,
                        color = Color(0xFF2DD4BF)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${state.game.completedLevels.size} of ${HanoiLevels.size} levels completed • ⭐ $totalEarned total stars",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                )
            }

            Spacer(Modifier.width(8.dp))

            // 3D Generated Sticker Badge on the right
            StickerBadge3D(
                type = stickerType,
                isDark = isDark,
                modifier = Modifier.size(54.dp)
            )
        }
    }
}

@Composable
private fun CategoryTabsPill(
    selectedTab: Int,
    isDark: Boolean,
    onSelectTab: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(30.dp),
        color = if (isDark) Color(0xE6131B2E) else Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, if (isDark) Color(0x3338BDF8) else Color(0xFFCBD5E1))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf("Classic", "4 Stands", "Dual Tower")
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isSelected) {
                                if (isDark) Color(0xFF2DD4BF) else Color(0xFF14B8A6)
                            } else Color.Transparent
                        )
                        .clickable { onSelectTab(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            Color(0xFF0F172A)
                        } else {
                            if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DualTowerQuoteCard(isDark: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF2DD4BF),
                spotColor = Color(0xFF2DD4BF)
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0x262DD4BF) else Color(0x1A14B8A6),
        border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💡", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "“Two towers. New strategies. A bigger test for a sharper mind.”",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Dual Tower mode tests your ability to plan ahead and swap two towers simultaneously!",
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
private fun LevelCard3D(
    level: Level,
    completed: Boolean,
    stars: Int,
    bestMoves: Int?,
    bestTimeSeconds: Long?,
    isDark: Boolean,
    floatOffsetY: Float,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xF2131B2E) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x3338BDF8) else Color(0xFFE2E8F0)
    val badgeColor = getLevelBadgeColor(level.id)
    val thumbRes = getLevelThumbnailRes(level.id, isDark)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (completed) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isDark) Color.Black else Color(0x26000000),
                spotColor = if (isDark) Color.Black else Color(0x26000000)
            ),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Badge, Title, Subtitle, Record
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Level ${level.id}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = getLevelDisplayName(level),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )

                val subtitle = if (level.mode == com.hanoi.binaryhanoi.domain.model.GameMode.Bicolor) {
                    "${level.pegCount} stands · ${level.diskCount} Red & ${level.diskCount} Blue"
                } else {
                    "${level.pegCount} stands · ${level.diskCount} disks"
                }
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )

                if (bestMoves != null || bestTimeSeconds != null) {
                    val movesStr = bestMoves?.let { "$it moves" } ?: ""
                    val timeStr = bestTimeSeconds?.let { " · ${it}s" } ?: ""
                    Text(
                        text = "Best: $movesStr$timeStr",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2DD4BF)
                    )
                }
            }

            // Center: 3D Board Miniature Thumbnail with subtle floating animation
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .graphicsLayer { translationY = floatOffsetY }
                    .size(width = 86.dp, height = 62.dp),
                contentAlignment = Alignment.Center
            ) {
                LevelMiniBoard3D(
                    level = level,
                    isDark = isDark,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Right Column: 3-Star Rating + Chevron
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..3) {
                        val active = i <= stars
                        Icon(
                            imageVector = if (active) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            tint = if (active) Color(0xFFFFC107) else (if (isDark) Color(0x44CBD5E1) else Color(0x33000000)),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayScreen(state: AppUiState, viewModel: GameViewModel, palette: HanoiPalette) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNameEditDialog by remember { mutableStateOf(false) }
    val isDark = state.themeMode == HanoiThemeMode.ClassicWood
    val selectedTab = state.activeCategoryTab

    val classicLevels = HanoiLevels.filter { it.mode == com.hanoi.binaryhanoi.domain.model.GameMode.Classic }
    val strategicLevels = HanoiLevels.filter { it.mode == com.hanoi.binaryhanoi.domain.model.GameMode.Strategic }
    val bicolorLevels = HanoiLevels.filter { it.mode == com.hanoi.binaryhanoi.domain.model.GameMode.Bicolor }
    val currentLevels = when (selectedTab) {
        0 -> classicLevels
        1 -> strategicLevels
        else -> bicolorLevels
    }

    val totalPossibleStars = currentLevels.size * 3
    val earnedStars = currentLevels.sumOf { state.game.levelStats[it.id]?.stars ?: 0 }

    val infiniteTransition = rememberInfiniteTransition(label = "Play3D")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thumbFloat"
    )

    Box(modifier = Modifier.fillMaxSize().background(palette.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.statusBarsPadding())
                LevelsHeader(
                    playerName = state.playerName,
                    onEditPlayerName = { showNameEditDialog = true },
                    isDark = isDark,
                    onToggleTheme = { viewModel.toggleTheme() },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            item {
                LevelsHeroBanner(activeTab = selectedTab, isDark = isDark)
            }

            item {
                PlayerProfileCard(
                    state = state,
                    activeTab = selectedTab,
                    isDark = isDark,
                    onEditName = { showNameEditDialog = true }
                )
            }

            item {
                CategoryTabsPill(
                    selectedTab = selectedTab,
                    isDark = isDark,
                    onSelectTab = { viewModel.setCategoryTab(it) }
                )
            }

            if (selectedTab == 2) {
                item {
                    DualTowerQuoteCard(isDark = isDark)
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "5 Classic Levels (3 Stands)"
                            1 -> "5 Strategic Levels (4 Stands)"
                            else -> "4 Dual Tower Levels (Bicolor)"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$earnedStars / $totalPossibleStars Stars",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                        )
                    }
                }
            }

            items(currentLevels) { level ->
                val stats = state.game.levelStats[level.id]
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LevelCard3D(
                        level = level,
                        completed = level.id in state.game.completedLevels,
                        stars = stats?.stars ?: 0,
                        bestMoves = stats?.bestMoves?.takeIf { it < Int.MAX_VALUE },
                        bestTimeSeconds = stats?.bestTimeSeconds?.takeIf { it < Long.MAX_VALUE },
                        isDark = isDark,
                        floatOffsetY = floatAnim,
                        onClick = { viewModel.startLevel(level.id) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showNameEditDialog) {
        PlayerNameDialog(
            currentName = state.playerName,
            onDismiss = { showNameEditDialog = false },
            onSave = {
                viewModel.updatePlayerName(it)
                showNameEditDialog = false
            }
        )
    }
}

@Composable
private fun TutorialScreen(state: AppUiState, viewModel: GameViewModel, palette: HanoiPalette) {
    val isDark = state.themeMode == HanoiThemeMode.ClassicWood
    val game = state.game

    var currentStepIndex by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    val moveProgress = remember { Animatable(0f) }

    // Sync step index if game history changes
    LaunchedEffect(game.moveHistory.size) {
        if (game.moveHistory.isNotEmpty() && !game.isComplete) {
            currentStepIndex = game.moveHistory.size.coerceAtMost(tutorialMoveSteps.size - 1)
        } else if (game.moveHistory.isEmpty()) {
            currentStepIndex = 0
        }
    }

    // 5-second per step animation with hand icon and synchronized texts
    LaunchedEffect(isPaused, currentStepIndex, game.isComplete) {
        if (!isPaused && currentStepIndex < tutorialMoveSteps.size && !game.isComplete) {
            moveProgress.snapTo(0f)
            moveProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
            )
            // Once 5000ms completes, execute move physically
            val step = tutorialMoveSteps[currentStepIndex]
            viewModel.makeMove(step.from, step.to)
            if (currentStepIndex + 1 < tutorialMoveSteps.size) {
                currentStepIndex++
            } else {
                viewModel.completeTutorial()
            }
        }
    }

    val currentStep = tutorialMoveSteps.getOrNull(currentStepIndex)
    val progress = moveProgress.value

    Box(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Top Bar (No Back Button) ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: 3D App Icon + Title & Step Count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.hanoi_mini_3d),
                        contentDescription = "Hanoi Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Interactive Tutorial",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = if (game.isComplete) "Complete! 🏆" else "Step ${(currentStepIndex + 1).coerceAtMost(7)} of 7",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
                        )
                    }
                }

                // Right: Step dots (7 steps)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 7) {
                        val isPast = i < currentStepIndex || game.isComplete
                        val isCurrent = i == currentStepIndex && !game.isComplete
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 10.dp else if (isPast) 7.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> Color(0xFF2DD4BF)
                                        isPast -> Color(0xFF10B981)
                                        isDark -> Color(0xFF334155)
                                        else -> Color(0xFFCBD5E1)
                                    }
                                )
                        )
                    }
                }
            }

            // ── Goal Card ────────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0x262DD4BF) else Color(0x1A14B8A6),
                border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎯", fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Goal: Move all disks from Stand 1 → Stand 3",
                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }

            // ── Glowing progress bar (7 moves) ────────────────────────────────
            val progressFraction by animateFloatAsState(
                targetValue = if (game.isComplete) 1f else ((currentStepIndex.toFloat() + progress) / 7f).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "tutorialProgressBar"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0EA5E9), Color(0xFF2DD4BF))
                            )
                        )
                )
            }

            // ── Rule & Step Action Text (Above graphics with dedicated padding) ──
            if (!game.isComplete && currentStep != null) {
                val isRule1 = currentStep.stepIndex in listOf(0, 1, 3, 4)
                val ruleShortText = if (isRule1) {
                    "Move only 1 disk at a time"
                } else {
                    "Never place larger disk on smaller disk"
                }

                val fromNum = currentStep.from.ordinal + 1
                val toNum = currentStep.to.ordinal + 1
                val actionText = when {
                    progress < 0.22f -> "Lift Disk ${currentStep.diskId} from Stand $fromNum"
                    progress < 0.78f -> "Stand $fromNum ➔ Stand $toNum"
                    else -> "Place on Stand $toNum"
                }

                val textShadow = Shadow(
                    color = if (isDark) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f),
                    offset = Offset(0f, 2f),
                    blurRadius = 6f
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isRule1) "RULE 1: " else "RULE 2: ",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isRule1) (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)) else (if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)),
                            style = LocalTextStyle.current.copy(shadow = textShadow)
                        )
                        Text(
                            text = ruleShortText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            style = LocalTextStyle.current.copy(shadow = textShadow)
                        )
                    }

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = actionText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                        style = LocalTextStyle.current.copy(shadow = textShadow)
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
            }

            // ── 3D Game Board (Vertically Centered with balanced spacing) ─────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val flightDisk = if (!game.isComplete && currentStep != null) {
                    game.pegs[currentStep.from]?.lastOrNull() ?: Disk(currentStep.diskId, currentStep.diskId)
                } else null

                GameBoard(
                    state = game.copy(
                        hint = currentStep?.let { Move(it.from, it.to, it.diskId) }
                    ),
                    palette = palette,
                    onPegTap = { /* Automated demonstration */ },
                    canvasHeight = 285.dp,
                    flightDisk = if (!game.isComplete) flightDisk else null,
                    flightFrom = if (!game.isComplete) currentStep?.from else null,
                    flightTo = if (!game.isComplete) currentStep?.to else null,
                    flightProgress = progress,
                    showHand = !game.isComplete
                )
            }

            // ── Playback Controls Bar ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Replay Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xCC1E293B) else Color(0xEEFFFFFF),
                    border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x33000000)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.startTutorialInteractive()
                            currentStepIndex = 0
                            isPaused = false
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Replay",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Replay",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                }

                // Pause / Resume Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xCC1E293B) else Color(0xEEFFFFFF),
                    border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x33000000)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isPaused = !isPaused }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = Color(0xFF2DD4BF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isPaused) "Resume" else "Pause",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                }

                // Direct "Play Game" Button
                Button(
                    onClick = { viewModel.startLevel(1) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2DD4BF),
                        contentColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Play Level 1", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // ── Central Pop-up when Tutorial is Complete ─────────────────────────
        if (game.isComplete) {
            Dialog(
                onDismissRequest = { /* Modal */ },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xF2131E33) else Color(0xFFFFFFFF),
                        border = BorderStroke(1.5.dp, Color(0xFF2DD4BF).copy(alpha = 0.6f)),
                        shadowElevation = 24.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("🎉", fontSize = 36.sp)
                            Text(
                                text = "Tutorial Complete!",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "You've mastered the 2 core rules of Hanoi. Time to challenge yourself!",
                                fontSize = 13.sp,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.startLevel(1) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2DD4BF),
                                    contentColor = Color(0xFF0F172A)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Play Level 1 ➔", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameScreen(state: AppUiState, viewModel: GameViewModel, palette: HanoiPalette) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    val isDark = state.themeMode == HanoiThemeMode.ClassicWood
    val game = state.game
    val targetStandNumber = game.level.pegCount
    val targetStandName = "Stand $targetStandNumber"

    val moves = game.moveCount
    val optimal = viewModel.optimalForCurrent()
    val stars = viewModel.calculateStars(moves, optimal)
    val minutes = game.elapsedSeconds / 60
    val seconds = game.elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Top Bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button + Level Title & Subtitle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xCC1E293B) else Color(0xEEFFFFFF),
                        border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x33000000)),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { viewModel.goBack() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isDark) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Level ${game.level.id}: ${getLevelDisplayName(game.level)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "Think • Move • Solve",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }

                // Right: Theme Toggle + Settings Gear
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = if (isDark) Color(0xCC1E293B) else Color(0xEEFFFFFF),
                        border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x33000000)),
                        shadowElevation = 4.dp,
                        modifier = Modifier.clickable { viewModel.toggleTheme() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                contentDescription = if (isDark) "Dark Mode" else "Light Mode",
                                tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isDark) "Dark" else "Light",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2DD4BF))
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(if (isDark) Alignment.CenterEnd else Alignment.CenterStart)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xCC1E293B) else Color(0xEEFFFFFF),
                        border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x33000000)),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { showSettingsDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = if (isDark) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ── Stats Header Row (Disks, Record, Time, Moves) ────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xF2131B2E) else Color(0xFFFFFFFF),
                border = BorderStroke(1.dp, if (isDark) Color(0x3338BDF8) else Color(0xFFE2E8F0)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val disksDescription = if (game.level.mode == com.hanoi.binaryhanoi.domain.model.GameMode.Bicolor) {
                            "${game.level.diskCount} Red + ${game.level.diskCount} Blue"
                        } else {
                            "${game.level.diskCount} Disks · ${game.level.pegCount} Stands"
                        }
                        Text(
                            text = disksDescription,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val currentLevelStats = game.levelStats[game.level.id]
                        if (currentLevelStats != null && currentLevelStats.bestMoves < Int.MAX_VALUE) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Record: ${currentLevelStats.bestMoves} moves · ${currentLevelStats.bestTimeSeconds}s",
                                color = Color(0xFF2DD4BF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "Time",
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    fontSize = 10.5.sp
                                )
                            }
                            Text(
                                timeFormatted,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Moves",
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                fontSize = 10.5.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${game.moveCount}",
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = " / $optimal",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Goal Banner ──────────────────────────────────────────────────
            val goalText = if (game.level.mode == com.hanoi.binaryhanoi.domain.model.GameMode.Bicolor) {
                "Goal: Swap towers! Move all Red disks to Stand 3, Blue to Stand 1."
            } else {
                val poleSuffix = if (targetStandNumber == 3) " (3rd Pole)" else if (targetStandNumber == 4) " (4th Pole)" else ""
                "Goal: Move all disks from Stand 1 to $targetStandName$poleSuffix"
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0x262DD4BF) else Color(0x1A14B8A6),
                border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎯", fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = goalText,
                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }

            // ── 3D Game Board (Fills remaining space, NO SCROLLING) ───────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .weight(1f)
            ) {
                GameBoard(state = game, palette = palette, onPegTap = viewModel::selectPeg)
            }
        }

        // ── Central Scorecard Pop-up when Level is Complete ──────────────────
        if (game.isComplete) {
            Dialog(
                onDismissRequest = { /* Modal pop-up */ },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xF2131E33) else Color(0xFFFFFFFF),
                        border = BorderStroke(1.5.dp, Color(0xFF2DD4BF).copy(alpha = 0.6f)),
                        shadowElevation = 24.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (moves <= optimal) "PERFECT SOLVE! 🏆" else "LEVEL COMPLETE! 🎉",
                                fontWeight = FontWeight.Black,
                                fontSize = 21.sp,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )

                            // 3 Stars Display
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..3) {
                                    val active = i <= stars
                                    Icon(
                                        imageVector = if (active) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                        contentDescription = null,
                                        tint = if (active) Color(0xFFFFC107) else Color(0x4494A3B8),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            // Stats Summary Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDark) Color(0x331E293B) else Color(0x10000000),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Moves", fontSize = 11.5.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                                        Spacer(Modifier.height(2.dp))
                                        Text("$moves / $optimal", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Time Taken", fontSize = 11.5.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                                        Spacer(Modifier.height(2.dp))
                                        Text(timeFormatted, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Rating", fontSize = 11.5.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            when (stars) {
                                                3 -> "3 / 3 ⭐"
                                                2 -> "2 / 3 ⭐"
                                                else -> "1 / 3 ⭐"
                                            },
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = Color(0xFFFFC107)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(2.dp))

                            // Action Buttons: Retry (only if not optimal) & Next Level (always)
                            if (moves > optimal) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = viewModel::reset,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Retry", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = viewModel::nextLevel,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2DD4BF),
                                            contentColor = Color(0xFF0F172A)
                                        ),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Text("Next Level ➔", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = viewModel::nextLevel,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2DD4BF),
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Next Level ➔", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
private fun InsightCard(title: String, body: String, palette: HanoiPalette) {
    Surface(shape = RoundedCornerShape(10.dp), color = palette.surface, tonalElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = palette.text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text(body, color = palette.muted, lineHeight = 20.sp, fontSize = 14.sp)
        }
    }
}


