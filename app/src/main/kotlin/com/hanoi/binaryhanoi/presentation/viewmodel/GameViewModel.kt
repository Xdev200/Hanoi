package com.hanoi.binaryhanoi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanoi.binaryhanoi.domain.model.GameState
import com.hanoi.binaryhanoi.domain.model.HanoiLevels
import com.hanoi.binaryhanoi.domain.model.Move
import com.hanoi.binaryhanoi.domain.model.PegId
import com.hanoi.binaryhanoi.domain.usecase.CalculateOptimalMovesUseCase
import com.hanoi.binaryhanoi.domain.usecase.SolveHanoiUseCase
import com.hanoi.binaryhanoi.domain.usecase.ValidateMoveUseCase
import com.hanoi.binaryhanoi.domain.usecase.initialPegs
import com.hanoi.binaryhanoi.presentation.theme.HanoiThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppUiState(
    val game: GameState,
    val screen: Screen = Screen.Home,
    val themeMode: HanoiThemeMode = HanoiThemeMode.ClassicWood,
    val playerName: String = "",
    val audioEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val solverSpeedMs: Long = 420L,
    val tutorialSeen: Boolean = false
)

enum class Screen { Home, Play, Game }

class GameViewModel : ViewModel() {
    private val validateMove = ValidateMoveUseCase()
    private val solveHanoi = SolveHanoiUseCase()
    private val optimalMoves = CalculateOptimalMovesUseCase()

    private val firstLevel = HanoiLevels.first()
    private val _uiState = MutableStateFlow(
        AppUiState(game = newGame(firstLevel.id, emptySet()), screen = Screen.Home)
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun show(screen: Screen) {
        _uiState.value = _uiState.value.copy(screen = screen)
    }

    fun startLevel(levelId: Int) {
        val completed = _uiState.value.game.completedLevels
        _uiState.value = _uiState.value.copy(game = newGame(levelId, completed), screen = Screen.Game)
    }

    fun startTutorialAuto() {
        startLevel(firstLevel.id)
        autoSolve()
    }

    fun updatePlayerName(name: String) {
        _uiState.value = _uiState.value.copy(playerName = name.take(24))
    }

    fun selectPeg(peg: PegId) {
        val state = _uiState.value.game
        val selected = state.selectedPeg
        if (selected == null) {
            if (state.pegs[peg].orEmpty().isNotEmpty()) {
                _uiState.value = _uiState.value.copy(game = state.copy(selectedPeg = peg, hint = null))
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
        val complete = pegs[state.activePegs.last()].orEmpty().size == state.level.diskCount
        val completed = if (complete) state.completedLevels + state.level.id else state.completedLevels
        _uiState.value = _uiState.value.copy(
            game = state.copy(
                pegs = pegs,
                selectedPeg = null,
                hint = null,
                moveCount = state.moveCount + 1,
                moveHistory = state.moveHistory + Move(from, to, moving.id, System.currentTimeMillis()),
                completedLevels = completed,
                isComplete = complete
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
        _uiState.value = _uiState.value.copy(
            game = state.copy(
                pegs = pegs,
                moveCount = (state.moveCount - 1).coerceAtLeast(0),
                moveHistory = state.moveHistory.dropLast(1),
                selectedPeg = null,
                isComplete = false
            )
        )
    }

    fun reset() {
        val current = _uiState.value.game
        _uiState.value = _uiState.value.copy(game = newGame(current.level.id, current.completedLevels))
    }

    fun hint() {
        val state = _uiState.value.game
        val solvedPath = solveHanoi(state.level.diskCount, state.level.pegCount)
        val move = solvedPath.getOrNull(state.moveHistory.size)
            ?.takeIf { candidate -> validateMove(candidate.from, candidate.to, state) }
            ?: state.activePegs.firstNotNullOfOrNull { from ->
                state.activePegs.firstOrNull { to -> validateMove(from, to, state) }?.let { Move(from, it, state.pegs[from].orEmpty().last().id) }
            }
        _uiState.value = _uiState.value.copy(game = state.copy(hint = move))
    }

    fun toggleRecursion() {
        val state = _uiState.value.game
        _uiState.value = _uiState.value.copy(game = state.copy(showRecursion = !state.showRecursion))
    }

    fun autoSolve() {
        val state = _uiState.value.game
        if (state.solverRunning) return
        viewModelScope.launch {
            val fresh = newGame(state.level.id, state.completedLevels).copy(solverRunning = true)
            _uiState.value = _uiState.value.copy(game = fresh)
            for (move in solveHanoi(fresh.level.diskCount, fresh.level.pegCount)) {
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

    fun setSolverSpeed(value: Float) {
        _uiState.value = _uiState.value.copy(solverSpeedMs = value.toLong().coerceIn(120L, 900L))
    }

    fun optimalForCurrent(): Int = with(_uiState.value.game.level) { optimalMoves(diskCount, pegCount) }

    private fun newGame(levelId: Int, completed: Set<Int>): GameState {
        val level = HanoiLevels.first { it.id == levelId }
        return GameState(level = level, pegs = initialPegs(level.diskCount, level.pegCount), completedLevels = completed)
    }
}
