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
    val savedAccountName: String = ""
)

@Entity(tableName = "claimed_tasks")
data class ClaimedTaskEntity(
    @PrimaryKey val taskId: String,
    val claimedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawal_records")
data class WithdrawalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val method: String,
    val accountNumber: String,
    val accountName: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)
