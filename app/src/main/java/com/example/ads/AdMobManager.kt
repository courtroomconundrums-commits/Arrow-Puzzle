package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {
    // =========================================================================================
    // [NOTE: ADMOB AD UNIT ID REPLACEMENT / রিয়েল অ্যাড আইডি বসানোর জায়গা]
    // বর্তমানে নিচে Google AdMob-এর Official TEST Ad Unit ID দেওয়া আছে।
    // অ্যাপ পাবলিশ করার সময় নিচের TEST ID-এর বদলে আপনার Real Ad Unit ID বসিয়ে দিন:
    //
    // আপনার Real Banner Ad Unit ID   : "ca-app-pub-5937358493599236/5526291785"
    // আপনার Real Rewarded Ad Unit ID : "ca-app-pub-5937358493599236/5979829767"
    // (এবং AndroidManifest.xml ফাইলে Real App ID: "ca-app-pub-5937358493599236~4244999709")
    // =========================================================================================

    // TODO: Replace this Test Banner ID with Real Banner ID: "ca-app-pub-5937358493599236/5526291785"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // TODO: Replace this Test Rewarded ID with Real Rewarded ID: "ca-app-pub-5937358493599236/5979829767"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    const val TEST_BANNER_AD_UNIT_ID = BANNER_AD_UNIT_ID
    const val TEST_REWARDED_AD_UNIT_ID = REWARDED_AD_UNIT_ID

    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    fun initialize(context: Context) {
        val appCtx = context.applicationContext
        if (!isInitialized) {
            isInitialized = true
            try {
                MobileAds.initialize(appCtx) {
                    mainHandler.post {
                        loadRewardedAd(appCtx)
                    }
                }
            } catch (_: Throwable) {
            }
        } else if (rewardedAd == null && !isLoadingRewarded) {
            loadRewardedAd(appCtx)
        }
    }

    fun loadRewardedAd(context: Context, onLoadedCallback: ((RewardedAd?) -> Unit)? = null) {
        val appCtx = context.applicationContext
        val existing = rewardedAd
        if (existing != null) {
            onLoadedCallback?.invoke(existing)
            return
        }
        if (isLoadingRewarded && onLoadedCallback == null) return
        isLoadingRewarded = true
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                appCtx,
                REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoadingRewarded = false
                        onLoadedCallback?.invoke(ad)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isLoadingRewarded = false
                        onLoadedCallback?.invoke(null)
                    }
                }
            )
        } catch (_: Throwable) {
            isLoadingRewarded = false
            onLoadedCallback?.invoke(null)
        }
    }

    /**
     * Shows a real Google AdMob RewardedAd (using Official Test ID by default).
     * If the ad isn't preloaded yet, loads it on-demand and displays it as soon as it arrives.
     * Only falls back to [onFallbackOverlayNeeded] if AdMob fails to load (e.g. no internet).
     */
    fun showRewardedAd(
        context: Context,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit,
        onFallbackOverlayNeeded: () -> Unit
    ) {
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
        } else {
            // Load on-demand and show the real AdMob Test Video Ad immediately when loaded
            loadRewardedAd(activity) { loadedAd ->
                if (loadedAd != null && !activity.isFinishing && !activity.isDestroyed) {
                    presentRewardedAd(
                        activity = activity,
                        ad = loadedAd,
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
}

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
