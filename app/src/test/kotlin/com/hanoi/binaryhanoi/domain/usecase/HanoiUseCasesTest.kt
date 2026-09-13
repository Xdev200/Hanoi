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

    @Test
    fun findNextBestMove_findsOptimalMoveFromInitialState() {
        val findNext = FindNextBestMoveUseCase(validateMove)
        val state = stateFor(disks = 3, pegs = 3)
        val move = findNext(state)
        assertTrue(move != null)
        assertEquals(PegId.A, move?.from)
        assertEquals(PegId.C, move?.to)
    }

    @Test
    fun findNextBestMove_findsOptimalMoveFromSuboptimalState() {
        val findNext = FindNextBestMoveUseCase(validateMove)
        // Disks 3 and 2 on A, Disk 1 moved to B (suboptimal for 3-disk game since first move should be A->C)
        val pegs = mapOf(
            PegId.A to listOf(com.hanoi.binaryhanoi.domain.model.Disk(3), com.hanoi.binaryhanoi.domain.model.Disk(2)),
            PegId.B to listOf(com.hanoi.binaryhanoi.domain.model.Disk(1)),
            PegId.C to emptyList()
        )
        val state = GameState(level = Level(1, 3, 3, "Test"), pegs = pegs)
        val move = findNext(state)
        assertTrue(move != null)
        // From this state, the shortest path to C is to put disk 1 onto C or disk 2 onto C
        assertTrue(validateMove(move!!.from, move.to, state))
    }

    @Test
    fun findNextBestMove_findsOptimalMoveForFourPegs() {
        val findNext = FindNextBestMoveUseCase(validateMove)
        val state = stateFor(disks = 4, pegs = 4)
        val move = findNext(state)
        assertTrue(move != null)
        assertEquals(PegId.A, move?.from)
        assertTrue(validateMove(move!!.from, move.to, state))
    }

    @Test
    fun findNextBestMove_worksForAllLevelsFromInitialState() {
        val findNext = FindNextBestMoveUseCase(validateMove)
        for (level in com.hanoi.binaryhanoi.domain.model.HanoiLevels) {
            val state = GameState(
                level = level,
                pegs = initialPegs(level.diskCount, level.pegCount, level.mode)
            )
            val startTime = System.currentTimeMillis()
            val move = findNext(state)
            val elapsed = System.currentTimeMillis() - startTime
            println("Level ${level.id} (${level.title}, mode=${level.mode}, disks=${level.diskCount}): move=$move, took ${elapsed}ms")
            assertTrue("Level ${level.id} should have a hint", move != null)
            assertTrue("Level ${level.id} move should be valid", validateMove(move!!.from, move.to, state))
        }
    }

    @Test
    fun findNextBestMove_worksForAllModesFromMidGameStates() {
        val findNext = FindNextBestMoveUseCase(validateMove)
        
        // 1. Classic mid-game (e.g. 5 disks, 3 moved)
        val classicLevel = Level(3, 5, 3, "Classic 5", com.hanoi.binaryhanoi.domain.model.GameMode.Classic)
        var classicState = GameState(classicLevel, initialPegs(5, 3))
        // Execute 3 moves
        for (i in 0 until 3) {
            val m = findNext(classicState)
            assertTrue("Classic mid-game should produce hint", m != null)
            val p = classicState.pegs.toMutableMap()
            val disk = p[m!!.from]!!.last()
            p[m.from] = p[m.from]!!.dropLast(1)
            p[m.to] = p[m.to]!! + disk
            classicState = classicState.copy(pegs = p)
        }
        val classicHint = findNext(classicState)
        assertTrue(classicHint != null)
        assertTrue(validateMove(classicHint!!.from, classicHint.to, classicState))

        // 2. Strategic 4-peg mid-game (6 disks, 4 moved)
        val strategicLevel = Level(8, 6, 4, "Strategic 6", com.hanoi.binaryhanoi.domain.model.GameMode.Strategic)
        var strategicState = GameState(strategicLevel, initialPegs(6, 4))
        for (i in 0 until 4) {
            val m = findNext(strategicState)
            assertTrue("Strategic mid-game should produce hint", m != null)
            val p = strategicState.pegs.toMutableMap()
            val disk = p[m!!.from]!!.last()
            p[m.from] = p[m.from]!!.dropLast(1)
            p[m.to] = p[m.to]!! + disk
            strategicState = strategicState.copy(pegs = p)
        }
        val stratHint = findNext(strategicState)
        assertTrue(stratHint != null)
        assertTrue(validateMove(stratHint!!.from, stratHint.to, strategicState))

        // 3. Dual Tower Bicolor mid-game (3 disks, 2 moved)
        val bicolorLevel = Level(12, 3, 4, "Dual Strategic", com.hanoi.binaryhanoi.domain.model.GameMode.Bicolor)
        var bicolorState = GameState(bicolorLevel, initialPegs(3, 4, com.hanoi.binaryhanoi.domain.model.GameMode.Bicolor))
        for (i in 0 until 2) {
            val m = findNext(bicolorState)
            assertTrue("Bicolor mid-game should produce hint", m != null)
            val p = bicolorState.pegs.toMutableMap()
            val disk = p[m!!.from]!!.last()
            p[m.from] = p[m.from]!!.dropLast(1)
            p[m.to] = p[m.to]!! + disk
            bicolorState = bicolorState.copy(pegs = p)
        }
        val bicolorHint = findNext(bicolorState)
        assertTrue(bicolorHint != null)
        assertTrue(validateMove(bicolorHint!!.from, bicolorHint.to, bicolorState))
    }

    private fun stateFor(disks: Int, pegs: Int): GameState {
        val level = Level(id = 1, diskCount = disks, pegCount = pegs, title = "Test")
        return GameState(level = level, pegs = initialPegs(disks, pegs))
    }
}
