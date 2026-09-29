package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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

    fun initialize(context: Context) {
        if (!isInitialized) {
            isInitialized = true
            try {
                MobileAds.initialize(context.applicationContext) {
                    loadRewardedAd(context.applicationContext)
                }
            } catch (_: Throwable) {
            }
        } else if (rewardedAd == null && !isLoadingRewarded) {
            loadRewardedAd(context.applicationContext)
        }
    }

    fun loadRewardedAd(context: Context) {
        if (isLoadingRewarded || rewardedAd != null) return
        isLoadingRewarded = true
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context.applicationContext,
                TEST_REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoadingRewarded = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isLoadingRewarded = false
                    }
                }
            )
        } catch (_: Throwable) {
            isLoadingRewarded = false
        }
    }

    /**
     * Attempts to show a loaded Google AdMob Test RewardedAd.
     * If the native AdMob ad is still loading or unavailable in the current environment,
     * invokes [onFallbackOverlayNeeded] so the in-app AdMob Test Video Ad overlay is shown.
     */
    fun showRewardedAd(
        context: Context,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit,
        onFallbackOverlayNeeded: () -> Unit
    ) {
        val activity = context.findActivity()
        val ad = rewardedAd
        if (ad != null && activity != null) {
            var earned = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(context.applicationContext)
                    if (earned) {
                        onRewardEarned()
                    }
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd(context.applicationContext)
                    onFallbackOverlayNeeded()
                }
            }
            ad.show(activity) {
                earned = true
            }
        } else {
            loadRewardedAd(context.applicationContext)
            onFallbackOverlayNeeded()
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
