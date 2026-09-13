package com.hanoi.binaryhanoi.domain.usecase

import com.hanoi.binaryhanoi.domain.model.Disk
import com.hanoi.binaryhanoi.domain.model.GameMode
import com.hanoi.binaryhanoi.domain.model.GameState
import com.hanoi.binaryhanoi.domain.model.Move
import com.hanoi.binaryhanoi.domain.model.PegId
import kotlin.math.pow
import kotlin.math.roundToInt

class ValidateMoveUseCase {
    operator fun invoke(from: PegId, to: PegId, state: GameState): Boolean {
        if (from == to || from !in state.activePegs || to !in state.activePegs) return false
        val movingDisk = state.pegs[from]?.lastOrNull() ?: return false
        val targetTop = state.pegs[to]?.lastOrNull()
        return if (state.level.mode == GameMode.Bicolor) {
            targetTop == null || movingDisk.size <= targetTop.size
        } else {
            targetTop == null || movingDisk.size < targetTop.size
        }
    }
}

class SolveHanoiUseCase {
    operator fun invoke(diskCount: Int, pegCount: Int, mode: GameMode = GameMode.Classic): List<Move> {
        if (mode == GameMode.Bicolor) {
            return solveBicolor(diskCount, pegCount)
        }
        return if (pegCount == 4) solveFourPeg(diskCount, PegId.A, PegId.D, PegId.B, PegId.C)
        else solveThreePeg(diskCount, PegId.A, PegId.C, PegId.B)
    }

    private fun solveThreePeg(n: Int, from: PegId, to: PegId, aux: PegId): List<Move> {
        val moves = mutableListOf<Move>()
        fun solve(count: Int, source: PegId, dest: PegId, spare: PegId) {
            if (count <= 0) return
            solve(count - 1, source, spare, dest)
            moves += Move(source, dest, count)
            solve(count - 1, spare, dest, source)
        }
        solve(n, from, to, aux)
        return moves
    }

    private fun solveFourPeg(n: Int, from: PegId, to: PegId, aux1: PegId, aux2: PegId): List<Move> {
        if (n <= 0) return emptyList()
        if (n == 1) return listOf(Move(from, to, 1))
        val k = bestSplit(n)
        return solveFourPeg(k, from, aux1, aux2, to) +
            solveThreePeg(n - k, from, to, aux2) +
            solveFourPeg(k, aux1, to, from, aux2)
    }

    private fun bestSplit(n: Int): Int {
        if (n <= 2) return n - 1
        var bestK = 1
        var bestMoves = Int.MAX_VALUE
        for (k in 1 until n) {
            val candidate = 2 * frameStewart(k) + ((1 shl (n - k)) - 1)
            if (candidate < bestMoves) {
                bestMoves = candidate
                bestK = k
            }
        }
        return bestK
    }

    private fun frameStewart(n: Int): Int {
        if (n <= 1) return n
        var best = Int.MAX_VALUE
        for (k in 1 until n) {
            best = minOf(best, 2 * frameStewart(k) + ((1 shl (n - k)) - 1))
        }
        return best
    }

