package com.hanoi.binaryhanoi.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hanoi.binaryhanoi.domain.model.GameState
import com.hanoi.binaryhanoi.domain.model.HanoiLevels
import com.hanoi.binaryhanoi.domain.model.Move
import com.hanoi.binaryhanoi.domain.model.PegId
import com.hanoi.binaryhanoi.domain.usecase.CalculateOptimalMovesUseCase
import com.hanoi.binaryhanoi.domain.usecase.FindNextBestMoveUseCase
import com.hanoi.binaryhanoi.domain.usecase.SolveHanoiUseCase
import com.hanoi.binaryhanoi.domain.usecase.ValidateMoveUseCase
import com.hanoi.binaryhanoi.domain.usecase.initialPegs
import com.hanoi.binaryhanoi.presentation.theme.HanoiThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

enum class TutorialPhase { IDLE, HIGHLIGHT_SOURCE, HIGHLIGHT_DEST, MOVED }

data class AppUiState(
    val game: GameState,
    val screen: Screen = Screen.Home,
    val themeMode: HanoiThemeMode = HanoiThemeMode.ClassicWood,
    val playerName: String = "",
    val audioEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val solverSpeedMs: Long = 420L,
    val tutorialSeen: Boolean = false,
    val activeCategoryTab: Int = 0, // 0: Classic 3-Peg, 1: Strategic 4-Peg
    val tutorialPhase: TutorialPhase = TutorialPhase.IDLE
)

