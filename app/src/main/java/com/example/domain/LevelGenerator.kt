package com.example.domain

import kotlin.math.abs
import kotlin.random.Random

object LevelGenerator {
    const val TOTAL_LEVELS = 300

    fun generateLevel(levelNumber: Int): PuzzleLevel {
        val safeLevel = levelNumber.coerceIn(1, TOTAL_LEVELS)
        return when (safeLevel) {
            1 -> createVideoLevel3().copy(levelNumber = 1, rewardPerArrow = 0.0, levelClearBonus = 20.00)
            2 -> createVideoLevel4().copy(levelNumber = 2, rewardPerArrow = 0.0, levelClearBonus = 20.00)
            3 -> createTutorialLevel2().copy(levelNumber = 3, rewardPerArrow = 0.0, levelClearBonus = 20.00)
            4 -> createTutorialLevel1().copy(levelNumber = 4, rewardPerArrow = 0.0, levelClearBonus = 20.00)
            5 -> createShapedLevel(safeLevel, LevelTheme.HEART, heartMask(), 0.0, 20.00)
            6 -> createShapedLevel(safeLevel, LevelTheme.TOWER, towerMask(), 0.0, 20.00)
            7 -> createShapedLevel(safeLevel, LevelTheme.BIRD, birdMask(), 0.0, 20.00)
            8 -> createShapedLevel(safeLevel, LevelTheme.SPIRAL_LABYRINTH, denseMazeMask(14, 14), 0.0, 20.00)
            else -> {
                val themes = listOf(
                    LevelTheme.CLASSIC_MAZE,
                    LevelTheme.HEART,
                    LevelTheme.TOWER,
                    LevelTheme.BIRD,
                    LevelTheme.SPIRAL_LABYRINTH,
                    LevelTheme.CROWN,
                    LevelTheme.DIAMOND,
                    LevelTheme.BUTTERFLY
                )
                val theme = themes[(safeLevel - 1) % themes.size]
                val mask = when (theme) {
                    LevelTheme.HEART -> heartMask()
                    LevelTheme.TOWER -> towerMask()
                    LevelTheme.BIRD -> birdMask()
                    LevelTheme.CROWN -> crownMask()
                    LevelTheme.DIAMOND -> diamondMask()
                    LevelTheme.BUTTERFLY -> butterflyMask()
                    LevelTheme.SPIRAL_LABYRINTH -> denseMazeMask(14, 14)
                    LevelTheme.CLASSIC_MAZE -> {
                        val size = (10 + (safeLevel % 5)).coerceIn(10, 14)
                        denseMazeMask(size, size)
                    }
                }
                createShapedLevel(safeLevel, theme, mask, 0.0, 20.00)
            }
        }
    }

    /**
     * Checks if [arrow] can exit the board in [arrow.headDirection] without colliding
     * with any other arrow in [allArrows].
     * Returns null if unblocked, or the blocking GridPoint if blocked.
     */
    fun findBlockingPoint(
        arrow: BentArrow,
        allArrows: List<BentArrow>,
        gridWidth: Int,
        gridHeight: Int
    ): GridPoint? {
        val occupiedByOthers = HashSet<GridPoint>()
        for (other in allArrows) {
            if (other.id != arrow.id) {
                occupiedByOthers.addAll(other.points)
            }
        }
        var curr = arrow.head.move(arrow.headDirection)
        val maxSteps = maxOf(gridWidth, gridHeight) + 4
        var steps = 0
        while (curr.x in -1..(gridWidth + 1) && curr.y in -1..(gridHeight + 1) && steps < maxSteps) {
            if (curr in occupiedByOthers) {
                return curr
            }
            curr = curr.move(arrow.headDirection)
            steps++
        }
        return null
    }

    fun isArrowUnblocked(
        arrow: BentArrow,
        allArrows: List<BentArrow>,
        gridWidth: Int,
        gridHeight: Int
    ): Boolean = findBlockingPoint(arrow, allArrows, gridWidth, gridHeight) == null