    private fun solveBicolor(n: Int, pegCount: Int): List<Move> {
        val activePegs = PegId.entries.take(pegCount)
        val initialPegs: Map<PegId, List<Disk>> = activePegs.associateWith { peg ->
            when (peg) {
                PegId.A -> (n downTo 1).map { Disk(id = it, size = it, colorGroup = 0) }
                PegId.C -> (n downTo 1).map { Disk(id = n + it, size = it, colorGroup = 1) }
                else -> emptyList()
            }
        }

        fun isGoal(pegs: Map<PegId, List<Disk>>): Boolean {
            val pegA = pegs[PegId.A].orEmpty()
            val pegC = pegs[PegId.C].orEmpty()
            val othersEmpty = activePegs.filter { it != PegId.A && it != PegId.C }.all { pegs[it].orEmpty().isEmpty() }
            return othersEmpty &&
                pegA.size == n && pegA.all { it.colorGroup == 1 } &&
                pegC.size == n && pegC.all { it.colorGroup == 0 }
        }

        fun encode(pegs: Map<PegId, List<Disk>>): String {
            return activePegs.joinToString("|") { p -> pegs[p].orEmpty().joinToString(",") { it.id.toString() } }
        }

        data class SearchNode(val pegs: Map<PegId, List<Disk>>, val moves: List<Move>)
        val queue = ArrayDeque<SearchNode>()
        queue.add(SearchNode(initialPegs, emptyList()))
        val visited = mutableSetOf<String>()
        visited.add(encode(initialPegs))

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (isGoal(current.pegs)) {
                return current.moves
            }

            for (from in activePegs) {
                val fromList = current.pegs[from].orEmpty()
                if (fromList.isEmpty()) continue
                val moving = fromList.last()
                for (to in activePegs) {
                    if (from == to) continue
                    val toList = current.pegs[to].orEmpty()
                    val targetTop = toList.lastOrNull()
                    if (targetTop == null || moving.size <= targetTop.size) {
                        val nextPegs = current.pegs.toMutableMap()
                        nextPegs[from] = fromList.dropLast(1)
                        nextPegs[to] = toList + moving
                        val key = encode(nextPegs)
                        if (visited.add(key)) {
                            queue.add(SearchNode(nextPegs, current.moves + Move(from, to, moving.id)))
                        }
                    }
                }
            }
        }
        return emptyList()
    }
}

class CalculateOptimalMovesUseCase {
    operator fun invoke(diskCount: Int, pegCount: Int, mode: GameMode = GameMode.Classic): Int {
        if (mode == GameMode.Bicolor) {
            return when {
                diskCount == 2 && pegCount == 3 -> 9
                diskCount == 3 && pegCount == 4 -> 13
                diskCount == 4 && pegCount == 4 -> 21
                diskCount == 3 && pegCount == 3 -> 25
                else -> SolveHanoiUseCase().invoke(diskCount, pegCount, mode).size
            }
        }
        if (pegCount == 3) return 2.0.pow(diskCount).roundToInt() - 1
        return SolveHanoiUseCase().invoke(diskCount, pegCount, mode).size
    }
}

fun initialPegs(diskCount: Int, pegCount: Int, mode: GameMode = GameMode.Classic): Map<PegId, List<Disk>> {
    val active = PegId.entries.take(pegCount)
    if (mode == GameMode.Bicolor) {
        return active.associateWith { peg ->
            when (peg) {
                PegId.A -> (diskCount downTo 1).map { Disk(id = it, size = it, colorGroup = 0) }
                PegId.C -> (diskCount downTo 1).map { Disk(id = diskCount + it, size = it, colorGroup = 1) }
                else -> emptyList()
            }
        }
    }
    return active.associateWith { peg ->
        if (peg == PegId.A) (diskCount downTo 1).map { Disk(it) } else emptyList()
    }
}

