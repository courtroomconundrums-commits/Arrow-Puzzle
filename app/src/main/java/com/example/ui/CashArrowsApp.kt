package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.ui.components.AdMobBannerBar
import com.example.ui.components.AdMobRewardedVideoDialog
import com.example.ui.components.ArrowPuzzleBoard
import com.example.ui.components.CashArrowSplashScreen
import com.example.ui.components.CuteHeartIcon
import com.example.ui.components.FlyingCashBurstOverlay
import com.example.ui.components.LevelPickerDialog
import com.example.ui.components.OutOfLivesDialog
import com.example.ui.components.PinkCashStackIcon
import com.example.ui.components.RewardClaimOverlay
import com.example.ui.components.SettingsAndLanguageDialog
import com.example.ui.components.TaskCenterModal
import com.example.ui.components.WithdrawScreen

@Composable
fun CashArrowsApp(viewModel: GameViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = state.strings
    val sym = state.currencySymbol
    val tickerMessages = strings.broadcastMessages
    val currentTicker = if (tickerMessages.isNotEmpty()) {
        tickerMessages[state.tickerIndex % tickerMessages.size]
    } else {
        strings.noWithdrawUpdate
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (state.screen) {
            ActiveScreen.SPLASH -> {
                CashArrowSplashScreen(
                    progress = state.splashProgress,
                    onSkip = { viewModel.skipSplash() }
                )
            }

            ActiveScreen.WITHDRAW -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        WithdrawScreen(
                            balance = state.displayBalance,
                            selectedTierBdt = state.selectedWithdrawTierBdt,
                            countryRegion = state.countryRegion,
                            savedPaymentMethodId = state.player.savedPaymentMethod,
                            savedAccountNumber = state.player.savedAccountNumber,
                            savedAccountName = state.player.savedAccountName,
                            strings = strings,
                            currencySymbol = sym,
                            tickerText = currentTicker,
                            showAccountModal = state.showAccountInputModal,
                            showConfirmModal = state.showWithdrawConfirmModal,
                            onSelectTierBdt = { viewModel.selectWithdrawTierBdt(it) },
                            onSelectCountryRegion = { viewModel.selectCountryRegion(it) },
                            onClickWithdrawButton = { viewModel.openAccountInputModal() },
                            onSaveAccountDetails = { method, acc, name ->
                                viewModel.saveAccountDetails(method, acc, name)
                            },
                            onConfirmWithdraw = { viewModel.confirmWithdrawalRequest() },
                            onCloseModals = { viewModel.closeWithdrawModals() },
                            onBack = { viewModel.navigateBackToGame() }
                        )
                    }
                    // AdMob Test Banner Ad at bottom of Withdraw Screen
                    AdMobBannerBar(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    )
                }
            }

            ActiveScreen.GAME -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    // 1. Top Blue HUD Header with Watch Video Ad button
                    TopGameHudHeader(
                        state = state,
                        onWithdrawClick = { viewModel.navigateToWithdraw() },
                        onOpenTaskCenter = { viewModel.setShowTaskCenter(true) },
                        onOpenSettings = { viewModel.setShowSettings(true) },
                        onWatchVideoAd = {
                            viewModel.triggerRewardedVideoAd(
                                context = context,
                                type = PendingRewardVideoType.WATCH_TO_EARN_BONUS,
                                description = "Watch Video Bonus: +$sym%.2f (BDT ৳10.00)".format(state.oneAdRewardDisplay)
                            )
                        },
                        onOpenLevelPicker = { viewModel.setShowLevelPicker(true) }
                    )

                    // 2. Broadcast Ticker Strip right below the header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF8FAFC),
                                        Color(0xFFE0F2FE),
                                        Color(0xFFF8FAFC)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "📢", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentTicker,
                            color = Color(0xFF1E3A8A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 3. Center Interactive Bent-Arrow Puzzle Board
                    ArrowPuzzleBoard(
                        gridWidth = state.currentLevelData.gridWidth,
                        gridHeight = state.currentLevelData.gridHeight,
                        activeArrows = state.activeArrows,
                        exitingArrows = state.exitingArrows,
                        zoomScale = state.zoomScale,
                        showGridLines = state.showGridLines,
                        showTutorialBanner = !state.player.hasSeenTutorial,
                        tutorialBannerText = strings.tutorialBanner,
                        pinchToZoomHintText = strings.pinchToZoomHint,
                        onArrowTapped = { viewModel.onArrowTapped(it) },
                        onZoomChanged = { viewModel.onZoomChanged(it) },
                        onDismissTutorial = { viewModel.dismissTutorial() },
                        modifier = Modifier.weight(1f)
                    )

                    // 4. Bottom Blue Control Toolbar (Hint Bulb, Zoom Slider, Grid Toggle)
                    BottomGameToolbar(
                        hintsRemaining = state.player.hintsRemaining,
                        gridGuidesRemaining = state.player.gridGuidesRemaining,
                        zoomScale = state.zoomScale,
                        showGridLines = state.showGridLines,
                        onHintClick = { viewModel.onUseHint(context) },
                        onZoomChanged = { viewModel.onZoomChanged(it) },
                        onGridToggleClick = { viewModel.onToggleGridGuides(context) }
                    )

                    // 5. Official Google AdMob Test Banner Ad Bar at the bottom
                    AdMobBannerBar(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    )
                }

                // Red Screen-Edge Vignette Flash when hitting a blocked arrow
                AnimatedVisibility(
                    visible = state.redFlashAlpha > 0f,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(18.dp, Color(0xFFEF4444).copy(alpha = 0.55f))
                    )
                }

                // Task Center Modal
                if (state.showTaskCenter) {
                    TaskCenterModal(
                        strings = strings,
                        currencySymbol = sym,
                        isCareerTab = state.selectedTaskTabCareer,
                        dailyTasks = state.dailyTasks,
                        careerTasks = state.careerTasks,
                        onSelectTab = { viewModel.setTaskTabCareer(it) },
                        onTaskAction = { viewModel.onOpenTaskReward(context, it) },
                        onClose = { viewModel.setShowTaskCenter(false) }
                    )
                }

                // Settings & Multi-Language Modal (8 Countries & Languages)
                if (state.showSettingsDialog) {
                    SettingsAndLanguageDialog(
                        strings = strings,
                        currentRegion = state.countryRegion,
                        currentLanguage = state.language,
                        soundEnabled = state.player.soundEnabled,
                        vibrationEnabled = state.player.vibrationEnabled,
                        onToggleSound = { viewModel.toggleSound() },
                        onToggleVibration = { viewModel.toggleVibration() },
                        onSelectCountryRegion = { viewModel.selectCountryRegion(it) },
                        onSelectLanguage = { viewModel.selectLanguage(it) },
                        onOpenLevelPicker = {
                            viewModel.setShowSettings(false)
                            viewModel.setShowLevelPicker(true)
                        },
                        onRestartLevel = { viewModel.onRetryCurrentLevel() },
                        onClose = { viewModel.setShowSettings(false) }
                    )
                }

                // 300 Levels Selector Dialog with Lock / Unlock Progression
                if (state.showLevelPickerDialog) {
                    LevelPickerDialog(
                        currentLevel = state.currentLevelData.levelNumber,
                        maxUnlockedLevel = state.player.maxUnlockedLevel,
                        isBengali = state.language.code == "BN",
                        onSelectLevel = { viewModel.loadLevel(it) },
                        onClose = { viewModel.setShowLevelPicker(false) }
                    )
                }

                // Out of Lives Modal
                if (state.showOutOfLivesDialog) {
                    OutOfLivesDialog(
                        strings = strings,
                        onFreeRevive = { viewModel.onReviveLives(context) },
                        onTryAgain = { viewModel.onRetryCurrentLevel() }
                    )
                }

                // Reward Claim Overlay
                val rewardOverlay = state.rewardOverlay
                if (rewardOverlay != null) {
                    RewardClaimOverlay(
                        overlay = rewardOverlay,
                        strings = strings,
                        currencySymbol = sym,
                        onClaimFullWithAd = { viewModel.onClaimReward(context, claimFullWithAd = true) },
                        onClaimBaseOnly = { viewModel.onClaimReward(context, claimFullWithAd = false) }
                    )
                }

                // Flying Cash Bills Burst Overlay
                val burst = state.activeCashBurst
                if (burst != null) {
                    FlyingCashBurstOverlay(
                        burst = burst,
                        currencySymbol = sym
                    )
                }
            }
        }

        // Google AdMob Test Rewarded Video Overlay
        val pendingAd = state.pendingRewardedAd
        if (pendingAd != null) {
            AdMobRewardedVideoDialog(
                rewardDescription = pendingAd.description,
                onRewardEarnedAndClose = { viewModel.onFallbackRewardedAdCompleted() },
                onCancelEarly = { viewModel.onDismissRewardedAdEarly() }
            )
        }

        if (state.isSimulatingAd) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF93C5FD),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Dark Center Toast Notification
        val toastMsg = state.toastMessage
        if (toastMsg != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.82f))
                    .padding(horizontal = 22.dp, vertical = 16.dp)
                    .testTag("game_toast_box")
            ) {
                Text(
                    text = toastMsg,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TopGameHudHeader(
    state: GameUiState,
    onWithdrawClick: () -> Unit,
    onOpenTaskCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onWatchVideoAd: () -> Unit,
    onOpenLevelPicker: () -> Unit
) {
    val strings = state.strings
    val sym = state.currencySymbol
    val minWithdraw = state.countryRegion.convertFromBdt(5000.0)
    val needed = (minWithdraw - state.displayBalance).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF6094F8), Color(0xFF4F83F1))
                )
            )
            .border(width = 1.5.dp, color = Color(0xFF1D4ED8))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Row 1: Balance Pill + Green Withdraw Button + More Needed Info Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF2563EB).copy(alpha = 0.65f))
                    .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(50))
                    .padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        PinkCashStackIcon(size = 32.dp)
                        if (state.floatingBalanceDelta != null) {
                            Text(
                                text = state.floatingBalanceDelta,
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .offset(x = 4.dp, y = (-8).dp)
                                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$sym %,.2f".format(state.displayBalance),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        modifier = Modifier.testTag("hud_balance_text")
                    )
                }

                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                            )
                        )
                        .border(1.5.dp, Color(0xFF14532D), RoundedCornerShape(10.dp))
                        .clickable { onWithdrawClick() }
                        .padding(horizontal = 10.dp)
                        .testTag("hud_withdraw_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.withdrawBtn,
                        color = Color(0xFF052E16),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E40AF).copy(alpha = 0.75f))
                    .border(1.dp, Color(0xFF60A5FA), RoundedCornerShape(10.dp))
                    .clickable { onWithdrawClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = strings.moreToWithdrawTop(
                        "%,.2f".format(needed),
                        "%,.2f".format(minWithdraw)
                    ),
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Task Center + Settings + Watch Video Ad Button + 3 Hearts with Level Badge + Remaining Arrows Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Task Center Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF93C5FD), Color(0xFF3B82F6))
                            )
                        )
                        .border(1.5.dp, Color(0xFF1E3A8A), RoundedCornerShape(12.dp))
                        .clickable { onOpenTaskCenter() }
                        .testTag("hud_task_center_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = strings.taskCenterTitle,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(22.dp)
                    )
                    if (state.unclaimableCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(3.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }

                // Settings & Language Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF93C5FD), Color(0xFF3B82F6))
                            )
                        )
                        .border(1.5.dp, Color(0xFF1E3A8A), RoundedCornerShape(12.dp))
                        .clickable { onOpenSettings() }
                        .testTag("hud_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = strings.settingsTitle,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Dedicated "Watch Video Ad" Button (+15 Cash & Task Progress)
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFDE047), Color(0xFFFACC15))
                            )
                        )
                        .border(1.5.dp, Color(0xFFCA8A04), RoundedCornerShape(12.dp))
                        .clickable { onWatchVideoAd() }
                        .padding(horizontal = 8.dp)
                        .testTag("hud_watch_video_ad_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = "Watch Video Ad",
                            tint = Color(0xFF991B1B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+$sym%.2f".format(state.oneAdRewardDisplay),
                            color = Color(0xFF991B1B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Center: 3-Hearts Container with Floating "Level X" Badge on top
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .clickable { onOpenLevelPicker() }
                    .testTag("hud_level_hearts_box")
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFEF3C7))
                        .border(1.5.dp, Color(0xFF78350F), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..state.maxLives) {
                        CuteHeartIcon(
                            isFilled = i <= state.livesRemaining,
                            size = 24.dp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF93C5FD))
                        .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = strings.levelLabel(state.currentLevelData.levelNumber),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Right: Remaining Arrows Pill ("⬆ 05/05")
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1E40AF).copy(alpha = 0.78f))
                    .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("hud_arrows_counter"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⬆",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "%02d/%02d".format(state.activeArrows.size, state.totalArrowsInLevel),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun BottomGameToolbar(
    hintsRemaining: Int,
    gridGuidesRemaining: Int,
    zoomScale: Float,
    showGridLines: Boolean,
    onHintClick: () -> Unit,
    onZoomChanged: (Float) -> Unit,
    onGridToggleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF93C5FD), Color(0xFF60A5FA))
                )
            )
            .border(
                width = 2.dp,
                color = Color(0xFF1E3A8A),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Hint Lightbulb Button
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.25f))
                .clickable { onHintClick() }
                .testTag("bottom_hint_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Hint",
                tint = Color(0xFFFACC15),
                modifier = Modifier.size(32.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (hintsRemaining > 0) hintsRemaining.toString() else "📺",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Center: Zoom Slider ("- [slider] +")
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF60A5FA))
                .border(1.5.dp, Color(0xFF1D4ED8), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDBEAFE))
                    .clickable { onZoomChanged(zoomScale - 0.12f) },
                contentAlignment = Alignment.Center
            ) {
                Text("−", color = Color(0xFF1E3A8A), fontWeight = FontWeight.Black, fontSize = 15.sp)
            }

            Slider(
                value = zoomScale,
                onValueChange = { onZoomChanged(it) },
                valueRange = 0.65f..1.65f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .testTag("bottom_zoom_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFDBEAFE),
                    inactiveTrackColor = Color(0xFF3B82F6)
                )
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDBEAFE))
                    .clickable { onZoomChanged(zoomScale + 0.12f) },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color(0xFF1E3A8A), fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
        }

        // Right: Grid Alignment Lines Toggle Button ("#")
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (showGridLines) Color(0xFFFDE047).copy(alpha = 0.45f)
                    else Color.White.copy(alpha = 0.25f)
                )
                .clickable { onGridToggleClick() }
                .testTag("bottom_grid_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Grid Lines",
                tint = Color(0xFFFACC15),
                modifier = Modifier.size(30.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (gridGuidesRemaining > 0) gridGuidesRemaining.toString() else "📺",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
