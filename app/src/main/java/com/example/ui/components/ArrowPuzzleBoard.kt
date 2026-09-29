package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.BentArrow
import com.example.domain.Direction
import com.example.domain.GridPoint
import com.example.ui.ExitingArrowAnim
import kotlin.math.hypot
import kotlin.math.min

@Composable
fun ArrowPuzzleBoard(
    gridWidth: Int,
    gridHeight: Int,
    activeArrows: List<BentArrow>,
    exitingArrows: List<ExitingArrowAnim>,
    zoomScale: Float,
    showGridLines: Boolean,
    showTutorialBanner: Boolean,
    tutorialBannerText: String,
    pinchToZoomHintText: String,
    onArrowTapped: (Int) -> Unit,
    onZoomChanged: (Float) -> Unit,
    onDismissTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var frameNowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Continuous 60fps animation clock when arrows are exiting
    LaunchedEffect(exitingArrows.isNotEmpty()) {
        while (exitingArrows.isNotEmpty()) {
            withFrameMillis {
                frameNowMs = System.currentTimeMillis()
            }
        }
    }

    // Reset pan when zoom returns near 1.0
    LaunchedEffect(zoomScale) {
        if (zoomScale <= 1.02f) {
            panOffset = Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .onSizeChanged { canvasSize = it }
            .pointerInput(zoomScale) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom != 1f) {
                        onZoomChanged((zoomScale * zoom).coerceIn(0.65f, 1.65f))
                    }
                    if (zoomScale > 1.0f) {
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-300f, 300f),
                            y = (panOffset.y + pan.y).coerceIn(-300f, 300f)
                        )
                    }
                }
            }
            .pointerInput(activeArrows, gridWidth, gridHeight, zoomScale, panOffset, canvasSize) {
                detectTapGestures { tapOffset ->
                    if (canvasSize.width <= 0 || canvasSize.height <= 0) return@detectTapGestures
                    val hitArrowId = findTappedArrowId(
                        tapOffset = tapOffset,
                        canvasWidth = canvasSize.width.toFloat(),
                        canvasHeight = canvasSize.height.toFloat(),
                        gridWidth = gridWidth,
                        gridHeight = gridHeight,
                        zoomScale = zoomScale,
                        panOffset = panOffset,
                        arrows = activeArrows
                    )
                    if (hitArrowId != null) {
                        onArrowTapped(hitArrowId)
                    }
                }
            }
            .testTag("arrow_puzzle_board")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val baseAvailable = min(w, h) * 0.80f
            val maxDim = maxOf(gridWidth, gridHeight).coerceAtLeast(1)
            val cellStep = (baseAvailable / maxDim) * zoomScale

            val boardWidthPx = (gridWidth - 1) * cellStep
            val boardHeightPx = (gridHeight - 1) * cellStep
            val originX = (w - boardWidthPx) / 2f + panOffset.x
            val originY = (h - boardHeightPx) / 2f + panOffset.y

            fun gridToPx(gx: Float, gy: Float): Offset =
                Offset(originX + gx * cellStep, originY + gy * cellStep)

            // 1. Optional helper alignment grid lines (matching 02:00 in video when # button is pressed)
            if (showGridLines) {
                val guideColor = Color(0xFF93C5FD).copy(alpha = 0.45f)
                // Outer boundary guidelines extending across the whole screen
                val leftEdge = originX - cellStep * 0.5f
                val rightEdge = originX + boardWidthPx + cellStep * 0.5f
                val topEdge = originY - cellStep * 0.5f
                val bottomEdge = originY + boardHeightPx + cellStep * 0.5f

                drawLine(guideColor, Offset(leftEdge, 0f), Offset(leftEdge, h), strokeWidth = 2.5f)
                drawLine(guideColor, Offset(rightEdge, 0f), Offset(rightEdge, h), strokeWidth = 2.5f)
                drawLine(guideColor, Offset(0f, topEdge), Offset(w, topEdge), strokeWidth = 2.5f)
                drawLine(guideColor, Offset(0f, bottomEdge), Offset(w, bottomEdge), strokeWidth = 2.5f)

                for (gx in 0 until gridWidth) {
                    val xPx = originX + gx * cellStep
                    drawLine(
                        color = guideColor.copy(alpha = 0.25f),
                        start = Offset(xPx, topEdge),
                        end = Offset(xPx, bottomEdge),
                        strokeWidth = 1.5f
                    )
                }
                for (gy in 0 until gridHeight) {
                    val yPx = originY + gy * cellStep
                    drawLine(
                        color = guideColor.copy(alpha = 0.25f),
                        start = Offset(leftEdge, yPx),
                        end = Offset(rightEdge, yPx),
                        strokeWidth = 1.5f
                    )
                }
            }

            // 2. Dot-matrix grid points (matching the grey dots on the board in the video)
            val dotRadius = (cellStep * 0.085f).coerceIn(2.2f, 4.8f)
            for (gy in 0 until gridHeight) {
                for (gx in 0 until gridWidth) {
                    drawCircle(
                        color = Color(0xFFCBD5E1),
                        radius = dotRadius,
                        center = gridToPx(gx.toFloat(), gy.toFloat())
                    )
                }
            }

            // 3. Draw active BentArrows
            val strokeThickness = (cellStep * 0.28f).coerceIn(5.5f, 16f)
            for (arrow in activeArrows) {
                val arrowColor = when {
                    arrow.isHintHighlighted -> Color(0xFF16A34A) // Bright green hint highlight (02:03 in video)
                    arrow.isFailedAttempt -> Color(0xFFEF4444)   // Red blocked arrow state (01:46 in video)
                    else -> Color(0xFF0F172A)                    // Classic dark navy arrow
                }

                if (arrow.isHintHighlighted) {
                    // Draw soft glowing green aura under the hinted arrow
                    drawArrowPolyline(
                        points = arrow.points.map { gridToPx(it.x.toFloat(), it.y.toFloat()) },
                        headDirection = arrow.headDirection,
                        color = Color(0xFF4ADE80).copy(alpha = 0.42f),
                        strokeWidth = strokeThickness * 1.85f,
                        cellStep = cellStep
                    )
                }

                drawArrowPolyline(
                    points = arrow.points.map { gridToPx(it.x.toFloat(), it.y.toFloat()) },
                    headDirection = arrow.headDirection,
                    color = arrowColor,
                    strokeWidth = strokeThickness,
                    cellStep = cellStep
                )
            }

            // 4. Draw slithering exiting arrows & flying pink cash bundles (matching 01:35 - 01:39 in video)
            for (anim in exitingArrows) {
                val elapsed = (frameNowMs - anim.startTimeMs).coerceAtLeast(0L)
                val progress = (elapsed / 480f).coerceIn(0f, 1f)

                val arrow = anim.arrow
                val segCount = (arrow.points.size - 1).coerceAtLeast(1)
                val extraSteps = maxDim + 4
                val extendedTrack = buildList {
                    addAll(arrow.points)
                    for (step in 1..extraSteps) {
                        add(arrow.head.move(arrow.headDirection, step))
                    }
                }

                val shift = progress * (segCount + extraSteps * 0.75f)
                val slidingPxPoints = sliceTrackAtShift(
                    track = extendedTrack,
                    shift = shift,
                    lengthSegments = segCount
                ).map { (gx, gy) -> gridToPx(gx, gy) }

                if (slidingPxPoints.size >= 2) {
                    val currentDir = determineHeadDirFromPx(
                        slidingPxPoints[slidingPxPoints.size - 2],
                        slidingPxPoints.last(),
                        arrow.headDirection
                    )
                    drawArrowPolyline(
                        points = slidingPxPoints,
                        headDirection = currentDir,
                        color = if (arrow.isHintHighlighted) Color(0xFF16A34A) else Color(0xFF0F172A),
                        strokeWidth = strokeThickness,
                        cellStep = cellStep
                    )
                }

                // Draw trailing pink cash bills flying from the arrow's cleared cells toward top-left wallet
                val sampleIndices = arrow.points.indices.filter {
                    it % maxOf(1, arrow.points.size / 5) == 0
                }.take(5)

                val targetWalletPx = Offset(w * 0.16f, -30f)
                for ((idx, ptIdx) in sampleIndices.withIndex()) {
                    val pt = arrow.points[ptIdx]
                    val startPx = gridToPx(pt.x.toFloat(), pt.y.toFloat())
                    val delayedProgress = ((progress - idx * 0.06f) / 0.85f).coerceIn(0f, 1f)
                    if (delayedProgress > 0f && delayedProgress < 1f) {
                        val curX = startPx.x + (targetWalletPx.x - startPx.x) * delayedProgress
                        val curY = startPx.y + (targetWalletPx.y - startPx.y) * (delayedProgress * delayedProgress)
                        drawMiniPinkCashBill(
                            center = Offset(curX, curY),
                            billSize = cellStep * 0.72f
                        )
                    }
                }
            }
        }

        // 5. Tutorial Ribbon & Pinch-to-Zoom Prompt (matching 00:14 - 00:16 in the video)
        AnimatedVisibility(
            visible = showTutorialBanner,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismissTutorial() }
            ) {
                // Golden yellow gradient banner across upper-middle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 86.dp)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFDE047).copy(alpha = 0.95f),
                                    Color(0xFFFACC15).copy(alpha = 0.95f),
                                    Color(0xFFFDE047).copy(alpha = 0.95f)
                                )
                            )
                        )
                        .border(1.5.dp, Color(0xFFEAB308))
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = tutorialBannerText,
                        color = Color(0xFF991B1B),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bottom-center pinch-to-zoom prompt box matching 00:14 - 00:16 in video
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 52.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(42.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👈", fontSize = 32.sp)
                        Text(text = "👉", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFF64748B), RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = pinchToZoomHintText,
                            color = Color(0xFF1E293B),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawArrowPolyline(
    points: List<Offset>,
    headDirection: Direction,
    color: Color,
    strokeWidth: Float,
    cellStep: Float
) {
    if (points.size < 2) return

    val headTip = points.last()
    val prevPt = points[points.size - 2]

    // Shorten the last shaft segment slightly so the triangular arrowhead tip lands cleanly
    val headLength = (cellStep * 0.46f).coerceIn(10f, 26f)
    val headHalfWidth = (cellStep * 0.34f).coerceIn(7f, 19f)

    val dx = headTip.x - prevPt.x
    val dy = headTip.y - prevPt.y
    val segLen = hypot(dx, dy)

    val shaftEnd = if (segLen > headLength * 0.6f) {
        Offset(
            x = headTip.x - (dx / segLen) * (headLength * 0.45f),
            y = headTip.y - (dy / segLen) * (headLength * 0.45f)
        )
    } else {
        headTip
    }

    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size - 1) {
            lineTo(points[i].x, points[i].y)
        }
        lineTo(shaftEnd.x, shaftEnd.y)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Butt,
            join = StrokeJoin.Miter
        )
    )

    // Draw sharp triangular arrowhead at headTip pointing in headDirection
    val tipOffset = Offset(
        x = headTip.x + headDirection.dx * (headLength * 0.28f),
        y = headTip.y + headDirection.dy * (headLength * 0.28f)
    )
    val baseCenter = Offset(
        x = tipOffset.x - headDirection.dx * headLength,
        y = tipOffset.y - headDirection.dy * headLength
    )
    val perpX = -headDirection.dy.toFloat()
    val perpY = headDirection.dx.toFloat()

    val headPath = Path().apply {
        moveTo(tipOffset.x, tipOffset.y)
        lineTo(baseCenter.x + perpX * headHalfWidth, baseCenter.y + perpY * headHalfWidth)
        lineTo(baseCenter.x - perpX * headHalfWidth, baseCenter.y - perpY * headHalfWidth)
        close()
    }
    drawPath(path = headPath, color = color)
}

