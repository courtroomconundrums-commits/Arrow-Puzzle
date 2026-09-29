package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AppLanguage
import com.example.domain.CountryRegion
import com.example.domain.LevelGenerator
import com.example.domain.LocalizedStrings
import com.example.domain.TaskItem
import com.example.ui.FlyingCashBurst
import com.example.ui.RewardOverlayState

/**
 * Task Center Modal matching 00:21 - 00:35 in the video and Screenshot 1 (Pusat Tugas / টাস্ক সেন্টার).
 */
@Composable
fun TaskCenterModal(
    strings: LocalizedStrings,
    currencySymbol: String,
    isCareerTab: Boolean,
    dailyTasks: List<TaskItem>,
    careerTasks: List<TaskItem>,
    onSelectTab: (Boolean) -> Unit,
    onTaskAction: (TaskItem) -> Unit,
    onClose: () -> Unit
) {
    val tasks = if (isCareerTab) careerTasks else dailyTasks
    val dailyUnclaimed = dailyTasks.any { it.isCompleted && !it.isClaimed }
    val careerUnclaimed = careerTasks.any { it.isCompleted && !it.isClaimed }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .clickable(enabled = true, onClick = {})
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .testTag("task_center_modal"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main blue board with floating yellow header
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Blue body card
                Column(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF93C5FD), Color(0xFF60A5FA), Color(0xFF3B82F6))
                            )
                        )
                        .border(3.dp, Color(0xFF1D4ED8), RoundedCornerShape(26.dp))
                        .padding(top = 36.dp, start = 12.dp, end = 12.dp, bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Cyan sub-header ribbon ("দৈনিক টাস্ক" / "ক্যারিয়ার টাস্ক")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF38BDF8).copy(alpha = 0.4f),
                                        Color(0xFFBAE6FD),
                                        Color(0xFF38BDF8).copy(alpha = 0.4f)
                                    )
                                )
                            )
                            .border(1.5.dp, Color.White, RoundedCornerShape(50))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCareerTab) strings.careerTasksTab else strings.dailyTasksTab,
                            color = Color(0xFF0F172A),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scrollable list of clipboard task cards
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 390.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            TaskClipboardCard(
                                task = task,
                                strings = strings,
                                currencySymbol = currencySymbol,
                                onAction = { onTaskAction(task) }
                            )
                        }
                    }
                }

                // Floating 3D Yellow Header ("টাস্ক সেন্টার" / "Pusat Tugas")
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFDE047), Color(0xFFFACC15), Color(0xFFEAB308))
                            )
                        )
                        .border(2.5.dp, Color(0xFFCA8A04), RoundedCornerShape(20.dp))
                        .padding(horizontal = 38.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = strings.taskCenterTitle,
                        color = Color(0xFF9A3412),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Red 'X' close button on top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 12.dp)
                        .size(44.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF87171), Color(0xFFEF4444), Color(0xFFDC2626))
                            )
                        )
                        .border(2.dp, Color(0xFF991B1B), RoundedCornerShape(12.dp))
                        .clickable { onClose() }
                        .testTag("close_task_center_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Bottom Folder Tabs ("দৈনিক টাস্ক" & "ক্যারিয়ার টাস্ক") matching 00:22 in video
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .offset(y = (-4).dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Daily Tasks Tab (Blue)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                        .background(
                            if (!isCareerTab) {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF60A5FA), Color(0xFF2563EB))
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                )
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = if (!isCareerTab) Color.White else Color(0xFF1E40AF),
                            shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
                        )
                        .clickable { onSelectTab(false) }
                        .testTag("daily_tasks_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.dailyTasksTab,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (dailyUnclaimed) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 6.dp, end = 10.dp)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }

                // Career Tasks Tab (Purple)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                        .background(
                            if (isCareerTab) {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFC084FC), Color(0xFF9333EA))
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFA855F7), Color(0xFF7E22CE))
                                )
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = if (isCareerTab) Color.White else Color(0xFF581C87),
                            shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
                        )
                        .clickable { onSelectTab(true) }
                        .testTag("career_tasks_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.careerTasksTab,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (careerUnclaimed) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 6.dp, end = 10.dp)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskClipboardCard(
    task: TaskItem,
    strings: LocalizedStrings,
    currencySymbol: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        // White rounded card with blue outline
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(2.dp, Color(0xFF3B82F6), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Pink Cash Stack + Amount ("৳ 25.00")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(74.dp)
            ) {
                PinkCashStackIcon(size = 40.dp)
                Text(
                    text = "$currencySymbol%.2f".format(task.rewardAmount),
                    color = Color(0xFFFEF08A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF475569))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Middle: Title + Progress Bar ("1/1", "2/3")
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.titleBn,
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val fraction = (task.currentProgress.toFloat() / task.targetProgress.coerceAtLeast(1))
                    .coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFCBD5E1))
                        .border(1.dp, Color(0xFF64748B), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFDE047), Color(0xFFFACC15))
                                )
                            )
                    )
                    Text(
                        text = "${task.currentProgress}/${task.targetProgress}",
                        color = Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Action button ("দাবি করুন" green vs "অসম্পূর্ণ" yellow)
            val isReadyToClaim = task.isCompleted && !task.isClaimed
            val buttonBrush = when {
                task.isClaimed -> Brush.verticalGradient(listOf(Color(0xFF94A3B8), Color(0xFF64748B)))
                isReadyToClaim -> Brush.verticalGradient(listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A)))
                else -> Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFFACC15), Color(0xFFEAB308)))
            }
            val borderColor = when {
                task.isClaimed -> Color(0xFF475569)
                isReadyToClaim -> Color(0xFF15803D)
                else -> Color(0xFFB45309)
            }
            val label = when {
                task.isClaimed -> strings.claimedBtn
                isReadyToClaim -> strings.claimBtn
                else -> strings.incompleteBtn
            }

            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(buttonBrush)
                    .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable(enabled = !task.isClaimed) { onAction() }
                    .padding(horizontal = 12.dp)
                    .testTag("task_action_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Top cyan clipboard clip icon matching 00:22 in video
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF38BDF8))
                .border(1.5.dp, Color(0xFF0369A1), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Reward Claim Overlay ("টাস্ক পুরস্কার" & "সফল" Level Complete) matching 00:37 - 00:47 & 01:40 - 01:43 in the video.
 */
@Composable
fun RewardClaimOverlay(
    overlay: RewardOverlayState,
    strings: LocalizedStrings,
    currencySymbol: String,
    onClaimFullWithAd: () -> Unit,
    onClaimBaseOnly: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reward_sunburst")
    val rayAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ray_angle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.80f))
            .clickable(enabled = true, onClick = {})
            .testTag("reward_claim_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Confetti & Golden Sunburst Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.39f

            // Glowing golden sunburst behind the pink cash bundle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFDE047).copy(alpha = 0.55f),
                        Color(0xFFFACC15).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = size.minDimension * 0.42f
                ),
                center = Offset(cx, cy),
                radius = size.minDimension * 0.42f
            )

            rotate(degrees = rayAngle, pivot = Offset(cx, cy)) {
                for (i in 0 until 12) {
                    rotate(degrees = i * 30f, pivot = Offset(cx, cy)) {
                        drawLine(
                            color = Color(0xFFFFEF99).copy(alpha = 0.32f),
                            start = Offset(cx, cy),
                            end = Offset(cx, cy - size.minDimension * 0.36f),
                            strokeWidth = 14f
                        )
                    }
                }
            }

            // Festive confetti ribbons around the screen
            val confettiColors = listOf(
                Color(0xFFF43F5E),
                Color(0xFF38BDF8),
                Color(0xFFFACC15),
                Color(0xFF4ADE80),
                Color(0xFFC084FC)
            )
            val spots = listOf(
                0.12f to 0.18f, 0.85f to 0.16f, 0.22f to 0.28f, 0.78f to 0.26f,
                0.08f to 0.42f, 0.91f to 0.45f, 0.16f to 0.68f, 0.84f to 0.72f,
                0.28f to 0.85f, 0.74f to 0.88f
            )
            spots.forEachIndexed { idx, (fx, fy) ->
                val c = confettiColors[idx % confettiColors.size]
                rotate(degrees = (idx * 47f + rayAngle * 0.5f) % 360f, pivot = Offset(size.width * fx, size.height * fy)) {
                    drawRect(
                        color = c,
                        topLeft = Offset(size.width * fx, size.height * fy),
                        size = androidx.compose.ui.geometry.Size(22f, 9f)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            // Title ("টাস্ক পুরস্কার" or "সফল")
            Text(
                text = if (overlay.isLevelComplete) strings.levelSuccessTitle else strings.taskRewardTitle,
                color = Color(0xFFFDE047),
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large Pink Cash Stack Illustration
            PinkCashStackIcon(size = 150.dp)

            Spacer(modifier = Modifier.height(16.dp))

            // Big Yellow Reward Amount ("+৳ 25.00" or "+৳ 148.86")
            Text(
                text = "+$currencySymbol %.2f".format(overlay.fullAmount),
                color = Color(0xFFFACC15),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Green 3D Button with TV icon ("দাবি করতে বিজ্ঞাপন দেখুন")
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(58.dp)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                        )
                    )
                    .border(2.dp, Color(0xFFBBF7D0), RoundedCornerShape(18.dp))
                    .clickable { onClaimFullWithAd() }
                    .testTag("claim_full_reward_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "📺", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.watchAdToClaimBtn,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom action: "Next Level" (no money earned) for Level Complete, or Base Claim for Task Rewards
            if (overlay.isLevelComplete) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable { onClaimBaseOnly() }
                        .padding(horizontal = 28.dp, vertical = 11.dp)
                        .testTag("claim_base_reward_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${strings.nextLevelBtn} ➔",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                Text(
                    text = strings.claimBaseOnlyBtn("%.2f".format(overlay.baseAmount)),
                    color = Color(0xFFCBD5E1),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onClaimBaseOnly() }
                        .padding(10.dp)
                        .testTag("claim_base_reward_button")
                )
            }
        }
    }
}

