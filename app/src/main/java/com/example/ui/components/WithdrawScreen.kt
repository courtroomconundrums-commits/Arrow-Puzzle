package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WithdrawalRecordEntity
import com.example.domain.CountryRegion
import com.example.domain.LocalizedStrings
import com.example.domain.MobileBankingOption

@Composable
fun WithdrawScreen(
    balance: Double,
    selectedTierBdt: Double,
    tiersBdt: List<Double> = listOf(5000.0, 10000.0, 20000.0, 30000.0),
    minWithdrawBdt: Double = 5000.0,
    countryRegion: CountryRegion,
    savedPaymentMethodId: String,
    savedAccountNumber: String,
    savedAccountName: String,
    isUserLoggedIn: Boolean,
    userId: String,
    userFullName: String,
    userEmailOrPhone: String,
    strings: LocalizedStrings,
    currencySymbol: String,
    tickerText: String,
    showUserAuthModal: Boolean,
    showAccountModal: Boolean,
    showConfirmModal: Boolean,
    withdrawals: List<WithdrawalRecordEntity> = emptyList(),
    onSelectTierBdt: (Double) -> Unit,
    onSelectCountryRegion: (CountryRegion) -> Unit,
    onClickWithdrawButton: () -> Unit,
    onOpenUserAuthModal: () -> Unit,
    onRegisterUser: (String, String, String) -> Unit,
    onLoginUser: (String, String) -> Unit,
    onUpdateUserProfile: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onResetPassword: (String, String, String) -> Unit = { _, _, _ -> },
    onLogoutUser: () -> Unit,
    onRefreshWithdrawalStatuses: () -> Unit,
    onSaveAccountDetails: (MobileBankingOption, String, String) -> Unit,
    onConfirmWithdraw: () -> Unit,
    onCloseModals: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        if (showUserAuthModal || showAccountModal || showConfirmModal) {
            onCloseModals()
        } else {
            onBack()
        }
    }

    val safeTiersBdt = if (tiersBdt.size >= 4) tiersBdt else listOf(minWithdrawBdt, 10000.0, 20000.0, 30000.0)
    val tiers = safeTiersBdt.map { countryRegion.convertFromBdt(it) }
    val effectiveSelectedTierBdt = maxOf(selectedTierBdt, minWithdrawBdt)
    val selectedTier = countryRegion.convertFromBdt(effectiveSelectedTierBdt)
    val neededAmount = (selectedTier - balance).coerceAtLeast(0.0)
    val progressFraction = (balance / selectedTier.coerceAtLeast(0.01)).toFloat().coerceIn(0.04f, 1f)
    val options = countryRegion.mobileBankingOptions
    val currentOption = options.find { it.id == savedPaymentMethodId } ?: options.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF4F86F7), Color(0xFF3B73F0), Color(0xFF2563EB))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("withdraw_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: Back button, Title, Country Flag Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .clickable { onBack() }
                        .testTag("withdraw_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = strings.withdrawScreenTitle,
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black
                )

                // User Profile Button in Top Bar
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFACC15))
                        .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                        .clickable { onOpenUserAuthModal() }
                        .padding(horizontal = 10.dp)
                        .testTag("withdraw_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isUserLoggedIn) "👤 Profile" else "🔐 Login",
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Golden Balance Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFEF08A), Color(0xFFFDE047), Color(0xFFFACC15))
                        )
                    )
                    .border(2.5.dp, Color.White, RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFA855F7), Color(0xFF9333EA))
                                )
                            )
                            .border(1.5.dp, Color.White, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👛", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.currentBalanceLabel,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$currencySymbol %,.2f".format(balance),
                        color = Color(0xFFD97706),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Broadcast Ticker Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFDBEAFE))
                    .border(1.5.dp, Color.White, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1D4ED8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentOption.iconEmoji,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tickerText,
                    color = Color(0xFF1E3A8A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Withdrawal Tiers
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF60A5FA).copy(alpha = 0.55f))
                        .border(2.dp, Color(0xFF93C5FD), RoundedCornerShape(22.dp))
                        .padding(top = 26.dp, bottom = 16.dp, start = 12.dp, end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TierClipboardCard(
                            amount = tiers[0],
                            currencySymbol = currencySymbol,
                            isSelected = selectedTierBdt == safeTiersBdt[0],
                            onClick = { onSelectTierBdt(safeTiersBdt[0]) },
                            modifier = Modifier.weight(1f)
                        )
                        TierClipboardCard(
                            amount = tiers[1],
                            currencySymbol = currencySymbol,
                            isSelected = selectedTierBdt == safeTiersBdt[1],
                            onClick = { onSelectTierBdt(safeTiersBdt[1]) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TierClipboardCard(
                            amount = tiers[2],
                            currencySymbol = currencySymbol,
                            isSelected = selectedTierBdt == safeTiersBdt[2],
                            onClick = { onSelectTierBdt(safeTiersBdt[2]) },
                            modifier = Modifier.weight(1f)
                        )
                        TierClipboardCard(
                            amount = tiers[3],
                            currencySymbol = currencySymbol,
                            isSelected = selectedTierBdt == safeTiersBdt[3],
                            onClick = { onSelectTierBdt(safeTiersBdt[3]) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD), Color(0xFF38BDF8))
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(50))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.withdrawTierSection,
                        color = Color(0xFF0F172A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Withdrawal Instructions
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF2563EB))
                        .border(2.dp, Color(0xFF93C5FD), RoundedCornerShape(22.dp))
                        .padding(top = 28.dp, bottom = 18.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = strings.withdrawRuleText("%,.2f".format(selectedTier)),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD), Color(0xFF38BDF8))
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(50))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.withdrawInstructionsSection,
                        color = Color(0xFF0F172A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = strings.withdrawNeededText(
                    "%,.2f".format(selectedTier),
                    "%,.2f".format(neededAmount)
                ),
                color = Color(0xFFFDE047),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 18.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF1E3A8A))
                        .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(50))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFDE047), Color(0xFFFACC15))
                                )
                            )
                    )
                }
                CashWalletIcon(size = 48.dp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Green Withdraw Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                        )
                    )
                    .border(2.dp, Color(0xFFBBF7D0), RoundedCornerShape(18.dp))
                    .clickable { onClickWithdrawButton() }
                    .testTag("submit_withdraw_tier_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.withdrawActionBtn("%,.2f".format(selectedTier)),
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Account / Registration Status Card (Required before withdrawal)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF1E3A8A).copy(alpha = 0.9f))
                    .border(
                        width = 1.5.dp,
                        color = if (isUserLoggedIn) Color(0xFF22C55E) else Color(0xFFFDE047),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("user_account_status_card")
            ) {
                if (isUserLoggedIn && userId.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👤 $userFullName",
                                color = Color(0xFFFDE047),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "$userEmailOrPhone • ID: $userId",
                                color = Color(0xFFBAE6FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFACC15))
                                    .border(1.dp, Color.White, RoundedCornerShape(10.dp))
                                    .clickable { onOpenUserAuthModal() }
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                                    .testTag("edit_user_profile_button")
                            ) {
                                Text(
                                    text = "✏️ Edit Profile",
                                    color = Color(0xFF0F172A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2563EB))
                                    .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                                    .clickable { onRefreshWithdrawalStatuses() }
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                                    .testTag("sync_firebase_status_button")
                            ) {
                                Text(
                                    text = "🔄 Sync",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEF4444))
                                    .clickable { onLogoutUser() }
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                                    .testTag("user_logout_button")
                            ) {
                                Text(
                                    text = "Logout",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenUserAuthModal() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔐 User Account (Login / Register)",
                                color = Color(0xFFFDE047),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Register or Login to submit withdrawal requests",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFACC15))
                                .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                                .clickable { onOpenUserAuthModal() }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("open_user_auth_button")
                        ) {
                            Text(
                                text = "Register / Login",
                                color = Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // User Withdrawal History & Live Status Tracker
            if (withdrawals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1E3A8A).copy(alpha = 0.85f))
                        .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(18.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 Withdrawal History (${withdrawals.size})",
                            color = Color(0xFFFDE047),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2563EB))
                                .clickable { onRefreshWithdrawalStatuses() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔄 Refresh Status",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    for (rec in withdrawals.take(8)) {
                        val badgeColor = when (rec.status) {
                            "PAID", "APPROVED" -> Color(0xFF22C55E)
                            "REJECTED" -> Color(0xFFEF4444)
                            else -> Color(0xFFFACC15)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.75f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${rec.requestCode} • ${rec.currencySymbol}${"%,.2f".format(rec.localAmount)} (${rec.method})",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Acc: ${rec.accountNumber} (${rec.accountName})",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(badgeColor.copy(alpha = 0.2f))
                                    .border(1.dp, badgeColor, RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = rec.status,
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modal 0: User Profile (Edit Profile) & Registration / Login / Forgot Password Modal
        if (showUserAuthModal) {
            UserAuthModal(
                isUserLoggedIn = isUserLoggedIn,
                userId = userId,
                initialName = userFullName.ifBlank { savedAccountName },
                initialEmailOrPhone = userEmailOrPhone.ifBlank { savedAccountNumber },
                initialSavedWallet = savedAccountNumber,
                onRegister = onRegisterUser,
                onLogin = onLoginUser,
                onUpdateProfile = onUpdateUserProfile,
                onResetPassword = onResetPassword,
                onLogout = onLogoutUser,
                onClose = onCloseModals
            )
        }

        // Modal 1: Country-Specific Mobile Banking Account Input Modal
        if (showAccountModal) {
            AccountDetailsInputModal(
                countryRegion = countryRegion,
                initialOption = currentOption,
                initialAccount = savedAccountNumber.ifBlank {
                    if (userEmailOrPhone.all { it.isDigit() || it == '+' }) userEmailOrPhone else ""
                },
                initialName = savedAccountName.ifBlank { userFullName },
                strings = strings,
                onSelectCountryRegion = onSelectCountryRegion,
                onSubmit = onSaveAccountDetails,
                onClose = onCloseModals
            )
        }

        // Modal 2: Confirm Withdrawal Modal
        if (showConfirmModal) {
            WithdrawConfirmModal(
                option = currentOption,
                accountNumber = savedAccountNumber,
                accountName = savedAccountName,
                strings = strings,
                onConfirm = onConfirmWithdraw,
                onClose = onCloseModals
            )
        }
    }
}

@Composable
fun UserAuthModal(
    isUserLoggedIn: Boolean = false,
    userId: String = "",
    initialName: String = "",
    initialEmailOrPhone: String = "",
    initialSavedWallet: String = "",
    onRegister: (String, String, String) -> Unit,
    onLogin: (String, String) -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onResetPassword: (String, String, String) -> Unit = { _, _, _ -> },
    onLogout: () -> Unit = {},
    onClose: () -> Unit
) {
    // Mode: 0 = Register, 1 = Login, 2 = Forgot Password, 3 = Edit Profile (when logged in)
    var authTab by remember(isUserLoggedIn) { mutableStateOf(if (isUserLoggedIn) 3 else 1) }
    var fullName by remember(initialName) { mutableStateOf(initialName) }
    var emailOrPhone by remember(initialEmailOrPhone) { mutableStateOf(initialEmailOrPhone) }
    var savedWallet by remember(initialSavedWallet) { mutableStateOf(initialSavedWallet) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .clickable(enabled = true, onClick = {})
            .padding(horizontal = 16.dp)
            .testTag("user_auth_modal"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF60A5FA))
                    .border(3.dp, Color(0xFF2563EB), RoundedCornerShape(26.dp))
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .verticalScroll(rememberScrollState())
                    .padding(top = 30.dp, bottom = 18.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isUserLoggedIn && authTab == 3) {
                    // ==================== EDITABLE USER PROFILE VIEW ====================
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                )
                            )
                            .border(2.5.dp, Color(0xFFFDE047), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullName.trim().take(1).uppercase().ifEmpty { "👤" },
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (userId.isNotBlank()) "User ID: $userId • Verified Account" else "Verified Player Profile",
                        color = Color(0xFF15803D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Edit your profile details, wallet number, or change your password below",
                        color = Color(0xFF475569),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "👤 Full Name",
                        color = Color(0xFF1E3A8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFDBEAFE))
                            .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                            .testTag("profile_full_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📱 Mobile Number or Email",
                        color = Color(0xFF1E3A8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFDBEAFE))
                            .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                            .testTag("profile_email_phone_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "💳 Default Withdrawal Wallet Number",
                        color = Color(0xFF1E3A8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = savedWallet,
                        onValueChange = { savedWallet = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFDBEAFE))
                            .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                            .testTag("profile_wallet_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "🔑 New Password (leave blank to keep current)",
                        color = Color(0xFF1E3A8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFDBEAFE))
                            .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                            .testTag("profile_new_password_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Save Profile Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                                )
                            )
                            .border(2.dp, Color(0xFF15803D), RoundedCornerShape(14.dp))
                            .clickable {
                                onUpdateProfile(fullName, emailOrPhone, savedWallet, password)
                            }
                            .testTag("save_user_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💾 Save Profile Changes",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Logout Button inside Profile
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEE2E2))
                            .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                            .clickable {
                                onLogout()
                                onClose()
                            }
                            .testTag("profile_logout_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🚪 Logout Account",
                            color = Color(0xFFB91C1C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    // ==================== REGISTER / LOGIN / FORGOT PASSWORD TABS ====================
                    Text(
                        text = when (authTab) {
                            0 -> "Create a new account to withdraw your earnings"
                            1 -> "Sign in with your registered mobile number/email and password"
                            else -> "Forgot your password? Verify your registered mobile/email to set a new password"
                        },
                        color = Color(0xFF1E3A8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Tabs: Login | Register | Forgot Password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFDBEAFE))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (authTab == 1) Color(0xFF2563EB) else Color.Transparent)
                                .clickable { authTab = 1 }
                                .testTag("tab_user_login"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔐 Login",
                                color = if (authTab == 1) Color.White else Color(0xFF1E3A8A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (authTab == 0) Color(0xFF2563EB) else Color.Transparent)
                                .clickable { authTab = 0 }
                                .testTag("tab_user_register"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📝 Register",
                                color = if (authTab == 0) Color.White else Color(0xFF1E3A8A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.15f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (authTab == 2) Color(0xFFD97706) else Color.Transparent)
                                .clickable { authTab = 2 }
                                .testTag("tab_user_forgot_password"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔑 Forgot?",
                                color = if (authTab == 2) Color.White else Color(0xFF92400E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (authTab == 0) {
                        Text(
                            text = "👤 Full Name",
                            color = Color(0xFF1E3A8A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        BasicTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFDBEAFE))
                                .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 11.dp)
                                .testTag("auth_full_name_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = "📱 Registered Mobile Number or Email",
                        color = Color(0xFF1E3A8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    BasicTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFDBEAFE))
                            .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                            .testTag("auth_email_phone_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (authTab == 2) "🔑 New Password (min 4 characters)" else "🔑 Password (minimum 4 characters)",
                        color = Color(0xFF1E3A8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    BasicTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFDBEAFE))
                            .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 11.dp)
                            .testTag("auth_password_input")
                    )

                    if (authTab == 2) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🔐 Confirm New Password",
                            color = Color(0xFF1E3A8A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        BasicTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFDBEAFE))
                                .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 11.dp)
                                .testTag("auth_confirm_password_input")
                        )
                    } else if (authTab == 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "🔑 Forgot Password?",
                                color = Color(0xFFD97706),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable { authTab = 2 }
                                    .padding(vertical = 4.dp)
                                    .testTag("forgot_password_link")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(50.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = if (authTab == 2) {
                                        listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFD97706))
                                    } else {
                                        listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                                    }
                                )
                            )
                            .border(
                                2.dp,
                                if (authTab == 2) Color(0xFFB45309) else Color(0xFF15803D),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                when (authTab) {
                                    0 -> onRegister(fullName, emailOrPhone, password)
                                    1 -> onLogin(emailOrPhone, password)
                                    2 -> onResetPassword(emailOrPhone, password, confirmPassword)
                                }
                            }
                            .testTag("submit_user_auth_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (authTab) {
                                0 -> "✅ Register & Continue"
                                1 -> "🔐 Login & Continue"
                                else -> "🔄 Reset Password & Login"
                            },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Floating Yellow Header
            Box(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFDE047), Color(0xFFFACC15), Color(0xFFEAB308))
                        )
                    )
                    .border(2.dp, Color(0xFFCA8A04), RoundedCornerShape(18.dp))
                    .padding(horizontal = 22.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (isUserLoggedIn && authTab == 3) {
                        "👤 My User Profile"
                    } else {
                        when (authTab) {
                            0 -> "📝 User Registration"
                            1 -> "🔐 User Login"
                            else -> "🔑 Forgot Password"
                        }
                    },
                    color = Color(0xFF9A3412),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Red 'X' close button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 10.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEF4444))
                    .border(2.dp, Color(0xFF991B1B), RoundedCornerShape(10.dp))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun TierClipboardCard(
    amount: Double,
    currencySymbol: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) Color(0xFFFDE047) else Color(0xFFEFF6FF))
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = if (isSelected) Color(0xFFCA8A04) else Color(0xFF60A5FA),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 8.dp)
                .testTag("tier_card_${amount.toInt()}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PinkCashStackIcon(size = 34.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$currencySymbol %,.2f".format(amount),
                color = Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .width(28.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF38BDF8))
                .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.White))
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✔", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun AccountDetailsInputModal(
    countryRegion: CountryRegion,
    initialOption: MobileBankingOption,
    initialAccount: String,
    initialName: String,
    strings: LocalizedStrings,
    onSelectCountryRegion: (CountryRegion) -> Unit,
    onSubmit: (MobileBankingOption, String, String) -> Unit,
    onClose: () -> Unit
) {
    val options = countryRegion.mobileBankingOptions
    var selectedOption by remember(countryRegion) {
        mutableStateOf(options.find { it.id == initialOption.id } ?: options.first())
    }
    var accountText by remember { mutableStateOf(initialAccount) }
    var nameText by remember { mutableStateOf(initialName) }

    LaunchedEffect(countryRegion) {
        selectedOption = countryRegion.mobileBankingOptions.first()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = true, onClick = {})
            .padding(horizontal = 18.dp)
            .testTag("account_input_modal"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF60A5FA))
                    .border(3.dp, Color(0xFF2563EB), RoundedCornerShape(26.dp))
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(top = 28.dp, bottom = 18.dp, start = 14.dp, end = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Local Banking Header (Auto-configured by Language/Country)
                Text(
                    text = strings.selectAccountMethodSubtitle,
                    color = Color(0xFF1E3A8A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2x2 Grid of Country-Specific Local Mobile Banking Methods!
                // e.g. Bangladesh -> Nagad, bKash, Rocket, Upay
                // e.g. India -> PhonePe, Paytm, Google Pay, BHIM UPI
                if (options.size >= 4) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MobileBankingMethodCard(
                                option = options[0],
                                isSelected = selectedOption.id == options[0].id,
                                onClick = { selectedOption = options[0] },
                                modifier = Modifier.weight(1f)
                            )
                            MobileBankingMethodCard(
                                option = options[1],
                                isSelected = selectedOption.id == options[1].id,
                                onClick = { selectedOption = options[1] },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MobileBankingMethodCard(
                                option = options[2],
                                isSelected = selectedOption.id == options[2].id,
                                onClick = { selectedOption = options[2] },
                                modifier = Modifier.weight(1f)
                            )
                            MobileBankingMethodCard(
                                option = options[3],
                                isSelected = selectedOption.id == options[3].id,
                                onClick = { selectedOption = options[3] },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Number / UPI / Email Input
                Text(
                    text = "${selectedOption.displayName} — ${strings.accountInputPlaceholder}",
                    color = Color(0xFF1E3A8A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                BasicTextField(
                    value = accountText,
                    onValueChange = { accountText = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDBEAFE))
                        .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 11.dp)
                        .testTag("account_number_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Holder Name Input
                Text(
                    text = strings.nameInputPlaceholder,
                    color = Color(0xFF1E3A8A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                BasicTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDBEAFE))
                        .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 11.dp)
                        .testTag("account_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Green Submit Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.76f)
                        .height(50.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                            )
                        )
                        .border(2.dp, Color(0xFF15803D), RoundedCornerShape(16.dp))
                        .clickable { onSubmit(selectedOption, accountText, nameText) }
                        .testTag("submit_account_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.submitBtn,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Floating Yellow Header
            Box(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFDE047), Color(0xFFFACC15), Color(0xFFEAB308))
                        )
                    )
                    .border(2.dp, Color(0xFFCA8A04), RoundedCornerShape(18.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = strings.accountDetailsHeader,
                    color = Color(0xFF9A3412),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Red 'X' close button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 10.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEF4444))
                    .border(2.dp, Color(0xFF991B1B), RoundedCornerShape(10.dp))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun MobileBankingMethodCard(
    option: MobileBankingOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgBrush = Brush.horizontalGradient(
        colors = listOf(Color(option.startColorHex), Color(option.endColorHex))
    )
    val textColor = Color(option.textColorHex)

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush)
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) Color(0xFF16A34A) else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
            .testTag("payment_method_${option.id}"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = option.iconEmoji, fontSize = 19.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = option.displayName,
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    text = option.subLabel,
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFF0F172A), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("✔", color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun WithdrawConfirmModal(
    option: MobileBankingOption,
    accountNumber: String,
    accountName: String,
    strings: LocalizedStrings,
    onConfirm: () -> Unit,
    onClose: () -> Unit
) {
    val maskedAccount = if (accountNumber.length > 4) {
        accountNumber.take(2) + "*".repeat((accountNumber.length - 4).coerceAtLeast(4)) + accountNumber.takeLast(2)
    } else {
        accountNumber
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = true, onClick = {})
            .padding(horizontal = 24.dp)
            .testTag("withdraw_confirm_modal"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF60A5FA))
                    .border(3.dp, Color(0xFF2563EB), RoundedCornerShape(26.dp))
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(top = 32.dp, bottom = 20.dp, start = 18.dp, end = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MobileBankingMethodCard(
                    option = option,
                    isSelected = true,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.78f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = strings.accountFieldLabel,
                    color = Color(0xFF1E3A8A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = maskedAccount,
                    color = Color(0xFFDC2626),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = strings.nameFieldLabel,
                    color = Color(0xFF1E3A8A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = accountName,
                    color = Color(0xFFDC2626),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(50.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                            )
                        )
                        .border(2.dp, Color(0xFF15803D), RoundedCornerShape(16.dp))
                        .clickable { onConfirm() }
                        .testTag("confirm_withdraw_final_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.withdrawBtn,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Box(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFDE047), Color(0xFFFACC15), Color(0xFFEAB308))
                        )
                    )
                    .border(2.dp, Color(0xFFCA8A04), RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = strings.confirmWithdrawHeader,
                    color = Color(0xFF9A3412),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 10.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEF4444))
                    .border(2.dp, Color(0xFF991B1B), RoundedCornerShape(10.dp))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