private fun DrawScope.drawMiniPinkCashBill(
    center: Offset,
    billSize: Float
) {
    rotate(degrees = -18f, pivot = center) {
        val w = billSize
        val h = billSize * 0.62f
        val topLeft = Offset(center.x - w / 2f, center.y - h / 2f)
        drawRoundRect(
            color = Color(0xFFBE185D),
            topLeft = Offset(topLeft.x, topLeft.y + h * 0.16f),
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )
        drawRoundRect(
            color = Color(0xFFF472B6),
            topLeft = topLeft,
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )
        // White paper band
        drawRect(
            color = Color.White,
            topLeft = Offset(center.x - w * 0.12f, topLeft.y),
            size = Size(w * 0.24f, h * 1.12f)
        )
    }
}

private fun sliceTrackAtShift(
    track: List<GridPoint>,
    shift: Float,
    lengthSegments: Int
): List<Pair<Float, Float>> {
    if (track.size < 2) return emptyList()
    val maxIdx = (track.size - 1).toFloat()
    val startPos = shift.coerceIn(0f, maxIdx)
    val endPos = (shift + lengthSegments).coerceIn(0f, maxIdx)
    if (endPos <= startPos + 0.05f) return emptyList()

    val result = mutableListOf<Pair<Float, Float>>()

    fun interpAt(pos: Float): Pair<Float, Float> {
        val idx = pos.toInt().coerceIn(0, track.size - 2)
        val frac = (pos - idx).coerceIn(0f, 1f)
        val a = track[idx]
        val b = track[idx + 1]
        return (a.x + (b.x - a.x) * frac) to (a.y + (b.y - a.y) * frac)
    }

    result.add(interpAt(startPos))
    val firstWhole = startPos.toInt() + 1
    val lastWhole = endPos.toInt()
    for (i in firstWhole..lastWhole) {
        if (i in track.indices && i.toFloat() > startPos && i.toFloat() < endPos) {
            result.add(track[i].x.toFloat() to track[i].y.toFloat())
        }
    }
    result.add(interpAt(endPos))
    return result
}

