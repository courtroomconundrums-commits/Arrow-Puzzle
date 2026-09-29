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
import com.example.domain.CountryRegion
import com.example.domain.LocalizedStrings
import com.example.domain.MobileBankingOption

@Composable
fun WithdrawScreen(
    balance: Double,
    selectedTierBdt: Double,
    countryRegion: CountryRegion,
    savedPaymentMethodId: String,
    savedAccountNumber: String,
    savedAccountName: String,
    strings: LocalizedStrings,
    currencySymbol: String,
    tickerText: String,
    showAccountModal: Boolean,
    showConfirmModal: Boolean,
    onSelectTierBdt: (Double) -> Unit,
    onSelectCountryRegion: (CountryRegion) -> Unit,
    onClickWithdrawButton: () -> Unit,
    onSaveAccountDetails: (MobileBankingOption, String, String) -> Unit,
    onConfirmWithdraw: () -> Unit,
    onCloseModals: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        if (showAccountModal || showConfirmModal) {
            onCloseModals()
        } else {
            onBack()
        }
    }

    val tiersBdt = listOf(5000.0, 10000.0, 20000.0, 30000.0)
    val tiers = tiersBdt.map { countryRegion.convertFromBdt(it) }
    val selectedTier = countryRegion.convertFromBdt(selectedTierBdt)
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

                // Active Country Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF1E3A8A))
                        .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = countryRegion.flagEmoji, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = countryRegion.countryCode,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Country / Mobile Banking Quick Switcher Bar so users can see auto-detected country or switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (region in CountryRegion.entries) {
                    val isSelected = region == countryRegion
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) Color(0xFFFDE047)
                                else Color(0xFF1E40AF).copy(alpha = 0.7f)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) Color.White else Color(0xFF60A5FA),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onSelectCountryRegion(region) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("withdraw_region_${region.countryCode}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = region.flagEmoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${region.countryCode} (${region.mobileBankingOptions.first().displayName})",
                            color = if (isSelected) Color(0xFF0F172A) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                        // Local mobile banking methods badge
                        Text(
                            text = options.joinToString(" • ") { it.displayName },
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // BDT to Local Currency Conversion Badge (e.g. 1 Ad = ৳10 BDT = ₹7.10 INR / $0.08 USD)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.72f))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "💱 ${countryRegion.oneAdConversionBadge()}",
                            color = Color(0xFF78350F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
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
                            isSelected = selectedTierBdt == tiersBdt[0],
                            onClick = { onSelectTierBdt(tiersBdt[0]) },
                            modifier = Modifier.weight(1f)
                        )
                        TierClipboardCard(
                            amount = tiers[1],
                            currencySymbol = currencySymbol,
                            isSelected = selectedTierBdt == tiersBdt[1],
                            onClick = { onSelectTierBdt(tiersBdt[1]) },
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
                            isSelected = selectedTierBdt == tiersBdt[2],
                            onClick = { onSelectTierBdt(tiersBdt[2]) },
                            modifier = Modifier.weight(1f)
                        )
                        TierClipboardCard(
                            amount = tiers[3],
                            currencySymbol = currencySymbol,
                            isSelected = selectedTierBdt == tiersBdt[3],
                            onClick = { onSelectTierBdt(tiersBdt[3]) },
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
        }

        // Modal 1: Country-Specific Mobile Banking Account Input Modal
        if (showAccountModal) {
            AccountDetailsInputModal(
                countryRegion = countryRegion,
                initialOption = currentOption,
                initialAccount = savedAccountNumber,
                initialName = savedAccountName,
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
                // Country Flag & Local Banking Header
                Text(
                    text = "${countryRegion.flagEmoji} ${countryRegion.countryName} • ${strings.selectAccountMethodSubtitle}",
                    color = Color(0xFF1E3A8A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Country Selector chips inside the modal so user can test BD (bKash/Nagad/Rocket), IN (PhonePe/Paytm), etc.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (reg in CountryRegion.entries) {
                        val isRegSelected = reg == countryRegion
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isRegSelected) Color(0xFFFACC15) else Color(0xFFE2E8F0)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isRegSelected) Color(0xFF15803D) else Color(0xFF94A3B8),
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { onSelectCountryRegion(reg) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${reg.flagEmoji} ${reg.countryCode}",
                                color = Color(0xFF0F172A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2x2 Grid of Country-Specific Local Mobile Banking Methods!
                // e.g. Bangladesh -> নগদ, bKash, রকেট, উপায়
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
