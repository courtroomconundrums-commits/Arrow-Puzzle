package com.example.domain

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    companion object {
        fun fromDelta(dx: Int, dy: Int): Direction {
            return when {
                dx > 0 -> RIGHT
                dx < 0 -> LEFT
                dy < 0 -> UP
                else -> DOWN
            }
        }
    }
}

data class GridPoint(val x: Int, val y: Int) {
    fun move(dir: Direction, steps: Int = 1): GridPoint =
        GridPoint(x + dir.dx * steps, y + dir.dy * steps)
}

data class BentArrow(
    val id: Int,
    val points: List<GridPoint>, // Ordered from tail (first) to head (last)
    val headDirection: Direction,
    val isFailedAttempt: Boolean = false,
    val isHintHighlighted: Boolean = false
) {
    val head: GridPoint get() = points.last()
    val tail: GridPoint get() = points.first()
}

enum class LevelTheme(val titleBn: String, val titleEn: String) {
    CLASSIC_MAZE("ক্লাসিক মেজ", "Classic Maze"),
    HEART("হার্ট পাজল", "Heart Puzzle"),
    TOWER("টাওয়ার পাজল", "Tower Puzzle"),
    BIRD("পাখি পাজল", "Bird Puzzle"),
    SPIRAL_LABYRINTH("স্পাইরাল ল্যাবিরিন্থ", "Spiral Labyrinth"),
    CROWN("মুকুট পাজল", "Crown Puzzle"),
    DIAMOND("ডায়মন্ড পাজল", "Diamond Puzzle"),
    BUTTERFLY("প্রজাপতি পাজল", "Butterfly Puzzle")
}

data class PuzzleLevel(
    val levelNumber: Int,
    val gridWidth: Int,
    val gridHeight: Int,
    val arrows: List<BentArrow>,
    val theme: LevelTheme,
    val rewardPerArrow: Double = 0.0,
    val levelClearBonus: Double = 20.00
)

data class TaskItem(
    val id: String,
    val isCareer: Boolean,
    val titleBn: String,
    val titleEn: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardAmount: Double,
    val rewardAmountBdt: Double = rewardAmount,
    val isClaimed: Boolean
) {
    val isCompleted: Boolean get() = currentProgress >= targetProgress
}

/**
 * Country-specific local mobile banking / digital wallet withdrawal option.
 */
data class MobileBankingOption(
    val id: String,
    val displayName: String,
    val subLabel: String,
    val iconEmoji: String,
    val startColorHex: Long,
    val endColorHex: Long,
    val textColorHex: Long = 0xFFFFFFFF
)