private fun determineHeadDirFromPx(
    prev: Offset,
    last: Offset,
    fallback: Direction
): Direction {
    val dx = last.x - prev.x
    val dy = last.y - prev.y
    if (hypot(dx, dy) < 0.5f) return fallback
    return if (kotlin.math.abs(dx) >= kotlin.math.abs(dy)) {
        if (dx >= 0f) Direction.RIGHT else Direction.LEFT
    } else {
        if (dy >= 0f) Direction.DOWN else Direction.UP
    }
}

private fun findTappedArrowId(
    tapOffset: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    gridWidth: Int,
    gridHeight: Int,
    zoomScale: Float,
    panOffset: Offset,
    arrows: List<BentArrow>
): Int? {
    val baseAvailable = min(canvasWidth, canvasHeight) * 0.80f
    val maxDim = maxOf(gridWidth, gridHeight).coerceAtLeast(1)
    val cellStep = (baseAvailable / maxDim) * zoomScale
    val boardWidthPx = (gridWidth - 1) * cellStep
    val boardHeightPx = (gridHeight - 1) * cellStep
    val originX = (canvasWidth - boardWidthPx) / 2f + panOffset.x
    val originY = (canvasHeight - boardHeightPx) / 2f + panOffset.y

    fun gridToPx(pt: GridPoint): Offset =
        Offset(originX + pt.x * cellStep, originY + pt.y * cellStep)

    val maxTapDistance = cellStep * 0.78f
    var bestArrowId: Int? = null
    var bestDistance = Float.MAX_VALUE

    for (arrow in arrows) {
        for (i in 0 until arrow.points.size - 1) {
            val a = gridToPx(arrow.points[i])
            val b = gridToPx(arrow.points[i + 1])
            val dist = pointToSegmentDistance(tapOffset, a, b)
            if (dist < bestDistance) {
                bestDistance = dist
                bestArrowId = arrow.id
            }
        }
    }

    return if (bestDistance <= maxTapDistance) bestArrowId else null
}

private fun pointToSegmentDistance(p: Offset, a: Offset, b: Offset): Float {
    val vx = b.x - a.x
    val vy = b.y - a.y
    val lenSq = vx * vx + vy * vy
    if (lenSq <= 0.0001f) return hypot(p.x - a.x, p.y - a.y)
    val t = (((p.x - a.x) * vx + (p.y - a.y) * vy) / lenSq).coerceIn(0f, 1f)
    val projX = a.x + t * vx
    val projY = a.y + t * vy
    return hypot(p.x - projX, p.y - projY)
}
