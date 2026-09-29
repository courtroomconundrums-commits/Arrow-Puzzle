package com.example

import com.example.domain.LevelGenerator
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun allLevelsAreSolvable() {
        for (lvl in 1..30) {
            val puzzle = LevelGenerator.generateLevel(lvl)
            assertTrue("Level $lvl should have arrows", puzzle.arrows.isNotEmpty())
            val remaining = puzzle.arrows.toMutableList()
            var progress = true
            while (remaining.isNotEmpty() && progress) {
                val unblocked = remaining.firstOrNull { arrow ->
                    LevelGenerator.isArrowUnblocked(
                        arrow = arrow,
                        allArrows = remaining,
                        gridWidth = puzzle.gridWidth,
                        gridHeight = puzzle.gridHeight
                    )
                }
                if (unblocked != null) {
                    remaining.remove(unblocked)
                } else {
                    progress = false
                }
            }
            assertTrue(
                "Level $lvl must be 100% solvable, stuck with ${remaining.size} arrows",
                remaining.isEmpty()
            )
        }
    }
}
