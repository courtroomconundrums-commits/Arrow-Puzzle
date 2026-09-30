package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import com.example.data.PlayerStateEntity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdMobManager {
    // =========================================================================================
    // [REPLACEMENT NOTE: DEFAULT AD CODES (ADMOB / UNITY / FACEBOOK)]
    // You can configure AdMob, Unity Ads, and Facebook Audience Network Ad Unit IDs and
    // ON/OFF switches directly from the Web Admin Panel (admin-panel/index.php).
    // You can also set your default Real Ad IDs directly in the constants below:
    //
    // 1. Google AdMob Real IDs:
    //    App ID      : "ca-app-pub-5937358493599236~4244999709" (in AndroidManifest.xml)
    //    Banner ID   : "ca-app-pub-5937358493599236/5526291785"
    //    Rewarded ID : "ca-app-pub-5937358493599236/5979829767"
    //
    // 2. Unity Ads IDs:
    //    Game ID     : "4089461" (Set your Unity Game ID)
    //    Banner ID   : "Banner_Android"
    //    Rewarded ID : "Rewarded_Android"
    //
    // 3. Facebook (Meta) Audience Network IDs:
    //    Banner ID   : "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"
    //    Rewarded ID : "VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID"
    // =========================================================================================

    const val GOOGLE_OFFICIAL_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val GOOGLE_OFFICIAL_TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    const val GOOGLE_OFFICIAL_TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    // [REPLACEMENT]: Default AdMob Banner & Rewarded IDs (also dynamically controlled by Admin Panel)
    const val BANNER_AD_UNIT_ID = GOOGLE_OFFICIAL_TEST_BANNER_ID
    const val REWARDED_AD_UNIT_ID = GOOGLE_OFFICIAL_TEST_REWARDED_ID

    // [REPLACEMENT]: Default Unity Ads IDs (also dynamically controlled by Admin Panel)
    const val DEFAULT_UNITY_GAME_ID = "4089461"
    const val DEFAULT_UNITY_BANNER_ID = "Banner_Android"
    const val DEFAULT_UNITY_REWARDED_ID = "Rewarded_Android"

    // [REPLACEMENT]: Default Facebook Audience Network IDs (also dynamically controlled by Admin Panel)
    const val DEFAULT_FB_BANNER_ID = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"
    const val DEFAULT_FB_REWARDED_ID = "VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID"

    private val _isSdkInitialized = MutableStateFlow(false)
    val isSdkInitialized: StateFlow<Boolean> = _isSdkInitialized.asStateFlow()

    // Live Admin Panel Controlled Ad StateFlows
    private val _adsEnabled = MutableStateFlow(true)
    val adsEnabled: StateFlow<Boolean> = _adsEnabled.asStateFlow()

    private val _bannerAdsEnabled = MutableStateFlow(true)
    val bannerAdsEnabled: StateFlow<Boolean> = _bannerAdsEnabled.asStateFlow()

    private val _rewardedAdsEnabled = MutableStateFlow(true)
    val rewardedAdsEnabled: StateFlow<Boolean> = _rewardedAdsEnabled.asStateFlow()

    private val _activeAdNetwork = MutableStateFlow("ADMOB") // "ADMOB", "UNITY", "FACEBOOK"
    val activeAdNetwork: StateFlow<String> = _activeAdNetwork.asStateFlow()

    private val _admobBannerUnitId = MutableStateFlow(BANNER_AD_UNIT_ID)
    val admobBannerUnitId: StateFlow<String> = _admobBannerUnitId.asStateFlow()

    private val _admobRewardedUnitId = MutableStateFlow(REWARDED_AD_UNIT_ID)
    val admobRewardedUnitId: StateFlow<String> = _admobRewardedUnitId.asStateFlow()

    private val _unityGameId = MutableStateFlow(DEFAULT_UNITY_GAME_ID)
    val unityGameId: StateFlow<String> = _unityGameId.asStateFlow()

    private val _unityBannerId = MutableStateFlow(DEFAULT_UNITY_BANNER_ID)
    val unityBannerId: StateFlow<String> = _unityBannerId.asStateFlow()

    private val _unityRewardedId = MutableStateFlow(DEFAULT_UNITY_REWARDED_ID)
    val unityRewardedId: StateFlow<String> = _unityRewardedId.asStateFlow()

    private val _fbBannerId = MutableStateFlow(DEFAULT_FB_BANNER_ID)
    val fbBannerId: StateFlow<String> = _fbBannerId.asStateFlow()

    private val _fbRewardedId = MutableStateFlow(DEFAULT_FB_REWARDED_ID)
    val fbRewardedId: StateFlow<String> = _fbRewardedId.asStateFlow()

    private var rewardedAd: RewardedAd? = null
    private var backupInterstitialAd: InterstitialAd? = null
    private var isLoadingRewarded = false
    private var isLoadingInterstitial = false
    private var isInitializing = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingWaiters = mutableListOf<(RewardedAd?) -> Unit>()

    /**
     * Syncs the active ad network, ON/OFF switches, and Ad Unit IDs (AdMob / Unity / Facebook)
     * from the Admin Panel configuration stored in PlayerStateEntity.
     */
    fun applyAdminAdConfig(player: PlayerStateEntity) {
        val prevRewardedId = _admobRewardedUnitId.value
        _adsEnabled.value = player.adsEnabled
        _bannerAdsEnabled.value = player.bannerAdsEnabled
        _rewardedAdsEnabled.value = player.rewardedAdsEnabled
        _activeAdNetwork.value = player.activeAdNetwork.uppercase().ifBlank { "ADMOB" }
        _admobBannerUnitId.value = player.admobBannerId.ifBlank { BANNER_AD_UNIT_ID }
        _admobRewardedUnitId.value = player.admobRewardedId.ifBlank { REWARDED_AD_UNIT_ID }
        _unityGameId.value = player.unityGameId.ifBlank { DEFAULT_UNITY_GAME_ID }
        _unityBannerId.value = player.unityBannerId.ifBlank { DEFAULT_UNITY_BANNER_ID }
        _unityRewardedId.value = player.unityRewardedId.ifBlank { DEFAULT_UNITY_REWARDED_ID }
        _fbBannerId.value = player.fbBannerId.ifBlank { DEFAULT_FB_BANNER_ID }
        _fbRewardedId.value = player.fbRewardedId.ifBlank { DEFAULT_FB_REWARDED_ID }

        if (prevRewardedId != _admobRewardedUnitId.value) {
            rewardedAd = null
        }
    }

    fun initialize(context: Context) {
        val appCtx = context.applicationContext
        if (_isSdkInitialized.value) {
            if (_adsEnabled.value && _rewardedAdsEnabled.value && rewardedAd == null && !isLoadingRewarded) {
                loadRewardedAd(appCtx)
            }
            if (_adsEnabled.value && backupInterstitialAd == null && !isLoadingInterstitial) {
                loadBackupInterstitial(appCtx)
            }
            return
        }
        if (isInitializing) return
        isInitializing = true

        mainHandler.post {
            try {
                val requestConfig = RequestConfiguration.Builder().build()
                MobileAds.setRequestConfiguration(requestConfig)
                MobileAds.initialize(appCtx) {
                    mainHandler.post {
                        isInitializing = false
                        _isSdkInitialized.value = true
                        if (_adsEnabled.value && _rewardedAdsEnabled.value) {
                            loadRewardedAd(appCtx)
                            loadBackupInterstitial(appCtx)
                        }
                    }
                }
            } catch (_: Throwable) {
                isInitializing = false
                _isSdkInitialized.value = true
            }
        }
    }

    fun loadRewardedAd(context: Context, onLoadedCallback: ((RewardedAd?) -> Unit)? = null) {
        val appCtx = context.applicationContext
        mainHandler.post {
            val existing = rewardedAd
            if (existing != null) {
                onLoadedCallback?.invoke(existing)
                return@post
            }
            if (onLoadedCallback != null) {
                pendingWaiters.add(onLoadedCallback)
            }
            if (isLoadingRewarded) return@post
            isLoadingRewarded = true

            val currentUnitId = _admobRewardedUnitId.value.ifBlank { REWARDED_AD_UNIT_ID }
            loadRewardedWithUnitId(
                appCtx = appCtx,
                unitId = currentUnitId,
                allowFallbackToOfficialTest = currentUnitId != GOOGLE_OFFICIAL_TEST_REWARDED_ID
            )
        }
    }

    private fun loadRewardedWithUnitId(
        appCtx: Context,
        unitId: String,
        allowFallbackToOfficialTest: Boolean
    ) {
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                appCtx,
                unitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        mainHandler.post {
                            rewardedAd = ad
                            isLoadingRewarded = false
                            val waiters = pendingWaiters.toList()
                            pendingWaiters.clear()
                            waiters.forEach { it.invoke(ad) }
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        mainHandler.post {
                            if (allowFallbackToOfficialTest) {
                                loadRewardedWithUnitId(
                                    appCtx = appCtx,
                                    unitId = GOOGLE_OFFICIAL_TEST_REWARDED_ID,
                                    allowFallbackToOfficialTest = false
                                )
                            } else {
                                rewardedAd = null
                                isLoadingRewarded = false
                                val waiters = pendingWaiters.toList()
                                pendingWaiters.clear()
                                waiters.forEach { it.invoke(null) }
                            }
                        }
                    }
                }
            )
        } catch (_: Throwable) {
            isLoadingRewarded = false
            val waiters = pendingWaiters.toList()
            pendingWaiters.clear()
            waiters.forEach { it.invoke(null) }
        }
    }

    private fun loadBackupInterstitial(context: Context) {
        val appCtx = context.applicationContext
        if (isLoadingInterstitial || backupInterstitialAd != null) return
        isLoadingInterstitial = true
        mainHandler.post {
            try {
                InterstitialAd.load(
                    appCtx,
                    GOOGLE_OFFICIAL_TEST_INTERSTITIAL_ID,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            backupInterstitialAd = ad
                            isLoadingInterstitial = false
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            backupInterstitialAd = null
                            isLoadingInterstitial = false
                        }
                    }
                )
            } catch (_: Throwable) {
                isLoadingInterstitial = false
            }
        }
    }

    /**
     * Shows a Rewarded Video Ad according to the Admin Panel settings:
     * - If Ads or Rewarded Ads are turned OFF in Admin Panel, immediately calls onFallbackOverlayNeeded or grants reward.
     * - If active network is UNITY or FACEBOOK, opens the Unity Ads / Facebook Audience Network video ad player.
     * - If active network is ADMOB, shows native Google AdMob RewardedAd (with fallback).
     */
    fun showRewardedAd(
        context: Context,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit,
        onFallbackOverlayNeeded: () -> Unit,
        onLoadingStateChanged: (Boolean) -> Unit = {}
    ) {
        // If Admin Panel turned OFF ads or rewarded ads, grant reward directly without showing ad
        if (!_adsEnabled.value || !_rewardedAdsEnabled.value) {
            onRewardEarned()
            onAdClosed()
            return
        }

        // If Admin Panel selected UNITY or FACEBOOK network, present the Unity / Facebook video ad dialog
        val network = _activeAdNetwork.value
        if (network == "UNITY" || network == "FACEBOOK") {
            onFallbackOverlayNeeded()
            return
        }

        val activity = context.findActivity()
        if (activity == null) {
            onFallbackOverlayNeeded()
            return
        }

        val readyAd = rewardedAd
        if (readyAd != null) {
            presentRewardedAd(
                activity = activity,
                ad = readyAd,
                onRewardEarned = onRewardEarned,
                onAdClosed = onAdClosed,
                onFallbackOverlayNeeded = onFallbackOverlayNeeded
            )
            return
        }

        onLoadingStateChanged(true)
        initialize(activity)

        var handled = false
        val timeoutRunnable = Runnable {
            if (!handled) {
                handled = true
                onLoadingStateChanged(false)
                val backupInterstitial = backupInterstitialAd
                if (backupInterstitial != null && !activity.isFinishing && !activity.isDestroyed) {
                    presentBackupInterstitial(
                        activity = activity,
                        ad = backupInterstitial,
                        onRewardEarned = onRewardEarned,
                        onAdClosed = onAdClosed,
                        onFallbackOverlayNeeded = onFallbackOverlayNeeded
                    )
                } else {
                    onFallbackOverlayNeeded()
                }
            }
        }
        mainHandler.postDelayed(timeoutRunnable, 5500L)

        loadRewardedAd(activity) { loadedAd ->
            if (handled) return@loadRewardedAd
            handled = true
            mainHandler.removeCallbacks(timeoutRunnable)
            onLoadingStateChanged(false)

            if (loadedAd != null && !activity.isFinishing && !activity.isDestroyed) {
                presentRewardedAd(
                    activity = activity,
                    ad = loadedAd,
                    onRewardEarned = onRewardEarned,
                    onAdClosed = onAdClosed,
                    onFallbackOverlayNeeded = onFallbackOverlayNeeded
                )
            } else {
                val backupInterstitial = backupInterstitialAd
                if (backupInterstitial != null && !activity.isFinishing && !activity.isDestroyed) {
                    presentBackupInterstitial(
                        activity = activity,
                        ad = backupInterstitial,
                        onRewardEarned = onRewardEarned,
                        onAdClosed = onAdClosed,
                        onFallbackOverlayNeeded = onFallbackOverlayNeeded
                    )
                } else {
                    onFallbackOverlayNeeded()
                }
            }
        }
    }

    private fun presentRewardedAd(
        activity: Activity,
        ad: RewardedAd,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit,
        onFallbackOverlayNeeded: () -> Unit
    ) {
        var earned = false
        rewardedAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadRewardedAd(activity.applicationContext)
                if (earned) {
                    onRewardEarned()
                }
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                loadRewardedAd(activity.applicationContext)
                onFallbackOverlayNeeded()
            }
        }
        ad.show(activity) {
            earned = true
        }
    }

    private fun presentBackupInterstitial(
        activity: Activity,
        ad: InterstitialAd,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit,
        onFallbackOverlayNeeded: () -> Unit
    ) {
        backupInterstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadBackupInterstitial(activity.applicationContext)
                loadRewardedAd(activity.applicationContext)
                onRewardEarned()
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                loadBackupInterstitial(activity.applicationContext)
                onFallbackOverlayNeeded()
            }
        }
        ad.show(activity)
    }
}

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