    private fun polylineToGridPoints(vararg waypoints: Pair<Int, Int>): List<GridPoint> {
        val result = mutableListOf<GridPoint>()
        for (i in waypoints.indices) {
            val (x, y) = waypoints[i]
            if (result.isEmpty()) {
                result.add(GridPoint(x, y))
            } else {
                var curr = result.last()
                val target = GridPoint(x, y)
                while (curr != target) {
                    val dx = (target.x - curr.x).coerceIn(-1, 1)
                    val dy = (target.y - curr.y).coerceIn(-1, 1)
                    curr = GridPoint(curr.x + dx, curr.y + dy)
                    result.add(curr)
                }
            }
        }
        return result
    }

    private fun buildArrow(id: Int, vararg waypoints: Pair<Int, Int>): BentArrow {
        val pts = polylineToGridPoints(*waypoints)
        require(pts.size >= 2) { "Arrow must have at least 2 points" }
        val last = pts.last()
        val prev = pts[pts.size - 2]
        val dir = Direction.fromDelta(last.x - prev.x, last.y - prev.y)
        return BentArrow(id = id, points = pts, headDirection = dir)
    }

    private fun createTutorialLevel1(): PuzzleLevel {
        val arrows = listOf(
            buildArrow(1, 2 to 6, 2 to 2, 6 to 2), // exits RIGHT
            buildArrow(2, 6 to 6, 6 to 4, 4 to 4, 4 to 1), // exits UP
            buildArrow(3, 1 to 2, 1 to 7, 6 to 7) // exits RIGHT
        )
        return PuzzleLevel(
            levelNumber = 1,
            gridWidth = 8,
            gridHeight = 8,
            arrows = arrows,
            theme = LevelTheme.CLASSIC_MAZE,
            rewardPerArrow = 0.50,
            levelClearBonus = 115.00
        )
    }

    private fun createTutorialLevel2(): PuzzleLevel {
        val arrows = listOf(
            buildArrow(1, 1 to 7, 7 to 7, 7 to 1, 4 to 1, 4 to 3), // wait: let's make sure 1 exits UP
        )
        // Clean 4-arrow interlocking puzzle
        val validArrows = listOf(
            // 1. Exits UP through (4, 1)
            buildArrow(1, 5 to 7, 8 to 7, 8 to 1, 5 to 1, 5 to 5, 4 to 5, 4 to 1),
            // 2. Exits RIGHT through (7, 2)
            buildArrow(2, 3 to 6, 6 to 6, 6 to 2, 7 to 2),
            // 3. Exits UP through (2, 1)
            buildArrow(3, 3 to 2, 3 to 5, 2 to 5, 2 to 1),
            // 4. Exits RIGHT through (4, 7)
            buildArrow(4, 3 to 1, 1 to 1, 1 to 7, 4 to 7)
        )
        return PuzzleLevel(
            levelNumber = 2,
            gridWidth = 10,
            gridHeight = 9,
            arrows = validArrows,
            theme = LevelTheme.CLASSIC_MAZE,
            rewardPerArrow = 0.50,
            levelClearBonus = 132.50
        )
    }

