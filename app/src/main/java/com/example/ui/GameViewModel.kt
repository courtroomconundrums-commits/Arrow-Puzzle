package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ads.AdMobManager
import com.example.data.GameRepository
import com.example.data.PlayerStateEntity
import com.example.data.WithdrawalRecordEntity
import com.example.domain.AppLanguage
import com.example.domain.BentArrow
import com.example.domain.CountryRegion
import com.example.domain.LevelGenerator
import com.example.domain.LocalizedStrings
import com.example.domain.MobileBankingOption
import com.example.domain.PuzzleLevel
import com.example.domain.TaskItem
import com.example.domain.getStrings
import com.example.sound.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ActiveScreen {
    SPLASH,
    GAME,
    WITHDRAW
}

enum class PendingRewardVideoType {
    CLAIM_FULL_OVERLAY_REWARD,
    REVIVE_LIVES,
    REFILL_HINTS,
    REFILL_GRID_GUIDES,
    WATCH_TO_EARN_BONUS
}

data class PendingRewardedAdState(
    val type: PendingRewardVideoType,
    val description: String
)

data class RewardOverlayState(
    val isLevelComplete: Boolean,
    val fullAmount: Double,
    val baseAmount: Double,
    val fullAmountBdt: Double = fullAmount,
    val baseAmountBdt: Double = baseAmount,
    val taskId: String? = null
)

data class ExitingArrowAnim(
    val arrow: BentArrow,
    val startTimeMs: Long
)

data class FlyingCashBurst(
    val id: Long,
    val amount: Double,
    val showCenterCard: Boolean
)

data class GameUiState(
    val screen: ActiveScreen = ActiveScreen.SPLASH,
    val splashProgress: Int = 0,
    val player: PlayerStateEntity = PlayerStateEntity(),
    val countryRegion: CountryRegion = CountryRegion.GLOBAL_US,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val strings: LocalizedStrings = getStrings(AppLanguage.ENGLISH),
    val currentLevelData: PuzzleLevel = LevelGenerator.generateLevel(1),
    val activeArrows: List<BentArrow> = emptyList(),
    val totalArrowsInLevel: Int = 5,
    val exitingArrows: List<ExitingArrowAnim> = emptyList(),
    val livesRemaining: Int = 3,
    val maxLives: Int = 3,
    val zoomScale: Float = 1.0f,
    val showGridLines: Boolean = false,
    val redFlashAlpha: Float = 0f,
    val showTaskCenter: Boolean = false,
    val selectedTaskTabCareer: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showLevelPickerDialog: Boolean = false,
    val showOutOfLivesDialog: Boolean = false,
    val rewardOverlay: RewardOverlayState? = null,
    val pendingRewardedAd: PendingRewardedAdState? = null,
    val activeCashBurst: FlyingCashBurst? = null,
    val floatingBalanceDelta: String? = null,
    val toastMessage: String? = null,
    val isSimulatingAd: Boolean = false,
    // Stored in Base BDT (5000, 8000, 10000, 20000 BDT) and converted to local currency for display
    val selectedWithdrawTierBdt: Double = 5000.0,
    val showAccountInputModal: Boolean = false,
    val showWithdrawConfirmModal: Boolean = false,
    val tickerIndex: Int = 0,
    val dailyTasks: List<TaskItem> = emptyList(),
    val careerTasks: List<TaskItem> = emptyList(),
    val withdrawals: List<WithdrawalRecordEntity> = emptyList()
) {
    val unclaimableCount: Int
        get() = (dailyTasks + careerTasks).count { it.isCompleted && !it.isClaimed }

    val currencySymbol: String
        get() = countryRegion.currencySymbol

    /**
     * Player's balance converted from Base Bangladesh Taka (BDT ৳) to the active country's currency.
     */
    val displayBalance: Double
        get() = countryRegion.convertFromBdt(player.balance)

    /**
     * Selected withdrawal tier converted from Base BDT to the active country's currency.
     */
    val displaySelectedWithdrawTier: Double
        get() = countryRegion.convertFromBdt(selectedWithdrawTierBdt)

    /**
     * Reward for watching 1 Video Ad (Base = 10.00 BDT) converted to the active country's currency.
     * e.g. Bangladesh = ৳ 10.00, India = ₹ 7.10, English/US = $ 0.08, Indonesia = Rp 1,325.00
     */
    val oneAdRewardDisplay: Double
        get() = countryRegion.oneAdRewardLocal()
}