/**
 * Center Cash Toast & Flying Bills Animation (matching 00:48 - 00:51 & 01:43 - 01:45 in the video).
 */
@Composable
fun FlyingCashBurstOverlay(
    burst: FlyingCashBurst,
    currencySymbol: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cash_fly")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "burst_progress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Center rounded dark card with pink cash stack & "+৳ 12.50" / "+৳ 49.62"
        if (progress < 0.72f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(165.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black.copy(alpha = 0.76f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PinkCashStackIcon(size = 86.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "+$currencySymbol %.2f".format(burst.amount),
                        color = Color(0xFFF87171),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // 5 Pink Cash Stacks flying in an arc from center up to top-left wallet
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = size.width * 0.5f
            val startY = size.height * 0.48f
            val targetX = size.width * 0.16f
            val targetY = size.height * 0.05f

            for (i in 0 until 5) {
                val itemProg = ((progress - i * 0.10f) / 0.60f).coerceIn(0f, 1f)
                if (itemProg > 0f && itemProg < 1f) {
                    val x = startX + (targetX - startX) * itemProg + kotlin.math.sin(itemProg * Math.PI).toFloat() * (i - 2) * 35f
                    val y = startY + (targetY - startY) * itemProg
                    rotate(degrees = -15f, pivot = Offset(x, y)) {
                        drawRoundRect(
                            color = Color(0xFFEC4899),
                            topLeft = Offset(x - 36f, y - 22f),
                            size = androidx.compose.ui.geometry.Size(72f, 44f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(x - 8f, y - 22f),
                            size = androidx.compose.ui.geometry.Size(16f, 44f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Out of Lives Modal ("চালিয়ে যাবেন? / লাইফ শেষ!") matching 01:52 - 01:55 in the video.
 */
@Composable
fun OutOfLivesDialog(
    strings: LocalizedStrings,
    onFreeRevive: () -> Unit,
    onTryAgain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .clickable(enabled = true, onClick = {})
            .padding(horizontal = 24.dp)
            .testTag("out_of_lives_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Blue outer frame with white inner card
            Column(
                modifier = Modifier
                    .padding(top = 26.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF60A5FA))
                    .border(3.dp, Color(0xFF2563EB), RoundedCornerShape(28.dp))
                    .padding(10.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(top = 34.dp, bottom = 22.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.outOfLivesTitle,
                    color = Color(0xFF2563EB),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.outOfLivesSubtitle,
                    color = Color(0xFF1E293B),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Sad cute pink heart matching 01:53 in video
                CuteHeartIcon(
                    isFilled = true,
                    isSad = true,
                    size = 110.dp
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Green Free Revive button ("বিনামূল্যে পুনরুজ্জীবন")
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .height(54.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                            )
                        )
                        .border(2.dp, Color(0xFF15803D), RoundedCornerShape(16.dp))
                        .clickable { onFreeRevive() }
                        .testTag("free_revive_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📺", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.freeReviveBtn,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.tryAgainBtn,
                    color = Color(0xFF94A3B8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onTryAgain() }
                        .padding(8.dp)
                        .testTag("try_again_button")
                )
            }

            // Floating Yellow Header ("চালিয়ে যাবেন?")
            Box(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFDE047), Color(0xFFFACC15), Color(0xFFEAB308))
                        )
                    )
                    .border(2.dp, Color(0xFFCA8A04), RoundedCornerShape(20.dp))
                    .padding(horizontal = 36.dp, vertical = 10.dp)
            ) {
                Text(
                    text = strings.continueHeader,
                    color = Color(0xFF9A3412),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Red 'X' close button on top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 12.dp)
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444))
                    .border(2.dp, Color(0xFF991B1B), RoundedCornerShape(12.dp))
                    .clickable { onTryAgain() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Multi-Language Settings & Level Controls Modal (with 8 full languages!).
 */
@Composable
fun SettingsAndLanguageDialog(
    strings: LocalizedStrings,
    currentRegion: CountryRegion,
    currentLanguage: AppLanguage,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onSelectCountryRegion: (CountryRegion) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onOpenLevelPicker: () -> Unit,
    onRestartLevel: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .clickable(enabled = true, onClick = {})
            .padding(horizontal = 20.dp, vertical = 32.dp)
            .testTag("settings_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))
                    )
                )
                .border(3.dp, Color(0xFF1D4ED8), RoundedCornerShape(26.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.settingsTitle,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444))
                        .clickable { onClose() }
                        .testTag("close_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sound & Vibration switches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔊 ${strings.soundLabel}",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { onToggleSound() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF22C55E))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📳 ${strings.vibrationLabel}",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { onToggleVibration() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF22C55E))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Country / Region & Mobile Banking Selector
            Text(
                text = "🌍 Country & Mobile Banking (Auto-Detected: ${currentRegion.flagEmoji} ${currentRegion.countryCode})",
                color = Color(0xFFFDE047),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 135.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CountryRegion.entries) { reg ->
                    val isRegSelected = reg == currentRegion
                    val methodsText = reg.mobileBankingOptions.joinToString("/") { it.displayName }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isRegSelected) Color(0xFFFDE047) else Color.White)
                            .border(
                                width = if (isRegSelected) 2.5.dp else 1.dp,
                                color = if (isRegSelected) Color(0xFF15803D) else Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectCountryRegion(reg) }
                            .padding(horizontal = 8.dp, vertical = 7.dp)
                            .testTag("region_option_${reg.countryCode}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = reg.flagEmoji, fontSize = 17.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${reg.countryCode} (${reg.currencySymbol}) • 1Ad=${reg.currencySymbol}${"%.2f".format(reg.oneAdRewardLocal())}",
                                color = Color(0xFF0F172A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                            Text(
                                text = methodsText,
                                color = Color(0xFF475569),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 8-Language Selector Grid
            Text(
                text = "🌐 ${strings.languageLabel}",
                color = Color(0xFFFDE047),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 135.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppLanguage.entries) { lang ->
                    val isSelected = lang == currentLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFFDE047) else Color.White)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF15803D) else Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectLanguage(lang) }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("lang_option_${lang.code}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = lang.flagEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lang.nativeName,
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                            Text(
                                text = "${lang.englishName} (${lang.currencySymbol})",
                                color = Color(0xFF475569),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                        if (isSelected) {
                            Text("✔", color = Color(0xFF15803D), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level Picker & Restart Level buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFACC15))
                        .border(2.dp, Color(0xFFCA8A04), RoundedCornerShape(14.dp))
                        .clickable { onOpenLevelPicker() }
                        .testTag("open_level_picker_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧩 300 Levels",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF22C55E))
                        .border(2.dp, Color(0xFF15803D), RoundedCornerShape(14.dp))
                        .clickable {
                            onRestartLevel()
                            onClose()
                        }
                        .testTag("restart_level_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔄 ${strings.tryAgainBtn}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * 300 Levels Selector Dialog with Lock / Unlock progression:
 * Only levels <= maxUnlockedLevel are unlocked; levels > maxUnlockedLevel are locked (🔒)
 * and unlock one by one as the player completes each level!
 */
@Composable
fun LevelPickerDialog(
    currentLevel: Int,
    maxUnlockedLevel: Int,
    isBengali: Boolean,
    onSelectLevel: (Int) -> Unit,
    onClose: () -> Unit
) {
    val levels = (1..LevelGenerator.TOTAL_LEVELS).toList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = true, onClick = {})
            .padding(horizontal = 18.dp, vertical = 36.dp)
            .testTag("level_picker_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF3B82F6))
                .border(3.dp, Color(0xFF1D4ED8), RoundedCornerShape(26.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBengali) "লেভেল নির্বাচন করুন (1 - 300)" else "Select Puzzle Level (1 - 300)",
                        color = Color(0xFFFDE047),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isBengali) {
                            "আনলক করা হয়েছে: $maxUnlockedLevel / ${LevelGenerator.TOTAL_LEVELS}"
                        } else {
                            "Unlocked: $maxUnlockedLevel / ${LevelGenerator.TOTAL_LEVELS}"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.White, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(levels) { lvl ->
                    val isUnlocked = lvl <= maxUnlockedLevel
                    val isCurrent = lvl == currentLevel
                    val isCompleted = lvl < maxUnlockedLevel
                    val badgeEmoji = if (!isUnlocked) {
                        "🔒"
                    } else {
                        when (lvl) {
                            1 -> "🎬"
                            2 -> "🎬"
                            5 -> "💖"
                            6 -> "🗼"
                            7 -> "🦜"
                            8 -> "🌀"
                            else -> when ((lvl - 1) % 8) {
                                1 -> "💖"
                                2 -> "🗼"
                                3 -> "🦜"
                                4 -> "🌀"
                                5 -> "👑"
                                6 -> "💎"
                                7 -> "🦋"
                                else -> "⬆️"
                            }
                        }
                    }
                    val cardBg = when {
                        isCurrent -> Color(0xFFFDE047)
                        isUnlocked -> Color.White
                        else -> Color(0xFF1E3A8A).copy(alpha = 0.65f)
                    }
                    val borderCol = when {
                        isCurrent -> Color(0xFF16A34A)
                        isUnlocked -> Color(0xFF93C5FD)
                        else -> Color(0xFF334155)
                    }
                    val textCol = if (isUnlocked) Color(0xFF0F172A) else Color(0xFF94A3B8)

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .border(
                                width = 2.dp,
                                color = borderCol,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectLevel(lvl) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .testTag("pick_level_$lvl"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = badgeEmoji, fontSize = 18.sp)
                        Text(
                            text = "Lv.$lvl",
                            color = textCol,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = when {
                                !isUnlocked -> if (isBengali) "লক" else "LOCKED"
                                isCompleted -> "✓ DONE"
                                else -> "▶ PLAY"
                            },
                            color = when {
                                !isUnlocked -> Color(0xFFF87171)
                                isCompleted -> Color(0xFF16A34A)
                                else -> Color(0xFF2563EB)
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
