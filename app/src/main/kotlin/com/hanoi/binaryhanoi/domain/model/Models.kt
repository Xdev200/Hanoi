package com.hanoi.binaryhanoi.domain.model

enum class PegId { A, B, C, D }

enum class GameMode { Classic, Strategic, Bicolor }

data class Disk(
    val id: Int,
    val size: Int = id,
    val colorGroup: Int = 0 // 0 = Standard / Red, 1 = Blue
)

data class Move(
    val from: PegId,
    val to: PegId,
    val diskId: Int,
    val timestamp: Long = 0L
)

data class Level(
    val id: Int,
    val diskCount: Int,
    val pegCount: Int,
    val title: String,
    val mode: GameMode = GameMode.Classic
)

data class LevelStats(
    val stars: Int = 0,
    val bestMoves: Int = Int.MAX_VALUE,
    val bestTimeSeconds: Long = Long.MAX_VALUE
)

data class TutorialStepInfo(
    val stepIndex: Int,
    val totalSteps: Int,
    val title: String,
    val description: String,
    val ruleHighlighted: String,
    val recommendedMove: Move? = null
)

data class GameState(
    val level: Level,
    val pegs: Map<PegId, List<Disk>>,
    val moveCount: Int = 0,
    val moveHistory: List<Move> = emptyList(),
    val selectedPeg: PegId? = null,
    val hint: Move? = null,
    val solverRunning: Boolean = false,
    val showRecursion: Boolean = false,
    val completedLevels: Set<Int> = emptySet(),
    val levelStats: Map<Int, LevelStats> = emptyMap(),
    val isComplete: Boolean = false,
    val isTutorialMode: Boolean = false,
    val tutorialStepInfo: TutorialStepInfo? = null,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val elapsedSeconds: Long = 0L
) {
    val activePegs: List<PegId> = PegId.entries.take(level.pegCount)
    val targetPeg: PegId get() = activePegs.last()
}

val HanoiLevels: List<Level> = listOf(
    // Classic 3-Stand Mode (Disks 3 to 7: 7, 15, 31, 63, 127 moves)
    Level(1, 3, 3, "Classic 3", GameMode.Classic),
    Level(2, 4, 3, "Classic 4", GameMode.Classic),
    Level(3, 5, 3, "Classic 5", GameMode.Classic),
    Level(4, 6, 3, "Classic 6", GameMode.Classic),
    Level(5, 7, 3, "Classic 7", GameMode.Classic),
    // Advanced 4-Stand Mode (Disks 4 to 8: 9, 13, 17, 25, 33 moves)
    Level(6, 4, 4, "Strategic 4", GameMode.Strategic),
    Level(7, 5, 4, "Strategic 5", GameMode.Strategic),
    Level(8, 6, 4, "Strategic 6", GameMode.Strategic),
    Level(9, 7, 4, "Master 7", GameMode.Strategic),
    Level(10, 8, 4, "Grandmaster 8", GameMode.Strategic),
    // Bicolor / Dual Tower Mode (Swap Red & Blue towers: 9, 13, 21, 25 moves)
    Level(11, 2, 3, "Dual Duo", GameMode.Bicolor),
    Level(12, 3, 4, "Dual Strategic", GameMode.Bicolor),
    Level(13, 4, 4, "Dual Crossroads", GameMode.Bicolor),
    Level(14, 3, 3, "Dual Master", GameMode.Bicolor)
)

