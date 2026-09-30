package com.example.data

import com.example.ads.AdMobManager
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GameRepository(private val dao: GameDao) {
    companion object {
        // =========================================================================================
        // [CONFIGURED]: APP-TO-WEB SERVER URL & FIREBASE REALTIME DATABASE CONFIGURATION
        // =========================================================================================
        const val WEB_ADMIN_SERVER_URL = "https://cashpuzzle.free.je/index.php"
        const val WEB_ADMIN_SERVER_ALT_URL = "https://cashpuzzle.free.je/admin-panel/index.php"

        const val DEFAULT_FIREBASE_DB_URL = "https://cashpuzzle-default-rtdb.firebaseio.com"
        const val FIREBASE_API_KEY = "AIzaSyDASsTjtkxGmJCwU3-1IUwT_a2Hsa9vdDc"
        const val FIREBASE_AUTH_DOMAIN = "cashpuzzle.firebaseapp.com"
        const val FIREBASE_PROJECT_ID = "cashpuzzle"
        const val FIREBASE_STORAGE_BUCKET = "cashpuzzle.firebasestorage.app"
        const val FIREBASE_MESSAGING_SENDER_ID = "433504025632"
        const val FIREBASE_APP_ID = "1:433504025632:web:f43153bf00c8210ea75045"
        const val FIREBASE_MEASUREMENT_ID = "G-F10LV901PP"

        const val APP_PACKAGE_NAME = "com.jazstudio.com"
    }

    val playerStateFlow: Flow<PlayerStateEntity?> = dao.getPlayerState()
    val claimedTasksFlow: Flow<List<ClaimedTaskEntity>> = dao.getClaimedTasks()
    val withdrawalsFlow: Flow<List<WithdrawalRecordEntity>> = dao.getWithdrawals()

    suspend fun ensureInitialized(): PlayerStateEntity {
        val existing = dao.getPlayerStateOnce()
        if (existing != null) {
            val needsUrlUpgrade = existing.firebaseDbUrl.isBlank() ||
                existing.firebaseDbUrl.contains("jaz-cash-arrow-puzzle") ||
                existing.webServerUrl.isBlank() ||
                existing.webServerUrl.contains("your-domain.com")
            val normalized = if (needsUrlUpgrade) {
                val upgraded = existing.copy(
                    firebaseDbUrl = DEFAULT_FIREBASE_DB_URL,
                    webServerUrl = WEB_ADMIN_SERVER_URL,
                    appPackageName = APP_PACKAGE_NAME
                )
                dao.upsertPlayerState(upgraded)
                upgraded
            } else {
                existing
            }
            AdMobManager.applyAdminAdConfig(normalized)
            return normalized
        }
        val initial = PlayerStateEntity()
        dao.upsertPlayerState(initial)
        AdMobManager.applyAdminAdConfig(initial)
        return initial
    }

    suspend fun updateState(transform: (PlayerStateEntity) -> PlayerStateEntity) {
        val current = ensureInitialized()
        val updated = transform(current)
        dao.upsertPlayerState(updated)
        AdMobManager.applyAdminAdConfig(updated)
    }

    /**
     * Fetches live Ads Configuration (AdMob / Unity / Facebook + ON/OFF), Daily Task Rewards,
     * and Minimum Withdrawal Tiers from the Web Admin PHP Server (by App Package Name)
     * and Firebase Realtime Database (/app_settings.json).
     */
    suspend fun syncRemoteAdminSettings(): PlayerStateEntity {
        val current = ensureInitialized()
        val pkg = current.appPackageName.ifBlank { APP_PACKAGE_NAME }
        val webUrl = current.webServerUrl.ifBlank { WEB_ADMIN_SERVER_URL }.trim()
        val fbUrl = current.firebaseDbUrl.ifBlank { DEFAULT_FIREBASE_DB_URL }.trimEnd('/')

        val remoteJson: JSONObject? = withContext(Dispatchers.IO) {
            // 1. Try App-to-Web PHP Admin Server API first (?api=get_config&package_name=...)
            val candidateUrls = listOf(webUrl, WEB_ADMIN_SERVER_ALT_URL).distinct()
            for (candidate in candidateUrls) {
                val fromWeb = try {
                    val sep = if (candidate.contains("?")) "&" else "?"
                    val conn = (URL("${candidate}${sep}api=get_config&package_name=$pkg").openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 5000
                        readTimeout = 5000
                        setRequestProperty("X-App-Package", pkg)
                    }
                    val body = if (conn.responseCode in 200..299) {
                        conn.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        null
                    }
                    conn.disconnect()
                    if (!body.isNullOrBlank() && body.trim().startsWith("{")) JSONObject(body) else null
                } catch (_: Throwable) {
                    null
                }

                if (fromWeb != null && fromWeb.optBoolean("package_verified", true)) {
                    return@withContext fromWeb
                }
            }

            // 2. Fallback to Firebase Realtime Database (/app_settings.json)
            try {
                val conn = (URL("$fbUrl/app_settings.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                val body = if (conn.responseCode in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
                conn.disconnect()
                if (!body.isNullOrBlank() && body != "null" && body.trim().startsWith("{")) {
                    val obj = JSONObject(body)
                    val allowedPkg = obj.optString("app_package_name", pkg)
                    if (allowedPkg.isBlank() || allowedPkg == pkg) obj else null
                } else {
                    null
                }
            } catch (_: Throwable) {
                null
            }
        }

        if (remoteJson == null) return current

        val updated = current.copy(
            adsEnabled = remoteJson.optBoolean("ads_enabled", current.adsEnabled),
            bannerAdsEnabled = remoteJson.optBoolean("banner_ads_enabled", current.bannerAdsEnabled),
            rewardedAdsEnabled = remoteJson.optBoolean("rewarded_ads_enabled", current.rewardedAdsEnabled),
            activeAdNetwork = remoteJson.optString("active_ad_network", current.activeAdNetwork).uppercase(),
            admobAppId = remoteJson.optString("admob_app_id", current.admobAppId),
            admobBannerId = remoteJson.optString("admob_banner_id", current.admobBannerId),
            admobRewardedId = remoteJson.optString("admob_rewarded_id", current.admobRewardedId),
            admobInterstitialId = remoteJson.optString("admob_interstitial_id", current.admobInterstitialId),
            unityGameId = remoteJson.optString("unity_game_id", current.unityGameId),
            unityBannerId = remoteJson.optString("unity_banner_id", current.unityBannerId),
            unityRewardedId = remoteJson.optString("unity_rewarded_id", current.unityRewardedId),
            fbAppId = remoteJson.optString("fb_app_id", current.fbAppId),
            fbBannerId = remoteJson.optString("fb_banner_id", current.fbBannerId),
            fbRewardedId = remoteJson.optString("fb_rewarded_id", current.fbRewardedId),
            minWithdrawBdt = remoteJson.optDouble("min_withdraw_bdt", current.minWithdrawBdt).coerceAtLeast(10.0),
            withdrawTier1Bdt = remoteJson.optDouble("withdraw_tier1_bdt", current.withdrawTier1Bdt).coerceAtLeast(10.0),
            withdrawTier2Bdt = remoteJson.optDouble("withdraw_tier2_bdt", current.withdrawTier2Bdt).coerceAtLeast(10.0),
            withdrawTier3Bdt = remoteJson.optDouble("withdraw_tier3_bdt", current.withdrawTier3Bdt).coerceAtLeast(10.0),
            withdrawTier4Bdt = remoteJson.optDouble("withdraw_tier4_bdt", current.withdrawTier4Bdt).coerceAtLeast(10.0),
            perAdRewardBdt = remoteJson.optDouble("per_ad_reward_bdt", current.perAdRewardBdt).coerceAtLeast(0.0),
            levelClearRewardBdt = remoteJson.optDouble("level_clear_reward_bdt", current.levelClearRewardBdt).coerceAtLeast(0.0),
            dailyTasksEnabled = remoteJson.optBoolean("daily_tasks_enabled", current.dailyTasksEnabled),
            taskLoginRewardBdt = remoteJson.optDouble("task_login_reward_bdt", current.taskLoginRewardBdt),
            taskAd1RewardBdt = remoteJson.optDouble("task_ad1_reward_bdt", current.taskAd1RewardBdt),
            taskLvl1RewardBdt = remoteJson.optDouble("task_lvl1_reward_bdt", current.taskLvl1RewardBdt),
            taskLvl3RewardBdt = remoteJson.optDouble("task_lvl3_reward_bdt", current.taskLvl3RewardBdt),
            taskLvl5RewardBdt = remoteJson.optDouble("task_lvl5_reward_bdt", current.taskLvl5RewardBdt),
            taskLvl20RewardBdt = remoteJson.optDouble("task_lvl20_reward_bdt", current.taskLvl20RewardBdt),
            taskAd3RewardBdt = remoteJson.optDouble("task_ad3_reward_bdt", current.taskAd3RewardBdt),
            taskAd15RewardBdt = remoteJson.optDouble("task_ad15_reward_bdt", current.taskAd15RewardBdt),
            taskAd30RewardBdt = remoteJson.optDouble("task_ad30_reward_bdt", current.taskAd30RewardBdt),
            taskAd50RewardBdt = remoteJson.optDouble("task_ad50_reward_bdt", current.taskAd50RewardBdt)
        )
        dao.upsertPlayerState(updated)
        AdMobManager.applyAdminAdConfig(updated)
        return updated
    }

    suspend fun markTaskClaimed(taskId: String, rewardAmount: Double) {
        dao.insertClaimedTask(ClaimedTaskEntity(taskId = taskId))
        updateState { state ->
            state.copy(balance = state.balance + rewardAmount)
        }
        val current = ensureInitialized()
        if (current.isUserLoggedIn && current.userId.isNotBlank()) {
            syncUserToFirebase(current)
        }
    }

    fun hashPassword(rawPassword: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(rawPassword.trim().toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Throwable) {
            rawPassword.trim()
        }
    }

    fun buildUserIdFromIdentifier(emailOrPhone: String): String {
        val clean = emailOrPhone.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
        return if (clean.isNotEmpty()) "USR-${clean.take(18).uppercase()}" else "USR-${(100000..999999).random()}"
    }

    /**
     * Registers a new user account in Room and syncs it to both Web Admin Server and Firebase Realtime Database.
     */
    suspend fun registerUser(
        fullName: String,
        emailOrPhone: String,
        rawPassword: String
    ): PlayerStateEntity {
        val cleanName = fullName.trim()
        val cleanIdentifier = emailOrPhone.trim()
        val pwdHash = hashPassword(rawPassword)
        val generatedUserId = buildUserIdFromIdentifier(cleanIdentifier)

        val current = ensureInitialized()
        val updated = current.copy(
            isUserLoggedIn = true,
            userId = generatedUserId,
            userFullName = cleanName,
            userEmailOrPhone = cleanIdentifier,
            userPasswordHash = pwdHash,
            savedAccountName = if (current.savedAccountName.isBlank()) cleanName else current.savedAccountName,
            savedAccountNumber = if (current.savedAccountNumber.isBlank() && cleanIdentifier.all { it.isDigit() || it == '+' }) {
                cleanIdentifier
            } else {
                current.savedAccountNumber
            }
        )
        dao.upsertPlayerState(updated)
        syncUserToFirebase(updated)
        return updated
    }

    /**
     * Logs in an existing user by verifying against Firebase / Web Server or local saved credentials.
     */
    suspend fun loginUser(
        emailOrPhone: String,
        rawPassword: String
    ): Result<PlayerStateEntity> {
        val cleanIdentifier = emailOrPhone.trim()
        val pwdHash = hashPassword(rawPassword)
        val candidateUserId = buildUserIdFromIdentifier(cleanIdentifier)
        val current = ensureInitialized()
        val dbUrl = current.firebaseDbUrl.ifBlank { DEFAULT_FIREBASE_DB_URL }.trimEnd('/')

        val remoteUserJson = withContext(Dispatchers.IO) {
            try {
                val url = URL("$dbUrl/users/$candidateUserId.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                val code = conn.responseCode
                val body = if (code in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
                conn.disconnect()
                if (!body.isNullOrBlank() && body != "null") JSONObject(body) else null
            } catch (_: Throwable) {
                null
            }
        }

        if (remoteUserJson != null) {
            val remoteHash = remoteUserJson.optString("passwordHash", "")
            if (remoteHash.isNotEmpty() && remoteHash != pwdHash) {
                return Result.failure(IllegalArgumentException("INVALID_PASSWORD"))
            }
            val remoteName = remoteUserJson.optString("fullName", current.userFullName.ifBlank { "Player" })
            val remoteBalance = remoteUserJson.optDouble("balanceBdt", current.balance)
            val remoteMaxLevel = remoteUserJson.optInt("levelReached", current.maxUnlockedLevel)
            val updated = current.copy(
                isUserLoggedIn = true,
                userId = candidateUserId,
                userFullName = remoteName,
                userEmailOrPhone = cleanIdentifier,
                userPasswordHash = pwdHash,
                balance = maxOf(current.balance, remoteBalance),
                maxUnlockedLevel = maxOf(current.maxUnlockedLevel, remoteMaxLevel),
                savedAccountName = if (current.savedAccountName.isBlank()) remoteName else current.savedAccountName
            )
            dao.upsertPlayerState(updated)
            syncUserToFirebase(updated)
            return Result.success(updated)
        }

        if (current.userEmailOrPhone.equals(cleanIdentifier, ignoreCase = true) &&
            current.userPasswordHash == pwdHash
        ) {
            val updated = current.copy(isUserLoggedIn = true)
            dao.upsertPlayerState(updated)
            syncUserToFirebase(updated)
            return Result.success(updated)
        }

        if (current.userEmailOrPhone.equals(cleanIdentifier, ignoreCase = true) &&
            current.userPasswordHash.isNotEmpty() &&
            current.userPasswordHash != pwdHash
        ) {
            return Result.failure(IllegalArgumentException("INVALID_PASSWORD"))
        }

        return Result.failure(IllegalArgumentException("USER_NOT_FOUND"))
    }

    suspend fun logoutUser() {
        updateState { state ->
            state.copy(isUserLoggedIn = false)
        }
    }

    /**
     * Updates the logged-in user's profile (Full Name, Mobile/Email, Saved Wallet Account, and optional New Password)
     * in Room Database and syncs immediately to Firebase Realtime Database & Web Admin Server.
     */
    suspend fun updateUserProfile(
        fullName: String,
        emailOrPhone: String,
        walletAccount: String,
        newRawPassword: String
    ): PlayerStateEntity {
        val cleanName = fullName.trim()
        val cleanIdentifier = emailOrPhone.trim()
        val cleanWallet = walletAccount.trim()
        val current = ensureInitialized()
        val finalUserId = if (current.userId.isNotBlank()) {
            current.userId
        } else {
            buildUserIdFromIdentifier(cleanIdentifier)
        }
        val finalPasswordHash = if (newRawPassword.trim().length >= 4) {
            hashPassword(newRawPassword.trim())
        } else {
            current.userPasswordHash
        }

        val updated = current.copy(
            isUserLoggedIn = true,
            userId = finalUserId,
            userFullName = cleanName,
            userEmailOrPhone = cleanIdentifier,
            userPasswordHash = finalPasswordHash,
            savedAccountName = cleanName,
            savedAccountNumber = if (cleanWallet.isNotBlank()) cleanWallet else current.savedAccountNumber
        )
        dao.upsertPlayerState(updated)
        syncUserToFirebase(updated)
        return updated
    }

    /**
     * Resets a user's forgotten password by verifying their registered Mobile Number or Email
     * against Firebase Realtime Database or local PlayerStateEntity, then saving the new password.
     */
    suspend fun resetUserPassword(
        emailOrPhone: String,
        newRawPassword: String
    ): Result<PlayerStateEntity> {
        val cleanIdentifier = emailOrPhone.trim()
        val newPwdHash = hashPassword(newRawPassword.trim())
        val candidateUserId = buildUserIdFromIdentifier(cleanIdentifier)
        val current = ensureInitialized()
        val dbUrl = current.firebaseDbUrl.ifBlank { DEFAULT_FIREBASE_DB_URL }.trimEnd('/')

        val remoteUserJson = withContext(Dispatchers.IO) {
            try {
                val url = URL("$dbUrl/users/$candidateUserId.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                val code = conn.responseCode
                val body = if (code in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
                conn.disconnect()
                if (!body.isNullOrBlank() && body != "null") JSONObject(body) else null
            } catch (_: Throwable) {
                null
            }
        }

        if (remoteUserJson != null) {
            val remoteName = remoteUserJson.optString("fullName", current.userFullName.ifBlank { "Player" })
            val remoteBalance = remoteUserJson.optDouble("balanceBdt", current.balance)
            val remoteMaxLevel = remoteUserJson.optInt("levelReached", current.maxUnlockedLevel)
            val updated = current.copy(
                isUserLoggedIn = true,
                userId = candidateUserId,
                userFullName = remoteName,
                userEmailOrPhone = cleanIdentifier,
                userPasswordHash = newPwdHash,
                balance = maxOf(current.balance, remoteBalance),
                maxUnlockedLevel = maxOf(current.maxUnlockedLevel, remoteMaxLevel)
            )
            dao.upsertPlayerState(updated)
            syncUserToFirebase(updated)
            return Result.success(updated)
        }

        if (current.userEmailOrPhone.equals(cleanIdentifier, ignoreCase = true) ||
            current.savedAccountNumber.equals(cleanIdentifier, ignoreCase = true)
        ) {
            val updated = current.copy(
                isUserLoggedIn = true,
                userId = current.userId.ifBlank { candidateUserId },
                userEmailOrPhone = cleanIdentifier,
                userPasswordHash = newPwdHash
            )
            dao.upsertPlayerState(updated)
            syncUserToFirebase(updated)
            return Result.success(updated)
        }

        return Result.failure(IllegalArgumentException("USER_NOT_FOUND"))
    }

    suspend fun recordWithdrawal(
        amountBdt: Double,
        localAmount: Double,
        currencySymbol: String,
        countryCode: String,
        method: String,
        accountNumber: String,
        accountName: String
    ): WithdrawalRecordEntity {
        val current = ensureInitialized()
        val code = "JAZ-${(100000..999999).random()}"
        val newBalance = (current.balance - amountBdt).coerceAtLeast(0.0)
        val record = WithdrawalRecordEntity(
            requestCode = code,
            userId = current.userId.ifBlank { "USR-GUEST" },
            userFullName = current.userFullName.ifBlank { accountName },
            userEmailOrPhone = current.userEmailOrPhone.ifBlank { accountNumber },
            amount = amountBdt,
            localAmount = localAmount,
            currencySymbol = currencySymbol,
            countryCode = countryCode,
            method = method,
            accountNumber = accountNumber,
            accountName = accountName,
            levelReached = current.maxUnlockedLevel,
            adsWatched = current.adsWatchedCount,
            status = "PENDING",
            syncedToFirebase = true
        )
        dao.insertWithdrawal(record)
        val updatedPlayer = current.copy(balance = newBalance)
        dao.upsertPlayerState(updatedPlayer)

        syncUserToFirebase(updatedPlayer)
        syncWithdrawalToFirebase(record, updatedPlayer)
        return record
    }

    suspend fun syncUserToFirebase(player: PlayerStateEntity) {
        if (player.userId.isBlank()) return
        withContext(Dispatchers.IO) {
            val pkg = player.appPackageName.ifBlank { APP_PACKAGE_NAME }
            val payload = JSONObject().apply {
                put("packageName", pkg)
                put("userId", player.userId)
                put("fullName", player.userFullName)
                put("emailOrPhone", player.userEmailOrPhone)
                put("passwordHash", player.userPasswordHash)
                put("countryCode", player.countryCode)
                put("balanceBdt", player.balance)
                put("levelReached", player.maxUnlockedLevel)
                put("levelsCompleted", player.levelsCompletedCount)
                put("adsWatched", player.adsWatchedCount)
                put("savedMethod", player.savedPaymentMethod)
                put("savedAccount", player.savedAccountNumber)
                put("updatedAt", System.currentTimeMillis())
            }

            // 1. Sync to Firebase Realtime Database
            try {
                val baseUrl = player.firebaseDbUrl.ifBlank { DEFAULT_FIREBASE_DB_URL }.trimEnd('/')
                val url = URL("$baseUrl/users/${player.userId}.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                }
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                }
                conn.responseCode
                conn.disconnect()
            } catch (_: Throwable) {
            }

            // 2. Also sync to Web Admin PHP Server API (?api=sync_user&package_name=...)
            try {
                val webUrl = player.webServerUrl.ifBlank { WEB_ADMIN_SERVER_URL }.trim()
                val sep = if (webUrl.contains("?")) "&" else "?"
                val conn = (URL("${webUrl}${sep}api=sync_user&package_name=$pkg").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("X-App-Package", pkg)
                }
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                }
                conn.responseCode
                conn.disconnect()
            } catch (_: Throwable) {
            }
        }
    }

    suspend fun syncWithdrawalToFirebase(record: WithdrawalRecordEntity, player: PlayerStateEntity) {
        withContext(Dispatchers.IO) {
            val pkg = player.appPackageName.ifBlank { APP_PACKAGE_NAME }
            val payload = JSONObject().apply {
                put("packageName", pkg)
                put("requestCode", record.requestCode)
                put("userId", record.userId)
                put("userFullName", record.userFullName)
                put("userEmailOrPhone", record.userEmailOrPhone)
                put("amountBdt", record.amount)
                put("localAmount", record.localAmount)
                put("currencySymbol", record.currencySymbol)
                put("countryCode", record.countryCode)
                put("method", record.method)
                put("accountNumber", record.accountNumber)
                put("accountName", record.accountName)
                put("levelReached", record.levelReached)
                put("adsWatched", record.adsWatched)
                put("status", record.status)
                put("timestamp", record.timestamp)
            }

            // 1. Sync to Firebase Realtime Database
            try {
                val baseUrl = player.firebaseDbUrl.ifBlank { DEFAULT_FIREBASE_DB_URL }.trimEnd('/')
                val url = URL("$baseUrl/withdrawals/${record.requestCode}.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                }
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                }
                conn.responseCode
                conn.disconnect()
            } catch (_: Throwable) {
            }

            // 2. Also sync to Web Admin PHP Server API (?api=submit_withdrawal&package_name=...)
            try {
                val webUrl = player.webServerUrl.ifBlank { WEB_ADMIN_SERVER_URL }.trim()
                val sep = if (webUrl.contains("?")) "&" else "?"
                val conn = (URL("${webUrl}${sep}api=submit_withdrawal&package_name=$pkg").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("X-App-Package", pkg)
                }
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                }
                conn.responseCode
                conn.disconnect()
            } catch (_: Throwable) {
            }
        }
    }

    /**
     * Refreshes both Admin Panel remote settings (Ads, Tasks, Min Withdrawal) AND
     * user withdrawal ticket statuses from Web Admin Server & Firebase Realtime Database.
     */
    suspend fun refreshWithdrawalStatusesFromFirebase(): Int {
        syncRemoteAdminSettings()
        val current = ensureInitialized()
        val baseUrl = current.firebaseDbUrl.ifBlank { DEFAULT_FIREBASE_DB_URL }.trimEnd('/')
        val webUrl = current.webServerUrl.ifBlank { WEB_ADMIN_SERVER_URL }.trim()
        val pkg = current.appPackageName.ifBlank { APP_PACKAGE_NAME }
        val localList = dao.getWithdrawalsOnce()
        if (localList.isEmpty()) return 0

        var updatedCount = 0
        withContext(Dispatchers.IO) {
            for (item in localList) {
                try {
                    // Check Web Admin Server first, then Firebase
                    var remoteStatus = ""
                    try {
                        val sep = if (webUrl.contains("?")) "&" else "?"
                        val wConn = (URL("${webUrl}${sep}api=ticket_status&package_name=$pkg&ticket=${item.requestCode}").openConnection() as HttpURLConnection).apply {
                            requestMethod = "GET"
                            connectTimeout = 4000
                            readTimeout = 4000
                        }
                        if (wConn.responseCode in 200..299) {
                            val wBody = wConn.inputStream.bufferedReader().use { it.readText() }
                            if (!wBody.isNullOrBlank() && wBody.trim().startsWith("{")) {
                                remoteStatus = JSONObject(wBody).optString("status", "").uppercase()
                            }
                        }
                        wConn.disconnect()
                    } catch (_: Throwable) {
                    }

                    if (remoteStatus.isBlank()) {
                        val url = URL("$baseUrl/withdrawals/${item.requestCode}.json")
                        val conn = (url.openConnection() as HttpURLConnection).apply {
                            requestMethod = "GET"
                            connectTimeout = 4000
                            readTimeout = 4000
                        }
                        if (conn.responseCode in 200..299) {
                            val body = conn.inputStream.bufferedReader().use { it.readText() }
                            if (!body.isNullOrBlank() && body != "null") {
                                val json = JSONObject(body)
                                remoteStatus = json.optString("status", item.status).uppercase()
                            }
                        }
                        conn.disconnect()
                    }

                    if (remoteStatus.isNotBlank() && remoteStatus != item.status) {
                        dao.updateWithdrawalStatus(item.id, remoteStatus)
                        if (remoteStatus == "REJECTED" && item.status != "REJECTED") {
                            val latestPlayer = ensureInitialized()
                            dao.upsertPlayerState(
                                latestPlayer.copy(balance = latestPlayer.balance + item.amount)
                            )
                        }
                        updatedCount++
                    }
                } catch (_: Throwable) {
                }
            }
        }
        return updatedCount
    }
}