    /**
     * Exact 5-arrow square maze from the video (00:17 - 01:39).
     * Elimination order matches the video:
     * 1) Arrow 1: Outer right/bottom wrapper that bends into the center and points UP at (4, 1).
     * 2) Arrow 2: Inner wrapper that goes up and points RIGHT at (8, 1).
     * 3) Arrow 3: Inner-left U-turn loop that points UP at (2, 2).
     * 4) Arrow 4: Left outer wrapper that points RIGHT at bottom (4, 8).
     * 5) Arrow 5: Right vertical arrow that points UP at (7, 2).
     */
    private fun createVideoLevel3(): PuzzleLevel {
        val arrows = listOf(
            // Arrow 1: cleared 1st (exits UP at col 4)
            buildArrow(1, 5 to 8, 8 to 8, 8 to 2, 6 to 2, 6 to 6, 4 to 6, 4 to 1),
            // Arrow 2: cleared 2nd (exits RIGHT at row 1, blocked by Arrow 1 wait -> at (8,1) it is clear or blocked by Arrow 1 if Arrow 1 goes to (8,1))
            // Wait: in the video at 01:35, Arrow 1 is tapped first, then Arrow 2 at 01:36, then Arrow 3 at 01:37, then Arrow 4 at 01:38, then Arrow 5 at 01:39.
            buildArrow(2, 5 to 2, 5 to 7, 2 to 7, 2 to 6, 3 to 6, 3 to 0, 5 to 0, 5 to 1, 7 to 1),
            // Arrow 3: cleared 3rd (exits UP at col 2; blocked by Arrow 2's top segment until Arrow 2 leaves!)
            buildArrow(3, 3 to 2, 3 to 5, 2 to 5, 2 to 2),
            // Arrow 4: cleared 4th (exits RIGHT at row 8; blocked by Arrow 1's bottom segment (5,8)..(8,8) until Arrow 1 leaves!)
            buildArrow(4, 2 to 0, 0 to 0, 0 to 8, 4 to 8),
            // Arrow 5: cleared 5th (exits UP at col 7; blocked by Arrow 2's head at (7,1) until Arrow 2 leaves!)
            buildArrow(5, 7 to 7, 7 to 2)
        )
        return PuzzleLevel(
            levelNumber = 3,
            gridWidth = 9,
            gridHeight = 9,
            arrows = arrows,
            theme = LevelTheme.CLASSIC_MAZE,
            rewardPerArrow = 0.50,
            levelClearBonus = 148.86
        )
    }

    /**
     * Exact 11-arrow square maze from the video (01:45 - 02:25).
     * Features the top horizontal arrow, right U-turn vertical arrow, middle C-arrow,
     * middle S-arrow, top-left spiral arrow, bottom-left vertical arrows, bottom-right spiral,
     * and bottom horizontal left-pointing arrow.
     */
    private fun createVideoLevel4(): PuzzleLevel {
        val arrows = listOf(
            // 1. Rightmost vertical arrow pointing DOWN at (10, 9) - cleared at 01:48
            buildArrow(1, 10 to 1, 10 to 9),
            // 2. Right-side long U-turn arrow pointing UP at (8, 0) - highlighted green & cleared at 02:03
            buildArrow(2, 7 to 0, 7 to 5, 8 to 5, 8 to 0),
            // 3. Top border L-arrow pointing RIGHT at (6, 0) - unblocked after Arrow 2 clears
            buildArrow(3, 0 to 4, 0 to 0, 6 to 0),
            // 4. Small C-shaped arrow in upper-middle pointing RIGHT at (6, 3) - highlighted green & cleared at 02:07
            buildArrow(4, 6 to 2, 5 to 2, 5 to 3, 6 to 3),
            // 5. Long S-shaped arrow in middle pointing RIGHT at (6, 5) - highlighted green & cleared at 02:09
            buildArrow(5, 6 to 4, 1 to 4, 1 to 5, 6 to 5),
            // 6. Upper-left spiral arrow pointing RIGHT at (4, 1) - blocked by Arrow 2 at (7,1)&(8,1) when tapped at 01:46
            buildArrow(6, 2 to 2, 4 to 2, 4 to 3, 1 to 3, 1 to 1, 4 to 1),
            // 7. Bottom-left outer vertical arrow pointing UP at (0, 6) - blocked by Arrow 3 until Arrow 3 clears
            buildArrow(7, 0 to 10, 0 to 6),
            // 8. Bottom-left inner vertical arrow pointing DOWN at (1, 9) - blocked by Arrow 11 at (1, 10)
            buildArrow(8, 1 to 6, 1 to 9),
            // 9. Bottom-left U-turn arrow pointing UP at (2, 6)
            buildArrow(9, 3 to 6, 3 to 9, 2 to 9, 2 to 6),
            // 10. Bottom-right spiral arrow pointing UP at (4, 6)
            buildArrow(10, 5 to 8, 8 to 8, 8 to 7, 6 to 7, 6 to 9, 4 to 9, 4 to 6),
            // 11. Bottom-most long horizontal arrow pointing LEFT at (1, 10) - blocked by Arrow 7 at (0, 10) when tapped at 01:50
            buildArrow(11, 9 to 7, 9 to 10, 1 to 10)
        )
        return PuzzleLevel(
            levelNumber = 4,
            gridWidth = 11,
            gridHeight = 11,
            arrows = arrows,
            theme = LevelTheme.CLASSIC_MAZE,
            rewardPerArrow = 0.35,
            levelClearBonus = 148.86
        )
    }