class FindNextBestMoveUseCase(
    private val validateMove: ValidateMoveUseCase = ValidateMoveUseCase()
) {
    operator fun invoke(state: GameState): Move? {
        if (state.isComplete) return null

        // 1. If Tutorial mode, follow step recommendation
        if (state.isTutorialMode) {
            val step = state.tutorialStepInfo?.recommendedMove
            if (step != null && validateMove(step.from, step.to, state)) {
                return step
            }
        }

        val mode = state.level.mode
        val pegCount = state.level.pegCount
        val diskCount = state.level.diskCount
        val targetPeg = state.targetPeg

        // 2. Classic 3-Peg Solver from ANY arbitrary state (O(N) mathematical solution)
        if (mode == GameMode.Classic && pegCount == 3) {
            val move = solveThreePegFromState(state.pegs, diskCount, targetPeg)
            if (move != null && validateMove(move.from, move.to, state)) {
                return move
            }
        }

        // 3. Multi-Peg (4 Stands) & Bicolor (Dual Tower) General Shortest-Path Search (BFS)
        return solveGeneralBfsFromState(state)
    }

    private fun solveThreePegFromState(pegs: Map<PegId, List<Disk>>, n: Int, target: PegId): Move? {
        fun nextStep(m: Int, dest: PegId): Move? {
            if (m <= 0) return null
            // Locate peg containing disk with size m
            val current = pegs.entries.firstOrNull { (_, list) -> list.any { it.size == m } }?.key ?: return null
            if (current == dest) {
                // Disk m is already in place; check smaller disks
                return nextStep(m - 1, dest)
            }
            // Disk m needs to move from current to dest.
            // All disks 1 until m must first be cleared to the spare peg.
            val spare = PegId.entries.take(3).first { it != current && it != dest }
            val allSmallerOnSpare = (1 until m).all { size ->
                pegs[spare]?.any { it.size == size } == true
            }
            if (allSmallerOnSpare) {
                val disk = pegs[current]?.lastOrNull { it.size == m }
                if (disk != null) {
                    return Move(current, dest, disk.id)
                }
            }
            return nextStep(m - 1, spare)
        }
        return nextStep(n, target)
    }

    private fun solveGeneralBfsFromState(state: GameState): Move? {
        val activePegs = state.activePegs
        val isBicolor = state.level.mode == GameMode.Bicolor
        val targetPeg = state.targetPeg
        val diskCount = state.level.diskCount

        fun isGoal(pegs: Map<PegId, List<Disk>>): Boolean {
            if (isBicolor) {
                val pegA = pegs[PegId.A].orEmpty()
                val pegC = pegs[PegId.C].orEmpty()
                val othersEmpty = activePegs.filter { it != PegId.A && it != PegId.C }.all { pegs[it].orEmpty().isEmpty() }
                return othersEmpty &&
                    pegA.size == diskCount && pegA.all { it.colorGroup == 1 } &&
                    pegC.size == diskCount && pegC.all { it.colorGroup == 0 }
            } else {
                val targetList = pegs[targetPeg].orEmpty()
                return targetList.size == diskCount && activePegs.filter { it != targetPeg }.all { pegs[it].orEmpty().isEmpty() }
            }
        }

        fun encode(pegs: Map<PegId, List<Disk>>): String {
            return activePegs.joinToString("|") { p -> pegs[p].orEmpty().joinToString(",") { "${it.size}:${it.colorGroup}" } }
        }

        data class Node(val pegs: Map<PegId, List<Disk>>, val firstMove: Move?)
        val queue = ArrayDeque<Node>()
        val visited = HashSet<String>(4096)

        queue.add(Node(state.pegs, null))
        visited.add(encode(state.pegs))

        var iterations = 0
        val maxIterations = 35000

        while (queue.isNotEmpty() && iterations < maxIterations) {
            iterations++
            val current = queue.removeFirst()
            if (current.firstMove != null && isGoal(current.pegs)) {
                return current.firstMove
            }

            for (from in activePegs) {
                val fromList = current.pegs[from].orEmpty()
                if (fromList.isEmpty()) continue
                val moving = fromList.last()

                for (to in activePegs) {
                    if (from == to) continue
                    val toList = current.pegs[to].orEmpty()
                    val targetTop = toList.lastOrNull()

                    val isValid = if (isBicolor) {
                        targetTop == null || moving.size <= targetTop.size
                    } else {
                        targetTop == null || moving.size < targetTop.size
                    }

                    if (isValid) {
                        val nextPegs = current.pegs.toMutableMap()
                        nextPegs[from] = fromList.dropLast(1)
                        nextPegs[to] = toList + moving
                        val key = encode(nextPegs)
                        if (visited.add(key)) {
                            val thisMove = Move(from, to, moving.id)
                            val fMove = current.firstMove ?: thisMove
                            if (isGoal(nextPegs)) {
                                return fMove
                            }
                            queue.add(Node(nextPegs, fMove))
                        }
                    }
                }
            }
        }

        // Fallback: return any legal move that avoids immediately reversing the last move
        val lastMove = state.moveHistory.lastOrNull()
        return activePegs.firstNotNullOfOrNull { from ->
            activePegs.firstOrNull { to ->
                from != to && (lastMove == null || !(lastMove.from == to && lastMove.to == from)) &&
                    validateMove(from, to, state)
            }?.let { to -> Move(from, to, state.pegs[from].orEmpty().last().id) }
        } ?: activePegs.firstNotNullOfOrNull { from ->
            activePegs.firstOrNull { to -> validateMove(from, to, state) }
                ?.let { to -> Move(from, to, state.pegs[from].orEmpty().last().id) }
        }
    }
}