enum class Screen { Home, Play, Game }

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("hanoi_game_prefs", Context.MODE_PRIVATE)

    private val validateMove = ValidateMoveUseCase()
    private val solveHanoi = SolveHanoiUseCase()
    private val optimalMoves = CalculateOptimalMovesUseCase()
    private val findNextBestMove = FindNextBestMoveUseCase(validateMove)

    private val firstLevel = HanoiLevels.first()
    private val screenStack = ArrayDeque<Screen>()
    private var timerJob: Job? = null

    private val _uiState = MutableStateFlow(
        AppUiState(
            game = newGame(firstLevel.id, emptySet()),
            screen = Screen.Home,
            tutorialSeen = prefs.getBoolean("tutorial_seen", false),
            playerName = prefs.getString("player_name", "") ?: ""
        )
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _uiState.value.game
                if (_uiState.value.screen == Screen.Game && !current.isComplete && !current.isTutorialMode) {
                    _uiState.value = _uiState.value.copy(
                        game = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                    )
                }
            }
        }
    }

    fun setCategoryTab(tab: Int) {
        _uiState.value = _uiState.value.copy(activeCategoryTab = tab)
    }

    fun show(screen: Screen) {
        // One-way navigation from launch screen: never add Home to backstack
        if (_uiState.value.screen == Screen.Home) {
            screenStack.clear()
        } else if (_uiState.value.screen != screen) {
            screenStack.addLast(_uiState.value.screen)
        }
        _uiState.value = _uiState.value.copy(screen = screen)
    }

    fun startLevel(levelId: Int) {
        val completed = _uiState.value.game.completedLevels
        val stats = _uiState.value.game.levelStats
        val wasInTutorial = _uiState.value.game.isTutorialMode
        if (_uiState.value.screen == Screen.Home) {
            screenStack.clear()
        } else if (_uiState.value.screen != Screen.Game) {
            screenStack.addLast(_uiState.value.screen)
        }
        val isTutorialSeenNow = if (wasInTutorial) true else _uiState.value.tutorialSeen
        if (wasInTutorial) {
            prefs.edit().putBoolean("tutorial_seen", true).apply()
        }
        _uiState.value = _uiState.value.copy(
            game = newGame(levelId, completed, stats),
            screen = Screen.Game,
            tutorialSeen = isTutorialSeenNow,
            tutorialPhase = TutorialPhase.IDLE
        )
    }

    fun nextLevel() {
        val currentId = _uiState.value.game.level.id
        val nextLevel = HanoiLevels.firstOrNull { it.id == currentId + 1 }
        if (nextLevel != null) {
            startLevel(nextLevel.id)
        } else {
            show(Screen.Play)
        }
    }

    fun goBack(): Boolean {
        val currentScreen = _uiState.value.screen
        // If in Game screen (playing a level), go back to Levels screen (Screen.Play)
        if (currentScreen == Screen.Game) {
            _uiState.value = _uiState.value.copy(screen = Screen.Play)
            return true
        }
        // From Play screen or Home screen: one-way navigation, never return to Home
        return false
    }

    fun startTutorialAuto() {
        startTutorialInteractive()
    }

    fun startTutorialInteractive() {
        screenStack.clear()
        val completed = _uiState.value.game.completedLevels
        val tutorialGame = newGame(firstLevel.id, completed).copy(
            isTutorialMode = true,
            tutorialStepInfo = tutorialStepsList[0],
            hint = tutorialStepsList[0].recommendedMove
        )
        _uiState.value = _uiState.value.copy(
            game = tutorialGame,
            screen = Screen.Game
        )
    }

    fun completeTutorial() {
        prefs.edit().putBoolean("tutorial_seen", true).apply()
        _uiState.value = _uiState.value.copy(tutorialSeen = true)
    }

    fun nextTutorialStep() {
        val state = _uiState.value.game
        if (!state.isTutorialMode) return
        val currentStepIdx = state.moveHistory.size
        val stepInfo = tutorialStepsList.getOrNull(currentStepIdx)
        val move = stepInfo?.recommendedMove ?: return
        makeMove(move.from, move.to)
    }

    fun toggleTutorialAuto() {
        val state = _uiState.value.game
        if (state.solverRunning) {
            _uiState.value = _uiState.value.copy(game = state.copy(solverRunning = false))
        } else {
            autoPlayTutorial()
        }
    }

    fun autoPlayTutorial() {
        val state = _uiState.value.game
        if (state.solverRunning) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                game = _uiState.value.game.copy(solverRunning = true),
                tutorialPhase = TutorialPhase.IDLE
            )
            while (_uiState.value.game.isTutorialMode &&
                   !_uiState.value.game.isComplete &&
                   _uiState.value.game.solverRunning) {

                val currentStepIdx = _uiState.value.game.moveHistory.size
                val stepInfo = tutorialStepsList.getOrNull(currentStepIdx)
                val move = stepInfo?.recommendedMove ?: break

                // Phase 1 — Pick Up (700ms): source peg glows gold, top disk lifts via spring
                _uiState.value = _uiState.value.copy(
                    game = _uiState.value.game.copy(selectedPeg = move.from, hint = null),
                    tutorialPhase = TutorialPhase.HIGHLIGHT_SOURCE
                )
                delay(700L)
                if (!_uiState.value.game.solverRunning) break

                // Phase 2 — Drop Here (700ms): destination peg glows teal
                _uiState.value = _uiState.value.copy(
                    game = _uiState.value.game.copy(selectedPeg = null, hint = move),
                    tutorialPhase = TutorialPhase.HIGHLIGHT_DEST
                )
                delay(700L)
                if (!_uiState.value.game.solverRunning) break

                // Phase 3 — Execute (400ms): disk moves and settles on destination peg
                _uiState.value = _uiState.value.copy(tutorialPhase = TutorialPhase.MOVED)
                makeMove(move.from, move.to)
                delay(400L)
            }
            _uiState.value = _uiState.value.copy(
                game = _uiState.value.game.copy(solverRunning = false),
                tutorialPhase = TutorialPhase.IDLE
            )
        }
    }

    fun exitTutorial() {
        val state = _uiState.value.game
        _uiState.value = _uiState.value.copy(
            game = newGame(state.level.id, state.completedLevels),
            solverSpeedMs = 420L
        )
    }

    fun updatePlayerName(name: String) {
        val trimmed = name.take(24)
        prefs.edit().putString("player_name", trimmed).apply()
        _uiState.value = _uiState.value.copy(playerName = trimmed)
    }

    fun selectPeg(peg: PegId) {
        val state = _uiState.value.game
        val selected = state.selectedPeg
        if (selected == null) {
            if (state.pegs[peg].orEmpty().isNotEmpty()) {
                _uiState.value = _uiState.value.copy(game = state.copy(selectedPeg = peg))
            }
            return
        }
        if (selected == peg) {
            _uiState.value = _uiState.value.copy(game = state.copy(selectedPeg = null))
            return
        }
        makeMove(selected, peg)
    }

    fun makeMove(from: PegId, to: PegId) {
        val state = _uiState.value.game
        if (!validateMove(from, to, state)) {
            _uiState.value = _uiState.value.copy(game = state.copy(selectedPeg = null))
            return
        }
        val moving = state.pegs[from].orEmpty().last()
        val pegs = state.pegs.toMutableMap()
        pegs[from] = pegs[from].orEmpty().dropLast(1)
        pegs[to] = pegs[to].orEmpty() + moving
        
        val complete = if (state.level.mode == com.hanoi.binaryhanoi.domain.model.GameMode.Bicolor) {
            val pegA = pegs[PegId.A].orEmpty()
            val pegC = pegs[PegId.C].orEmpty()
            val othersEmpty = state.activePegs.filter { it != PegId.A && it != PegId.C }.all { pegs[it].orEmpty().isEmpty() }
            othersEmpty &&
                pegA.size == state.level.diskCount &&
                pegA.all { it.colorGroup == 1 } &&
                pegC.size == state.level.diskCount &&
                pegC.all { it.colorGroup == 0 }
        } else {
            pegs[state.activePegs.last()].orEmpty().size == state.level.diskCount
        }

        val completed = if (complete) state.completedLevels + state.level.id else state.completedLevels
        val newHistory = state.moveHistory + Move(from, to, moving.id, System.currentTimeMillis())
        val newMoveCount = state.moveCount + 1

        val newLevelStats = if (complete && !state.isTutorialMode) {
            val optimal = optimalForCurrent()
            val starsEarned = when {
                newMoveCount <= optimal -> 3
                newMoveCount <= (optimal * 1.3).toInt() + 1 -> 2
                else -> 1
            }
            val existing = state.levelStats[state.level.id] ?: com.hanoi.binaryhanoi.domain.model.LevelStats()
            val updated = existing.copy(
                stars = maxOf(existing.stars, starsEarned),
                bestMoves = minOf(existing.bestMoves, newMoveCount),
                bestTimeSeconds = minOf(existing.bestTimeSeconds, state.elapsedSeconds)
            )
            state.levelStats + (state.level.id to updated)
        } else {
            state.levelStats
        }
        
        val nextStepInfo = if (state.isTutorialMode) tutorialStepsList.getOrNull(newHistory.size) else null
        val nextHint = if (state.isTutorialMode) nextStepInfo?.recommendedMove else null

        _uiState.value = _uiState.value.copy(
            game = state.copy(
                pegs = pegs,
                selectedPeg = null,
                hint = nextHint,
                moveCount = newMoveCount,
                moveHistory = newHistory,
                completedLevels = completed,
                levelStats = newLevelStats,
                isComplete = complete,
                tutorialStepInfo = nextStepInfo
            )
        )
    }

    fun undo() {
        val state = _uiState.value.game
        val last = state.moveHistory.lastOrNull() ?: return
        val pegs = state.pegs.toMutableMap()
        val moving = pegs[last.to].orEmpty().lastOrNull() ?: return
        pegs[last.to] = pegs[last.to].orEmpty().dropLast(1)
        pegs[last.from] = pegs[last.from].orEmpty() + moving
        val newHistory = state.moveHistory.dropLast(1)
        val nextStepInfo = if (state.isTutorialMode) tutorialStepsList.getOrNull(newHistory.size) else null
        val nextHint = if (state.isTutorialMode) nextStepInfo?.recommendedMove else null

        _uiState.value = _uiState.value.copy(
            game = state.copy(
                pegs = pegs,
                moveCount = (state.moveCount - 1).coerceAtLeast(0),
                moveHistory = newHistory,
                selectedPeg = null,
                isComplete = false,
                tutorialStepInfo = nextStepInfo,
                hint = nextHint
            )
        )
    }

    fun reset() {
        val current = _uiState.value.game
        if (current.isTutorialMode) {
            startTutorialInteractive()
        } else {
            _uiState.value = _uiState.value.copy(game = newGame(current.level.id, current.completedLevels, current.levelStats))
        }
    }

    fun hint() {
        val state = _uiState.value.game
        if (state.isComplete) return
        val move = findNextBestMove(state)
        _uiState.value = _uiState.value.copy(game = state.copy(hint = move))
    }

    fun executeHint() {
        val hint = _uiState.value.game.hint ?: return
        makeMove(hint.from, hint.to)
    }

    fun toggleRecursion() {
        val state = _uiState.value.game
        _uiState.value = _uiState.value.copy(game = state.copy(showRecursion = !state.showRecursion))
    }

    fun autoSolve() {
        val state = _uiState.value.game
        if (state.solverRunning) return
        viewModelScope.launch {
            val fresh = newGame(state.level.id, state.completedLevels, state.levelStats).copy(solverRunning = true)
            _uiState.value = _uiState.value.copy(game = fresh)
            for (move in solveHanoi(fresh.level.diskCount, fresh.level.pegCount, fresh.level.mode)) {
                if (!_uiState.value.game.solverRunning) break
                makeMove(move.from, move.to)
                delay(_uiState.value.solverSpeedMs)
            }
            _uiState.value = _uiState.value.copy(game = _uiState.value.game.copy(solverRunning = false))
        }
    }

    fun toggleTheme() {
        val next = if (_uiState.value.themeMode == HanoiThemeMode.ClassicWood) HanoiThemeMode.Light else HanoiThemeMode.ClassicWood
        _uiState.value = _uiState.value.copy(themeMode = next)
    }

    fun toggleAudio() {
        _uiState.value = _uiState.value.copy(audioEnabled = !_uiState.value.audioEnabled)
    }

    fun toggleHaptics() {
        _uiState.value = _uiState.value.copy(hapticsEnabled = !_uiState.value.hapticsEnabled)
    }

    fun setSolverSpeed(value: Float) {
        _uiState.value = _uiState.value.copy(solverSpeedMs = value.toLong().coerceIn(120L, 2000L))
    }

    fun optimalForCurrent(): Int = with(_uiState.value.game.level) { optimalMoves(diskCount, pegCount, mode) }

    fun calculateStars(moves: Int, optimal: Int): Int = when {
        moves <= optimal -> 3
        moves <= (optimal * 1.3).toInt() + 1 -> 2
        else -> 1
    }

    private fun newGame(
        levelId: Int,
        completed: Set<Int>,
        stats: Map<Int, com.hanoi.binaryhanoi.domain.model.LevelStats> = emptyMap()
    ): GameState {
        val level = HanoiLevels.first { it.id == levelId }
        return GameState(
            level = level,
            pegs = initialPegs(level.diskCount, level.pegCount, level.mode),
            completedLevels = completed,
            levelStats = stats,
            startTimeMillis = System.currentTimeMillis(),
            elapsedSeconds = 0L
        )
    }
}