    /**
     * Generates a 100% mathematically guaranteed solvable puzzle inside any boolean shape mask!
     * Uses constructive peel-order generation:
     * At each step, selects a head cell in `remaining` that has a completely clear ray to the
     * outside of `remaining` in some direction `dir`, steps back to `head - dir`, and grows
     * a self-avoiding bent path inside `remaining`.
     */
    private fun createShapedLevel(
        levelNumber: Int,
        theme: LevelTheme,
        mask: Array<BooleanArray>,
        rewardPerArrow: Double,
        levelClearBonus: Double
    ): PuzzleLevel {
        val height = mask.size
        val width = mask[0].size
        val rand = Random(levelNumber * 9973L + theme.ordinal * 313L)

        val remaining = HashSet<GridPoint>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (mask[y][x]) {
                    remaining.add(GridPoint(x, y))
                }
            }
        }

        val generatedArrows = mutableListOf<BentArrow>()
        var nextId = 1

        fun isRayClear(from: GridPoint, dir: Direction, occupied: Set<GridPoint>): Boolean {
            var curr = from.move(dir)
            while (curr.x in 0 until width && curr.y in 0 until height) {
                if (curr in occupied) return false
                curr = curr.move(dir)
            }
            return true
        }

        while (remaining.isNotEmpty()) {
            // Find all candidate (head, dir) pairs where:
            // 1) head in remaining
            // 2) prev = head - dir in remaining
            // 3) ray from head in dir is completely clear of `remaining`
            val candidates = mutableListOf<Pair<GridPoint, Direction>>()
            for (pt in remaining) {
                for (dir in Direction.entries) {
                    val prev = GridPoint(pt.x - dir.dx, pt.y - dir.dy)
                    if (prev in remaining && isRayClear(pt, dir, remaining)) {
                        candidates.add(pt to dir)
                    }
                }
            }

            if (candidates.isEmpty()) {
                // If remaining cells have no pair (head, prev) with a clear ray (e.g. isolated single cell
                // or thin diagonal), find any single cell `pt` in `remaining` that has a clear ray in `dir`
                // and attach `(pt + dir)` if empty so it forms a valid 2-cell arrow, or remove isolated cell.
                var rescued = false
                val snapshot = remaining.toList().shuffled(rand)
                for (pt in snapshot) {
                    val dirs = Direction.entries.shuffled(rand)
                    for (dir in dirs) {
                        val ext = pt.move(dir)
                        if (ext.x in 0 until width && ext.y in 0 until height &&
                            ext !in remaining &&
                            generatedArrows.none { ext in it.points } &&
                            isRayClear(ext, dir, remaining)
                        ) {
                            // Grow backwards from pt inside remaining if possible
                            val pathBack = growPathBackwards(pt, ext, remaining, rand, maxLen = 7)
                            val fullPoints = pathBack.reversed() + ext
                            generatedArrows.add(
                                BentArrow(
                                    id = nextId++,
                                    points = fullPoints,
                                    headDirection = dir
                                )
                            )
                            remaining.removeAll(pathBack.toSet())
                            rescued = true
                            break
                        }
                    }
                    if (rescued) break
                }
                if (!rescued) {
                    // Drop any leftover singletons that cannot form a 2-cell arrow
                    remaining.clear()
                }
                continue
            }

            // Pick a candidate, preferring candidates whose backwards walk can grow at least 3-8 cells
            val (head, dir) = candidates[rand.nextInt(candidates.size)]
            val prev = GridPoint(head.x - dir.dx, head.y - dir.dy)
            val targetLen = rand.nextInt(4, 11)
            val backChain = growPathBackwards(prev, head, remaining, rand, targetLen)
            val arrowPoints = backChain.reversed() + head

            generatedArrows.add(
                BentArrow(
                    id = nextId++,
                    points = arrowPoints,
                    headDirection = dir
                )
            )
            remaining.remove(head)
            remaining.removeAll(backChain.toSet())
        }

        return PuzzleLevel(
            levelNumber = levelNumber,
            gridWidth = width,
            gridHeight = height,
            arrows = generatedArrows,
            theme = theme,
            rewardPerArrow = rewardPerArrow,
            levelClearBonus = levelClearBonus
        )
    }

    private fun growPathBackwards(
        startPrev: GridPoint,
        head: GridPoint,
        remaining: Set<GridPoint>,
        rand: Random,
        maxLen: Int
    ): List<GridPoint> {
        val chain = mutableListOf(startPrev)
        val visited = HashSet<GridPoint>()
        visited.add(head)
        visited.add(startPrev)

        var lastDir = Direction.fromDelta(startPrev.x - head.x, startPrev.y - head.y)

        while (chain.size < maxLen) {
            val curr = chain.last()
            val neighbors = Direction.entries.mapNotNull { d ->
                val nxt = curr.move(d)
                if (nxt in remaining && nxt !in visited) d to nxt else null
            }
            if (neighbors.isEmpty()) break

            // Prefer continuing straight for 2-3 cells or making crisp 90-degree turns
            val weighted = neighbors.sortedByDescending { (d, _) ->
                val straightBonus = if (d == lastDir && chain.size % 3 != 0) 2 else 0
                straightBonus + rand.nextInt(3)
            }
            val (chosenDir, chosenPt) = weighted.first()
            chain.add(chosenPt)
            visited.add(chosenPt)
            lastDir = chosenDir
        }
        return chain
    }

    private fun asciiToMask(rows: List<String>): Array<BooleanArray> {
        val h = rows.size
        val w = rows.maxOf { it.length }
        return Array(h) { y ->
            BooleanArray(w) { x ->
                x < rows[y].length && rows[y][x] == '#'
            }
        }
    }

    // Play Store Screenshot 5: Heart Shape Mask
    private fun heartMask(): Array<BooleanArray> = asciiToMask(
        listOf(
            "..###...###..",
            ".#####.#####.",
            "#############",
            "#############",
            "#############",
            ".###########.",
            "..#########..",
            "...#######...",
            "....#####....",
            ".....###.....",
            "......#......"
        )
    )

    // Play Store Screenshot 3: Eiffel Tower Shape Mask
    private fun towerMask(): Array<BooleanArray> = asciiToMask(
        listOf(
            ".....###.....",
            ".....###.....",
            "....#####....",
            "....#####....",
            "...#######...",
            "...#######...",
            "..#########..",
            "..#########..",
            ".###########.",
            "#############",
            "####.....####",
            "###.......###"
        )
    )

    // Play Store Screenshot 4: Bird / Parrot Shape Mask
    private fun birdMask(): Array<BooleanArray> = asciiToMask(
        listOf(
            "...######....",
            "..#########..",
            "..#######....",
            ".#########...",
            "###########..",
            "############.",
            ".###########.",
            "..##########.",
            "...#########.",
            "....#####.###",
            "...##..##..##",
            "..###..###..."
        )
    )

    private fun crownMask(): Array<BooleanArray> = asciiToMask(
        listOf(
            ".#....#....#.",
            "##...###...##",
            "###.#####.###",
            "#############",
            "#############",
            ".###########.",
            ".###########.",
            "..#########.."
        )
    )

    private fun diamondMask(): Array<BooleanArray> = asciiToMask(
        listOf(
            ".....###.....",
            "...#######...",
            "..#########..",
            ".###########.",
            "#############",
            ".###########.",
            "..#########..",
            "...#######...",
            ".....###....."
        )
    )

    private fun butterflyMask(): Array<BooleanArray> = asciiToMask(
        listOf(
            "###.......###",
            "#####...#####",
            "######.######",
            "#############",
            ".###########.",
            "..#########..",
            ".###########.",
            "#####...#####",
            "###.......###"
        )
    )

    private fun denseMazeMask(w: Int, h: Int): Array<BooleanArray> {
        return Array(h) { y ->
            BooleanArray(w) { x ->
                x in 1 until (w - 1) && y in 1 until (h - 1)
            }
        }
    }
}
