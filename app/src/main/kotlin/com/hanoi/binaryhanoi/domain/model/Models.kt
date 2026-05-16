package com.hanoi.binaryhanoi.domain.model

enum class PegId { A, B, C, D }

data class Disk(
    val id: Int,
    val size: Int = id
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
    val title: String
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
    val isComplete: Boolean = false
) {
    val activePegs: List<PegId> = PegId.entries.take(level.pegCount)
}

val HanoiLevels: List<Level> = buildList {
    var id = 1
    for (disks in 3..8) add(Level(id++, disks, 3, "Classic ${disks}"))
    for (disks in 3..6) add(Level(id++, disks, 4, "Frame-Stewart ${disks}"))
    add(Level(id++, 7, 4, "Four Peg Master"))
    add(Level(id, 8, 4, "Recursive Summit"))
}
