package com.hanoi.binaryhanoi.domain.usecase

import com.hanoi.binaryhanoi.domain.model.GameState
import com.hanoi.binaryhanoi.domain.model.Level
import com.hanoi.binaryhanoi.domain.model.PegId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HanoiUseCasesTest {
    private val validateMove = ValidateMoveUseCase()
    private val solveHanoi = SolveHanoiUseCase()
    private val optimalMoves = CalculateOptimalMovesUseCase()

    @Test
    fun validMove_allowsSmallDiskOntoEmptyPeg() {
        val state = stateFor(disks = 3, pegs = 3)

        assertTrue(validateMove(PegId.A, PegId.B, state))
    }

    @Test
    fun validMove_rejectsLargerDiskOntoSmallerDisk() {
        val firstMove = stateFor(disks = 3, pegs = 3).let { state ->
            val pegs = state.pegs.toMutableMap()
            pegs[PegId.A] = pegs[PegId.A].orEmpty().dropLast(1)
            pegs[PegId.B] = listOf(requireNotNull(state.pegs[PegId.A]?.last()))
            state.copy(pegs = pegs)
        }

        assertFalse(validateMove(PegId.A, PegId.B, firstMove))
    }

    @Test
    fun solver_returnsOptimalThreePegCounts() {
        assertEquals(7, solveHanoi(3, 3).size)
        assertEquals(31, optimalMoves(5, 3))
    }

    @Test
    fun solver_returnsFrameStewartFourPegCountForSixDisks() {
        assertEquals(17, solveHanoi(6, 4).size)
        assertEquals(17, optimalMoves(6, 4))
    }

    private fun stateFor(disks: Int, pegs: Int): GameState {
        val level = Level(id = 1, diskCount = disks, pegCount = pegs, title = "Test")
        return GameState(level = level, pegs = initialPegs(disks, pegs))
    }
}
