package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_state")
data class PlayerStateEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 100.00,
    val currentLevel: Int = 1,
    val maxUnlockedLevel: Int = 1,
    val levelsCompletedCount: Int = 0,
    val dailyLevelsCompleted: Int = 0,
    val adsWatchedCount: Int = 0,
    val hintsRemaining: Int = 3,
    val gridGuidesRemaining: Int = 3,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val languageBengali: Boolean = false,
    val languageCode: String = "EN",
    val countryCode: String = "US",
    val hasAutoDetectedRegion: Boolean = false,
    val hasShownInstallBonus: Boolean = false,
    val hasSeenTutorial: Boolean = false,
    val savedPaymentMethod: String = "PAYPAL",
    val savedAccountNumber: String = "",
    val savedAccountName: String = "",
    // User Auth & Profile
    val isUserLoggedIn: Boolean = false,
    val userId: String = "",
    val userFullName: String = "",
    val userEmailOrPhone: String = "",
    val userPasswordHash: String = "",
    // App-to-Web Integrated Server & Firebase URLs
    val webServerUrl: String = GameRepository.WEB_ADMIN_SERVER_URL,
    val firebaseDbUrl: String = GameRepository.DEFAULT_FIREBASE_DB_URL,
    val appPackageName: String = GameRepository.APP_PACKAGE_NAME,
    // Admin Panel Controlled Ads Settings (AdMob, Unity Ads, Facebook Audience Network + ON/OFF)
    val adsEnabled: Boolean = true,
    val bannerAdsEnabled: Boolean = true,
    val rewardedAdsEnabled: Boolean = true,
    val activeAdNetwork: String = "ADMOB", // "ADMOB", "UNITY", "FACEBOOK"
    val admobAppId: String = "ca-app-pub-3940256099942544~3347511713",
    val admobBannerId: String = "ca-app-pub-3940256099942544/6300978111",
    val admobRewardedId: String = "ca-app-pub-3940256099942544/5224354917",
    val admobInterstitialId: String = "ca-app-pub-3940256099942544/1033173712",
    val unityGameId: String = "4089461",
    val unityBannerId: String = "Banner_Android",
    val unityRewardedId: String = "Rewarded_Android",
    val fbAppId: String = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID",
    val fbBannerId: String = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID",
    val fbRewardedId: String = "VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID",
    // Admin Panel Controlled Minimum Withdrawal & Tiers (in Base BDT)
    val minWithdrawBdt: Double = 5000.0,
    val withdrawTier1Bdt: Double = 5000.0,
    val withdrawTier2Bdt: Double = 10000.0,
    val withdrawTier3Bdt: Double = 20000.0,
    val withdrawTier4Bdt: Double = 30000.0,
    val perAdRewardBdt: Double = 10.0,
    val levelClearRewardBdt: Double = 20.0,
    // Admin Panel Controlled Daily Tasks
    val dailyTasksEnabled: Boolean = true,
    val taskLoginRewardBdt: Double = 25.0,
    val taskAd1RewardBdt: Double = 10.0,
    val taskLvl1RewardBdt: Double = 25.0,
    val taskLvl3RewardBdt: Double = 30.0,
    val taskLvl5RewardBdt: Double = 40.0,
    val taskLvl20RewardBdt: Double = 75.0,
    val taskAd3RewardBdt: Double = 30.0,
    val taskAd15RewardBdt: Double = 150.0,
    val taskAd30RewardBdt: Double = 300.0,
    val taskAd50RewardBdt: Double = 500.0
)

@Entity(tableName = "claimed_tasks")
data class ClaimedTaskEntity(
    @PrimaryKey val taskId: String,
    val claimedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawal_records")
data class WithdrawalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requestCode: String = "JAZ-100001",
    val userId: String = "",
    val userFullName: String = "",
    val userEmailOrPhone: String = "",
    val amount: Double,
    val localAmount: Double = amount,
    val currencySymbol: String = "৳",
    val countryCode: String = "BD",
    val method: String,
    val accountNumber: String,
    val accountName: String,
    val levelReached: Int = 1,
    val adsWatched: Int = 0,
    val status: String = "PENDING",
    val syncedToFirebase: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
