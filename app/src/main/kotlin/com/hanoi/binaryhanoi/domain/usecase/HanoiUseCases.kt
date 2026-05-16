package com.hanoi.binaryhanoi.domain.usecase

import com.hanoi.binaryhanoi.domain.model.Disk
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
        return targetTop == null || movingDisk.size < targetTop.size
    }
}

class SolveHanoiUseCase {
    operator fun invoke(diskCount: Int, pegCount: Int): List<Move> {
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
}

class CalculateOptimalMovesUseCase {
    operator fun invoke(diskCount: Int, pegCount: Int): Int {
        if (pegCount == 3) return 2.0.pow(diskCount).roundToInt() - 1
        return SolveHanoiUseCase().invoke(diskCount, pegCount).size
    }
}

fun initialPegs(diskCount: Int, pegCount: Int): Map<PegId, List<Disk>> {
    val active = PegId.entries.take(pegCount)
    return active.associateWith { peg ->
        if (peg == PegId.A) (diskCount downTo 1).map { Disk(it) } else emptyList()
    }
}