class GameViewModel(
    private val appContext: Context,
    private val repository: GameRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            var initialPlayer = repository.ensureInitialized()
            // Auto-detect country, language, currency, and local mobile banking on first launch
            if (!initialPlayer.hasAutoDetectedRegion) {
                val detectedRegion = CountryRegion.detectFromDevice(appContext)
                val defaultMethodId = detectedRegion.mobileBankingOptions.first().id
                repository.updateState { state ->
                    state.copy(
                        countryCode = detectedRegion.countryCode,
                        languageCode = detectedRegion.defaultLanguageCode,
                        languageBengali = (detectedRegion.defaultLanguageCode == "BN"),
                        savedPaymentMethod = defaultMethodId,
                        hasAutoDetectedRegion = true
                    )
                }
                initialPlayer = repository.ensureInitialized()
            }

            loadLevelInternal(initialPlayer.currentLevel)

            combine(
                repository.playerStateFlow,
                repository.claimedTasksFlow,
                repository.withdrawalsFlow
            ) { playerOpt, claimedList, withdrawals ->
                Triple(playerOpt ?: PlayerStateEntity(), claimedList, withdrawals)
            }.collect { (player, claimedList, withdrawals) ->
                val region = CountryRegion.fromCountryCode(player.countryCode)
                val lang = AppLanguage.fromCode(player.languageCode)
                val loc = getStrings(lang)
                val claimedSet = claimedList.map { it.taskId }.toSet()
                val daily = buildDailyTasks(player, claimedSet, loc, region)
                val career = buildCareerTasks(player, claimedSet, loc, region)
                val currentLoadedLevel = _uiState.value.currentLevelData.levelNumber
                if (currentLoadedLevel != player.currentLevel &&
                    _uiState.value.activeArrows.isEmpty() &&
                    _uiState.value.rewardOverlay == null
                ) {
                    loadLevelInternal(player.currentLevel)
                }
                _uiState.update { state ->
                    val updatedOverlay = state.rewardOverlay?.let { ov ->
                        ov.copy(
                            fullAmount = region.convertFromBdt(ov.fullAmountBdt),
                            baseAmount = region.convertFromBdt(ov.baseAmountBdt)
                        )
                    }
                    state.copy(
                        player = player,
                        countryRegion = region,
                        language = lang,
                        strings = loc,
                        dailyTasks = daily,
                        careerTasks = career,
                        withdrawals = withdrawals,
                        rewardOverlay = updatedOverlay
                    )
                }
            }
        }

        // Splash screen progress animation (0% -> 100%)
        viewModelScope.launch {
            val steps = listOf(9, 16, 18, 36, 68, 96, 100)
            for (pct in steps) {
                delay(160L)
                _uiState.update { it.copy(splashProgress = pct) }
            }
            delay(200L)
            _uiState.update { it.copy(screen = ActiveScreen.GAME) }

            // Show First App Install 100 Taka Bonus (converted to user's country currency) on first launch!
            val currentPlayer = repository.ensureInitialized()
            if (!currentPlayer.hasShownInstallBonus) {
                val region = CountryRegion.fromCountryCode(currentPlayer.countryCode)
                val sym = region.currencySymbol
                val installBonusLocal = region.convertFromBdt(100.00)
                repository.updateState { it.copy(hasShownInstallBonus = true) }
                soundManager.playArrowClearAndCash(
                    currentPlayer.soundEnabled,
                    currentPlayer.vibrationEnabled
                )
                val bonusToast = if (region == CountryRegion.BANGLADESH) {
                    "🎁 স্বাগতম বোনাস: +৳100.00 যোগ হয়েছে!"
                } else {
                    "🎁 Install Bonus (৳100 BDT): +$sym%.2f Added!".format(installBonusLocal)
                }
                showToast(bonusToast)
            }
        }

        // Broadcast ticker loop
        viewModelScope.launch {
            while (true) {
                delay(3500L)
                _uiState.update { state ->
                    val count = state.strings.broadcastMessages.size.coerceAtLeast(1)
                    state.copy(tickerIndex = (state.tickerIndex + 1) % count)
                }
            }
        }
    }

    fun skipSplash() {
        _uiState.update { it.copy(splashProgress = 100, screen = ActiveScreen.GAME) }
    }

    fun dismissTutorial() {
        viewModelScope.launch {
            repository.updateState { it.copy(hasSeenTutorial = true) }
        }
    }

    /**
     * Switches the user's CountryRegion, automatically updating the country's default
     * language, currency conversion rate from BDT, and local mobile banking withdrawal methods.
     */
    fun selectCountryRegion(region: CountryRegion, alsoSwitchLanguage: Boolean = true) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        viewModelScope.launch {
            repository.updateState { current ->
                val newLangCode = if (alsoSwitchLanguage) region.defaultLanguageCode else current.languageCode
                val firstMethod = region.mobileBankingOptions.first().id
                current.copy(
                    countryCode = region.countryCode,
                    languageCode = newLangCode,
                    languageBengali = (newLangCode == "BN"),
                    savedPaymentMethod = firstMethod
                )
            }
            val methodsSummary = region.mobileBankingOptions.joinToString(", ") { it.displayName }
            showToast("${region.flagEmoji} ${region.oneAdConversionBadge()} • $methodsSummary")
        }
    }

    /**
     * Switches language and also syncs the corresponding CountryRegion so currency icon
     * (e.g. English -> $, Bengali -> ৳, Hindi -> ₹) and BDT currency conversion update immediately.
     */
    fun selectLanguage(lang: AppLanguage) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        val matchedRegion = CountryRegion.fromLanguage(lang)
        viewModelScope.launch {
            repository.updateState { current ->
                val firstMethod = matchedRegion.mobileBankingOptions.first().id
                current.copy(
                    countryCode = matchedRegion.countryCode,
                    languageCode = lang.code,
                    languageBengali = (lang == AppLanguage.BENGALI),
                    savedPaymentMethod = firstMethod
                )
            }
            showToast("${lang.flagEmoji} ${lang.nativeName} (${matchedRegion.currencySymbol}) • ${matchedRegion.oneAdConversionBadge()}")
        }
    }

    fun loadLevel(levelNumber: Int) {
        val state = _uiState.value
        val safeLevel = levelNumber.coerceIn(1, LevelGenerator.TOTAL_LEVELS)
        if (safeLevel > state.player.maxUnlockedLevel) {
            soundManager.playBlockedError(state.player.soundEnabled, state.player.vibrationEnabled)
            val prevLvl = state.player.maxUnlockedLevel
            val msg = if (state.language == AppLanguage.BENGALI) {
                "লেভেল $safeLevel লক করা আছে! আনলক করতে আগে লেভেল $prevLvl সম্পন্ন করুন।"
            } else {
                "Level $safeLevel is locked! Complete Level $prevLvl first to unlock."
            }
            showToast(msg)
            return
        }

        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        viewModelScope.launch {
            repository.updateState {
                it.copy(currentLevel = safeLevel)
            }
            loadLevelInternal(safeLevel)
            _uiState.update {
                it.copy(
                    showLevelPickerDialog = false,
                    showSettingsDialog = false,
                    showOutOfLivesDialog = false,
                    rewardOverlay = null
                )
            }
        }
    }

    private fun loadLevelInternal(levelNumber: Int) {
        val puzzle = LevelGenerator.generateLevel(levelNumber)
        _uiState.update { state ->
            state.copy(
                currentLevelData = puzzle,
                activeArrows = puzzle.arrows,
                totalArrowsInLevel = puzzle.arrows.size,
                exitingArrows = emptyList(),
                livesRemaining = 3,
                showOutOfLivesDialog = false,
                zoomScale = 1.0f
            )
        }
    }

    fun onArrowTapped(arrowId: Int) {
        val state = _uiState.value
        if (state.showOutOfLivesDialog || state.rewardOverlay != null || state.pendingRewardedAd != null) return
        val arrow = state.activeArrows.find { it.id == arrowId } ?: return

        if (!state.player.hasSeenTutorial) {
            dismissTutorial()
        }

        val isUnblocked = LevelGenerator.isArrowUnblocked(
            arrow = arrow,
            allArrows = state.activeArrows,
            gridWidth = state.currentLevelData.gridWidth,
            gridHeight = state.currentLevelData.gridHeight
        )

        if (isUnblocked) {
            soundManager.playArrowClearAndCash(
                state.player.soundEnabled,
                state.player.vibrationEnabled
            )
            val remainingArrows = state.activeArrows.filterNot { it.id == arrowId }
            val anim = ExitingArrowAnim(arrow = arrow, startTimeMs = System.currentTimeMillis())

            _uiState.update {
                it.copy(
                    activeArrows = remainingArrows,
                    exitingArrows = it.exitingArrows + anim
                )
            }

            viewModelScope.launch {
                delay(500L)
                _uiState.update { st ->
                    st.copy(
                        exitingArrows = st.exitingArrows.filterNot { a -> a.arrow.id == arrowId }
                    )
                }

                if (remainingArrows.isEmpty()) {
                    onLevelCleared()
                }
            }
        } else {
            soundManager.playBlockedError(
                state.player.soundEnabled,
                state.player.vibrationEnabled
            )
            val newLives = (state.livesRemaining - 1).coerceAtLeast(0)
            val updatedArrows = state.activeArrows.map {
                if (it.id == arrowId) it.copy(isFailedAttempt = true, isHintHighlighted = false) else it
            }
            _uiState.update {
                it.copy(
                    activeArrows = updatedArrows,
                    livesRemaining = newLives,
                    redFlashAlpha = 0.75f,
                    showOutOfLivesDialog = newLives == 0
                )
            }
            viewModelScope.launch {
                delay(450L)
                _uiState.update { it.copy(redFlashAlpha = 0f) }
            }
        }
    }

    private fun onLevelCleared() {
        val state = _uiState.value
        val region = state.countryRegion
        soundManager.playLevelWin(state.player.soundEnabled, state.player.vibrationEnabled)
        // Level complete video ad reward = 20.00 BDT (৳ 20.00), Next Level without ad = 0.00 BDT
        val fullBonusBdt = 20.00
        val baseBonusBdt = 0.0
        val nextLevel = (state.currentLevelData.levelNumber + 1).coerceAtMost(LevelGenerator.TOTAL_LEVELS)

        viewModelScope.launch {
            repository.updateState { p ->
                p.copy(
                    currentLevel = nextLevel,
                    maxUnlockedLevel = maxOf(p.maxUnlockedLevel, nextLevel),
                    levelsCompletedCount = p.levelsCompletedCount + 1,
                    dailyLevelsCompleted = p.dailyLevelsCompleted + 1
                )
            }
            _uiState.update {
                it.copy(
                    rewardOverlay = RewardOverlayState(
                        isLevelComplete = true,
                        fullAmount = region.convertFromBdt(fullBonusBdt),
                        baseAmount = 0.0,
                        fullAmountBdt = fullBonusBdt,
                        baseAmountBdt = baseBonusBdt,
                        taskId = null
                    )
                )
            }
        }
    }

    fun triggerRewardedVideoAd(
        context: Context,
        type: PendingRewardVideoType,
        description: String
    ) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        AdMobManager.showRewardedAd(
            context = context,
            onRewardEarned = {
                completeRewardedVideoAction(type)
            },
            onAdClosed = {},
            onFallbackOverlayNeeded = {
                _uiState.update {
                    it.copy(
                        pendingRewardedAd = PendingRewardedAdState(
                            type = type,
                            description = description
                        )
                    )
                }
            }
        )
    }

    fun onDismissRewardedAdEarly() {
        _uiState.update { it.copy(pendingRewardedAd = null) }
        val state = _uiState.value
        showToast(
            if (state.language == AppLanguage.BENGALI) "পুরস্কার পেতে সম্পূর্ণ ভিডিও দেখুন!"
            else "Watch the full video to earn the reward!"
        )
    }

    fun onFallbackRewardedAdCompleted() {
        val pending = _uiState.value.pendingRewardedAd ?: return
        _uiState.update { it.copy(pendingRewardedAd = null) }
        completeRewardedVideoAction(pending.type)
    }

    private fun completeRewardedVideoAction(type: PendingRewardVideoType) {
        val state = _uiState.value
        val region = state.countryRegion
        val sym = state.currencySymbol
        viewModelScope.launch {
            repository.updateState { p -> p.copy(adsWatchedCount = p.adsWatchedCount + 1) }

            when (type) {
                PendingRewardVideoType.CLAIM_FULL_OVERLAY_REWARD -> {
                    val overlay = _uiState.value.rewardOverlay ?: return@launch
                    val amountToCreditBdt = overlay.fullAmountBdt
                    val displayAmount = region.convertFromBdt(amountToCreditBdt)
                    if (overlay.taskId != null) {
                        repository.markTaskClaimed(overlay.taskId, amountToCreditBdt)
                    } else {
                        repository.updateState { p -> p.copy(balance = p.balance + amountToCreditBdt) }
                    }
                    if (overlay.isLevelComplete) {
                        val nextLvl = repository.ensureInitialized().currentLevel
                        loadLevelInternal(nextLvl)
                    }
                    soundManager.playArrowClearAndCash(
                        state.player.soundEnabled,
                        state.player.vibrationEnabled
                    )
                    _uiState.update {
                        it.copy(
                            rewardOverlay = null,
                            activeCashBurst = FlyingCashBurst(
                                id = System.currentTimeMillis(),
                                amount = displayAmount,
                                showCenterCard = true
                            ),
                            floatingBalanceDelta = "+$sym %.2f".format(displayAmount)
                        )
                    }
                    delay(1450L)
                    _uiState.update {
                        it.copy(activeCashBurst = null, floatingBalanceDelta = null)
                    }
                }

                PendingRewardVideoType.REVIVE_LIVES -> {
                    soundManager.playArrowClearAndCash(
                        state.player.soundEnabled,
                        state.player.vibrationEnabled
                    )
                    _uiState.update { st ->
                        st.copy(
                            livesRemaining = 3,
                            showOutOfLivesDialog = false,
                            activeArrows = st.activeArrows.map { it.copy(isFailedAttempt = false) }
                        )
                    }
                }

                PendingRewardVideoType.REFILL_HINTS -> {
                    repository.updateState { it.copy(hintsRemaining = it.hintsRemaining + 3) }
                    showToast("+3 💡 Hints Added!")
                }

                PendingRewardVideoType.REFILL_GRID_GUIDES -> {
                    repository.updateState { it.copy(gridGuidesRemaining = it.gridGuidesRemaining + 3) }
                    _uiState.update { it.copy(showGridLines = true) }
                    showToast("+3 # Grid Guides Added!")
                }

                PendingRewardVideoType.WATCH_TO_EARN_BONUS -> {
                    // Base reward for 1 Video Ad = 10.00 BDT (৳ 10.00)
                    // Converted to user's country currency (e.g. ₹ 7.10 in India, $ 0.08 in USD, Rp 1,325 in Indonesia)
                    val bonusBdt = CountryRegion.BASE_AD_REWARD_BDT
                    val displayBonus = region.convertFromBdt(bonusBdt)
                    repository.updateState { p -> p.copy(balance = p.balance + bonusBdt) }
                    soundManager.playArrowClearAndCash(
                        state.player.soundEnabled,
                        state.player.vibrationEnabled
                    )
                    _uiState.update {
                        it.copy(
                            activeCashBurst = FlyingCashBurst(
                                id = System.currentTimeMillis(),
                                amount = displayBonus,
                                showCenterCard = true
                            ),
                            floatingBalanceDelta = "+$sym %.2f".format(displayBonus)
                        )
                    }
                    delay(1450L)
                    _uiState.update {
                        it.copy(activeCashBurst = null, floatingBalanceDelta = null)
                    }
                }
            }
        }
    }

    fun onUseHint(context: Context) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        if (state.activeArrows.isEmpty()) return

        if (state.player.hintsRemaining <= 0) {
            triggerRewardedVideoAd(
                context = context,
                type = PendingRewardVideoType.REFILL_HINTS,
                description = "Watch Test Video Ad for +3 Hints 💡"
            )
            return
        }

        val unblocked = state.activeArrows.firstOrNull { arrow ->
            LevelGenerator.isArrowUnblocked(
                arrow = arrow,
                allArrows = state.activeArrows,
                gridWidth = state.currentLevelData.gridWidth,
                gridHeight = state.currentLevelData.gridHeight
            )
        } ?: state.activeArrows.first()

        _uiState.update { st ->
            st.copy(
                activeArrows = st.activeArrows.map { a ->
                    if (a.id == unblocked.id) {
                        a.copy(isHintHighlighted = true, isFailedAttempt = false)
                    } else {
                        a.copy(isHintHighlighted = false)
                    }
                }
            )
        }
        viewModelScope.launch {
            repository.updateState { p ->
                p.copy(hintsRemaining = (p.hintsRemaining - 1).coerceAtLeast(0))
            }
        }
    }

    fun onToggleGridGuides(context: Context) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        val enabling = !state.showGridLines
        if (enabling && state.player.gridGuidesRemaining <= 0) {
            triggerRewardedVideoAd(
                context = context,
                type = PendingRewardVideoType.REFILL_GRID_GUIDES,
                description = "Watch Test Video Ad for +3 Grid Guides #"
            )
            return
        } else if (enabling) {
            viewModelScope.launch {
                repository.updateState {
                    it.copy(gridGuidesRemaining = (it.gridGuidesRemaining - 1).coerceAtLeast(0))
                }
            }
        }
        _uiState.update { it.copy(showGridLines = enabling) }
    }

    fun onZoomChanged(newZoom: Float) {
        _uiState.update { it.copy(zoomScale = newZoom.coerceIn(0.65f, 1.65f)) }
    }

    fun onReviveLives(context: Context) {
        triggerRewardedVideoAd(
            context = context,
            type = PendingRewardVideoType.REVIVE_LIVES,
            description = _uiState.value.strings.freeReviveBtn
        )
    }

    fun onRetryCurrentLevel() {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        loadLevelInternal(state.currentLevelData.levelNumber)
    }

    fun setShowTaskCenter(visible: Boolean) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        _uiState.update { it.copy(showTaskCenter = visible) }
    }

    fun setTaskTabCareer(isCareer: Boolean) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        _uiState.update { it.copy(selectedTaskTabCareer = isCareer) }
    }

    fun onOpenTaskReward(context: Context, task: TaskItem) {
        val state = _uiState.value
        val region = state.countryRegion
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        if (!task.isCompleted) {
            if (task.id.startsWith("daily_ad_")) {
                val adBonusLocal = region.oneAdRewardLocal()
                triggerRewardedVideoAd(
                    context = context,
                    type = PendingRewardVideoType.WATCH_TO_EARN_BONUS,
                    description = "${task.titleBn} (+${state.currencySymbol}%.2f)".format(adBonusLocal)
                )
            }
            return
        }
        if (task.isClaimed) return
        val fullBdt = task.rewardAmountBdt
        val baseBdt = fullBdt / 2.0
        _uiState.update {
            it.copy(
                showTaskCenter = false,
                rewardOverlay = RewardOverlayState(
                    isLevelComplete = false,
                    fullAmount = region.convertFromBdt(fullBdt),
                    baseAmount = region.convertFromBdt(baseBdt),
                    fullAmountBdt = fullBdt,
                    baseAmountBdt = baseBdt,
                    taskId = task.id
                )
            )
        }
    }

    fun onClaimReward(context: Context, claimFullWithAd: Boolean) {
        val state = _uiState.value
        val region = state.countryRegion
        val overlay = state.rewardOverlay ?: return
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)

        if (claimFullWithAd) {
            triggerRewardedVideoAd(
                context = context,
                type = PendingRewardVideoType.CLAIM_FULL_OVERLAY_REWARD,
                description = "+${state.currencySymbol} %.2f".format(overlay.fullAmount)
            )
            return
        }

        viewModelScope.launch {
            // If Level Complete and user clicks "Next Level" (claimFullWithAd = false):
            // No money is earned (0.0), just advance to the Next Level!
            if (overlay.isLevelComplete) {
                val nextLvl = repository.ensureInitialized().currentLevel
                loadLevelInternal(nextLvl)
                _uiState.update { it.copy(rewardOverlay = null) }
                return@launch
            }

            val amountToCreditBdt = overlay.baseAmountBdt
            val displayAmount = region.convertFromBdt(amountToCreditBdt)
            if (overlay.taskId != null) {
                repository.markTaskClaimed(overlay.taskId, amountToCreditBdt)
            } else if (amountToCreditBdt > 0.0) {
                repository.updateState { p -> p.copy(balance = p.balance + amountToCreditBdt) }
            }

            soundManager.playArrowClearAndCash(
                state.player.soundEnabled,
                state.player.vibrationEnabled
            )
            val sym = state.currencySymbol

            _uiState.update {
                it.copy(
                    rewardOverlay = null,
                    activeCashBurst = FlyingCashBurst(
                        id = System.currentTimeMillis(),
                        amount = displayAmount,
                        showCenterCard = true
                    ),
                    floatingBalanceDelta = "+$sym %.2f".format(displayAmount)
                )
            }
            delay(1450L)
            _uiState.update {
                it.copy(
                    activeCashBurst = null,
                    floatingBalanceDelta = null
                )
            }
        }
    }

    fun navigateToWithdraw() {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        _uiState.update { it.copy(screen = ActiveScreen.WITHDRAW) }
    }

    fun navigateBackToGame() {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        _uiState.update {
            it.copy(
                screen = ActiveScreen.GAME,
                showAccountInputModal = false,
                showWithdrawConfirmModal = false
            )
        }
    }

    fun selectWithdrawTierBdt(tierBdtAmount: Double) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        _uiState.update { it.copy(selectedWithdrawTierBdt = tierBdtAmount) }
    }

    fun openAccountInputModal() {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        _uiState.update { it.copy(showAccountInputModal = true) }
    }

    fun closeWithdrawModals() {
        _uiState.update {
            it.copy(
                showAccountInputModal = false,
                showWithdrawConfirmModal = false
            )
        }
    }

    fun saveAccountDetails(option: MobileBankingOption, accountInput: String, nameInput: String) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        val trimmedAccount = accountInput.trim()
        val trimmedName = nameInput.trim()

        val digitsOnly = trimmedAccount.all { it.isDigit() || it == '+' || it == '-' } && trimmedAccount.length in 8..15
        val isUpiOrEmail = (trimmedAccount.contains("@") || trimmedAccount.contains(".")) && trimmedAccount.length >= 5

        if (!digitsOnly && !isUpiOrEmail) {
            showToast(state.strings.invalidAccountToast)
            return
        }
        if (trimmedName.isEmpty()) {
            showToast(state.strings.nameInputPlaceholder)
            return
        }

        viewModelScope.launch {
            repository.updateState { p ->
                p.copy(
                    savedPaymentMethod = option.id,
                    savedAccountNumber = trimmedAccount,
                    savedAccountName = trimmedName
                )
            }
            _uiState.update {
                it.copy(
                    showAccountInputModal = false,
                    showWithdrawConfirmModal = true
                )
            }
        }
    }

    fun confirmWithdrawalRequest() {
        val state = _uiState.value
        val region = state.countryRegion
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        val tierBdt = state.selectedWithdrawTierBdt
        val currentBalBdt = state.player.balance
        if (currentBalBdt < tierBdt) {
            val neededLocal = region.convertFromBdt(tierBdt - currentBalBdt)
            _uiState.update { it.copy(showWithdrawConfirmModal = false) }
            showToast(state.strings.insufficientBalanceToast("%,.2f".format(neededLocal)))
            return
        }

        val tierLocal = region.convertFromBdt(tierBdt)
        viewModelScope.launch {
            repository.recordWithdrawal(
                amount = tierBdt,
                method = state.player.savedPaymentMethod,
                accountNumber = state.player.savedAccountNumber,
                accountName = state.player.savedAccountName
            )
            _uiState.update { it.copy(showWithdrawConfirmModal = false) }
            showToast(state.strings.withdrawSubmittedToast("%,.2f".format(tierLocal)))
        }
    }

    fun setShowSettings(visible: Boolean) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        _uiState.update { it.copy(showSettingsDialog = visible) }
    }

    fun setShowLevelPicker(visible: Boolean) {
        val state = _uiState.value
        soundManager.playClick(state.player.soundEnabled, state.player.vibrationEnabled)
        _uiState.update { it.copy(showLevelPickerDialog = visible) }
    }

    fun toggleSound() {
        viewModelScope.launch {
            repository.updateState { it.copy(soundEnabled = !it.soundEnabled) }
        }
    }

    fun toggleVibration() {
        viewModelScope.launch {
            repository.updateState { it.copy(vibrationEnabled = !it.vibrationEnabled) }
        }
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(toastMessage = message) }
            delay(2400L)
            _uiState.update { st ->
                if (st.toastMessage == message) st.copy(toastMessage = null) else st
            }
        }
    }

    private fun buildDailyTasks(
        player: PlayerStateEntity,
        claimedIds: Set<String>,
        loc: LocalizedStrings,
        region: CountryRegion
    ): List<TaskItem> {
        fun makeTask(
            id: String,
            title: String,
            current: Int,
            target: Int,
            bdtReward: Double
        ): TaskItem = TaskItem(
            id = id,
            isCareer = false,
            titleBn = title,
            titleEn = title,
            currentProgress = current.coerceAtMost(target),
            targetProgress = target,
            rewardAmount = region.convertFromBdt(bdtReward),
            rewardAmountBdt = bdtReward,
            isClaimed = id in claimedIds
        )

        return listOf(
            makeTask("daily_login", loc.dailyLoginTask, 1, 1, 25.00),
            makeTask("daily_ad_1", loc.watchAdsTask(1), player.adsWatchedCount, 1, 10.00),
            makeTask("daily_lvl_1", loc.completeLevelsTask(1), player.dailyLevelsCompleted, 1, 25.00),
            makeTask("daily_lvl_3", loc.completeLevelsTask(3), player.dailyLevelsCompleted, 3, 30.00),
            makeTask("daily_lvl_5", loc.completeLevelsTask(5), player.dailyLevelsCompleted, 5, 40.00),
            makeTask("daily_lvl_20", loc.completeLevelsTask(20), player.dailyLevelsCompleted, 20, 75.00),
            makeTask("daily_ad_3", loc.watchAdsTask(3), player.adsWatchedCount, 3, 30.00),
            makeTask("daily_ad_15", loc.watchAdsTask(15), player.adsWatchedCount, 15, 150.00),
            makeTask("daily_ad_30", loc.watchAdsTask(30), player.adsWatchedCount, 30, 300.00),
            makeTask("daily_ad_50", loc.watchAdsTask(50), player.adsWatchedCount, 50, 500.00)
        )
    }

    private fun buildCareerTasks(
        player: PlayerStateEntity,
        claimedIds: Set<String>,
        loc: LocalizedStrings,
        region: CountryRegion
    ): List<TaskItem> {
        val milestones = listOf(
            1 to 25.00,
            3 to 25.00,
            5 to 25.00,
            10 to 30.00,
            20 to 30.00,
            30 to 30.00,
            50 to 35.00,
            80 to 35.00,
            100 to 35.00,
            150 to 40.00,
            200 to 50.00,
            300 to 100.00
        )
        return milestones.map { (target, bdtReward) ->
            val id = "career_lvl_$target"
            TaskItem(
                id = id,
                isCareer = true,
                titleBn = loc.completeLevelsTask(target),
                titleEn = loc.completeLevelsTask(target),
                currentProgress = player.levelsCompletedCount.coerceAtMost(target),
                targetProgress = target,
                rewardAmount = region.convertFromBdt(bdtReward),
                rewardAmountBdt = bdtReward,
                isClaimed = id in claimedIds
            )
        }
    }

    class Factory(
        private val appContext: Context,
        private val repository: GameRepository,
        private val soundManager: SoundManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(appContext, repository, soundManager) as T
        }
    }
}