data class TutorialMoveStep(
    val stepIndex: Int, // 0..6
    val from: PegId,
    val to: PegId,
    val diskId: Int,
    val title: String,
    val description: String,
    val ruleHighlighted: String,
    val aimingText: String,
    val liftingText: String,
    val flyingText: String,
    val droppingText: String
)

val tutorialMoveSteps: List<TutorialMoveStep> = listOf(
    TutorialMoveStep(
        stepIndex = 0,
        from = PegId.A,
        to = PegId.C,
        diskId = 1,
        title = "Step 1 of 7: Start with Top Disk",
        description = "Move small Disk 1 from Stand 1 to Stand 3.",
        ruleHighlighted = "Rule 1: Always move only 1 top disk at a time!",
        aimingText = "Aiming for top Disk 1 on Stand 1...",
        liftingText = "🖐️ Lifting Disk 1 from Stand 1",
        flyingText = "🚀 Flying Disk 1 across to Stand 3",
        droppingText = "🫳 Dropping Disk 1 onto Stand 3"
    ),
    TutorialMoveStep(
        stepIndex = 1,
        from = PegId.A,
        to = PegId.B,
        diskId = 2,
        title = "Step 2 of 7: Clear Stand 1",
        description = "Move medium Disk 2 from Stand 1 to Stand 2 (spare stand).",
        ruleHighlighted = "Rule 2: Cannot place larger Disk 2 on smaller Disk 1, so park on Stand 2!",
        aimingText = "Aiming for Disk 2 on Stand 1...",
        liftingText = "🖐️ Lifting Disk 2 from Stand 1",
        flyingText = "🚀 Flying Disk 2 across to Stand 2",
        droppingText = "🫳 Dropping Disk 2 onto Stand 2"
    ),
    TutorialMoveStep(
        stepIndex = 2,
        from = PegId.C,
        to = PegId.B,
        diskId = 1,
        title = "Step 3 of 7: Stack Small on Medium",
        description = "Move Disk 1 from Stand 3 onto Disk 2 at Stand 2.",
        ruleHighlighted = "Rule 2: Smaller Disk 1 safely sits on top of larger Disk 2!",
        aimingText = "Aiming for Disk 1 on Stand 3...",
        liftingText = "🖐️ Picking up Disk 1 from Stand 3",
        flyingText = "🚀 Flying Disk 1 across to Stand 2",
        droppingText = "🫳 Stacking Disk 1 safely onto Disk 2"
    ),
    TutorialMoveStep(
        stepIndex = 3,
        from = PegId.A,
        to = PegId.C,
        diskId = 3,
        title = "Step 4 of 7: Move Giant Base Disk!",
        description = "Stand 3 is clear! Move giant Base Disk 3 directly to Stand 3.",
        ruleHighlighted = "Target Goal: Base disk reaches its final home on Stand 3!",
        aimingText = "Aiming for giant Base Disk 3 on Stand 1...",
        liftingText = "🖐️ Lifting heavy Base Disk 3",
        flyingText = "🚀 Flying Base Disk 3 directly to Stand 3",
        droppingText = "🫳 Placing Base Disk 3 onto Stand 3"
    ),
    TutorialMoveStep(
        stepIndex = 4,
        from = PegId.B,
        to = PegId.A,
        diskId = 1,
        title = "Step 5 of 7: Free up Stand 2",
        description = "Move Disk 1 from Stand 2 back to Stand 1 to unblock Disk 2.",
        ruleHighlighted = "Strategy: Move Disk 1 out of the way so Disk 2 is free to move!",
        aimingText = "Aiming for Disk 1 on Stand 2...",
        liftingText = "🖐️ Picking up Disk 1 from Stand 2",
        flyingText = "🚀 Flying Disk 1 back to Stand 1",
        droppingText = "🫳 Parking Disk 1 on Stand 1"
    ),
    TutorialMoveStep(
        stepIndex = 5,
        from = PegId.B,
        to = PegId.C,
        diskId = 2,
        title = "Step 6 of 7: Build the Tower",
        description = "Move Disk 2 from Stand 2 onto Disk 3 at Stand 3.",
        ruleHighlighted = "Rule 2: Medium Disk 2 rests on larger Disk 3 at Stand 3!",
        aimingText = "Aiming for Disk 2 on Stand 2...",
        liftingText = "🖐️ Lifting Disk 2 from Stand 2",
        flyingText = "🚀 Flying Disk 2 across to Stand 3",
        droppingText = "🫳 Stacking Disk 2 onto Base Disk 3"
    ),
    TutorialMoveStep(
        stepIndex = 6,
        from = PegId.A,
        to = PegId.C,
        diskId = 1,
        title = "Step 7 of 7: Crown the Tower! 🏆",
        description = "Move Disk 1 from Stand 1 to the top of Stand 3 to solve!",
        ruleHighlighted = "Optimal Solution: Tower rebuilt in exactly 7 optimal moves! 🎉",
        aimingText = "Aiming for final Disk 1 on Stand 1...",
        liftingText = "🖐️ Lifting final Disk 1 from Stand 1",
        flyingText = "🚀 Flying Disk 1 across to Stand 3",
        droppingText = "👑 Crowning the Tower on Stand 3!"
    )
)

val tutorialStepsList: List<com.hanoi.binaryhanoi.domain.model.TutorialStepInfo> = tutorialMoveSteps.map { step ->
    com.hanoi.binaryhanoi.domain.model.TutorialStepInfo(
        stepIndex = step.stepIndex,
        totalSteps = 7,
        title = step.title,
        description = step.description,
        ruleHighlighted = step.ruleHighlighted,
        recommendedMove = Move(step.from, step.to, step.diskId)
    )
}

