package com.example.domain

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flagEmoji: String,
    val currencySymbol: String
) {
    ENGLISH("EN", "English (Global)", "United States", "🇺🇸", "$"),
    BENGALI("BN", "English (BD)", "Bangladesh", "🇧🇩", "৳"),
    INDONESIAN("ID", "English (ID)", "Indonesia", "🇮🇩", "Rp"),
    HINDI("HI", "English (IN)", "India", "🇮🇳", "₹"),
    URDU("UR", "English (PK)", "Pakistan", "🇵🇰", "Rs"),
    ARABIC("AR", "English (SA)", "Saudi Arabia", "🇸🇦", "SR"),
    SPANISH("ES", "English (ES)", "Spain / LatAm", "🇪🇸", "$"),
    PORTUGUESE("PT", "English (BR)", "Brazil", "🇧🇷", "R$");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
}

data class LocalizedStrings(
    val withdrawBtn: String,
    val moreToWithdrawTop: (String, String) -> String,
    val levelLabel: (Int) -> String,
    val tutorialBanner: String,
    val pinchToZoomHint: String,
    val taskCenterTitle: String,
    val dailyTasksTab: String,
    val careerTasksTab: String,
    val claimBtn: String,
    val incompleteBtn: String,
    val claimedBtn: String,
    val taskRewardTitle: String,
    val levelSuccessTitle: String,
    val watchAdToClaimBtn: String,
    val claimBaseOnlyBtn: (String) -> String,
    val nextLevelBtn: String,
    val outOfLivesTitle: String,
    val outOfLivesSubtitle: String,
    val freeReviveBtn: String,
    val tryAgainBtn: String,
    val continueHeader: String,
    val withdrawScreenTitle: String,
    val currentBalanceLabel: String,
    val withdrawTierSection: String,
    val withdrawInstructionsSection: String,
    val withdrawRuleText: (String) -> String,
    val withdrawNeededText: (String, String) -> String,
    val withdrawActionBtn: (String) -> String,
    val selectAccountMethodSubtitle: String,
    val accountInputPlaceholder: String,
    val nameInputPlaceholder: String,
    val submitBtn: String,
    val accountDetailsHeader: String,
    val accountFieldLabel: String,
    val nameFieldLabel: String,
    val confirmWithdrawHeader: String,
    val invalidAccountToast: String,
    val insufficientBalanceToast: (String) -> String,
    val withdrawSubmittedToast: (String) -> String,
    val settingsTitle: String,
    val soundLabel: String,
    val vibrationLabel: String,
    val languageLabel: String,
    val countryLabel: String,
    val broadcastMessages: List<String>,
    val noWithdrawUpdate: String = "No recent withdrawal updates.",
    val dailyLoginTask: String,
    val watchAdsTask: (Int) -> String,
    val completeLevelsTask: (Int) -> String
)

fun getStrings(language: AppLanguage): LocalizedStrings {
    val sym = language.currencySymbol
    return LocalizedStrings(
        withdrawBtn = "Withdrawal",
        moreToWithdrawTop = { needed, tier -> "Earn $sym$needed more to withdraw $sym$tier" },
        levelLabel = { lvl -> "Level $lvl" },
        tutorialBanner = "Tap an unblocked arrow to slide it off the board and earn cash rewards!",
        pinchToZoomHint = "Pinch with two fingers to zoom in or out",
        taskCenterTitle = "Task Center",
        dailyTasksTab = "Daily Tasks",
        careerTasksTab = "Career Tasks",
        claimBtn = "Claim",
        incompleteBtn = "Go",
        claimedBtn = "Claimed",
        taskRewardTitle = "Task Reward",
        levelSuccessTitle = "Level Complete!",
        watchAdToClaimBtn = "Watch Ad to Claim Full Reward",
        claimBaseOnlyBtn = { amt -> "Claim $sym$amt only" },
        nextLevelBtn = "Next Level",
        outOfLivesTitle = "Out of Lives!",
        outOfLivesSubtitle = "Watch a short video ad to revive all 3 hearts and continue playing!",
        freeReviveBtn = "Free Revive (Watch Ad)",
        tryAgainBtn = "Restart Level",
        continueHeader = "Continue?",
        withdrawScreenTitle = "Withdrawal",
        currentBalanceLabel = "Current Balance",
        withdrawTierSection = "Select Withdrawal Amount",
        withdrawInstructionsSection = "Withdrawal Instructions",
        withdrawRuleText = { tier ->
            "1. Minimum withdrawal amount is $sym$tier.\n" +
                "2. Select your preferred mobile wallet or payment gateway and enter a valid account number.\n" +
                "3. Requests are verified by Admin and paid within 24 hours."
        },
        withdrawNeededText = { tier, needed ->
            "Target: $sym$tier • Need $sym$needed more to withdraw"
        },
        withdrawActionBtn = { tier -> "Withdrawal $sym$tier Now" },
        selectAccountMethodSubtitle = "Select your preferred payment method & enter account details",
        accountInputPlaceholder = "Enter Account / Wallet Number",
        nameInputPlaceholder = "Enter Account Holder Full Name",
        submitBtn = "Save & Continue",
        accountDetailsHeader = "Account Details",
        accountFieldLabel = "Account / Wallet Number:",
        nameFieldLabel = "Account Holder Name:",
        confirmWithdrawHeader = "Confirm Withdrawal",
        invalidAccountToast = "Please enter a valid wallet/account number (8-15 digits or valid email/UPI)!",
        insufficientBalanceToast = { needed -> "Insufficient balance! You need $sym$needed more to withdraw." },
        withdrawSubmittedToast = { amt -> "Withdrawal request for $sym$amt submitted successfully!" },
        settingsTitle = "Settings & Region",
        soundLabel = "Sound Effects",
        vibrationLabel = "Vibration",
        languageLabel = "Select Region & Currency",
        countryLabel = "Country & Local Payment Gateway",
        broadcastMessages = listOf(
            "User 884***219 withdrew ${sym}5,000 via Mobile Wallet!",
            "User 912***405 just received ${sym}10,000 instant payout!",
            "User 730***882 completed Level 20 and claimed bonus cash!",
            "User 654***110 withdrew ${sym}5,000 successfully!"
        ),
        dailyLoginTask = "Daily Login Bonus",
        watchAdsTask = { count -> "Watch $count Video Ad${if (count > 1) "s" else ""}" },
        completeLevelsTask = { count -> "Complete $count Level${if (count > 1) "s" else ""}" }
    )
}
