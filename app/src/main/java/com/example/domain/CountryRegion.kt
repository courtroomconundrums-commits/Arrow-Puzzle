package com.example.domain

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale
import kotlin.math.roundToLong

data class MobileBankingOption(
    val id: String,
    val displayName: String,
    val subLabel: String,
    val iconEmoji: String,
    val startColorHex: Long,
    val endColorHex: Long,
    val textColorHex: Long = 0xFFFFFFFF
)

enum class CountryRegion(
    val countryCode: String,
    val countryName: String,
    val flagEmoji: String,
    val currencyCode: String,
    val currencySymbol: String,
    val defaultLanguageCode: String,
    val bdtConversionRate: Double,
    val mobileBankingOptions: List<MobileBankingOption>
) {
    BANGLADESH(
        countryCode = "BD",
        countryName = "Bangladesh",
        flagEmoji = "🇧🇩",
        currencyCode = "BDT",
        currencySymbol = "৳",
        defaultLanguageCode = "EN",
        bdtConversionRate = 1.0,
        mobileBankingOptions = listOf(
            MobileBankingOption("BKASH", "bKash", "Personal Wallet", "💸", 0xFFE2136E, 0xFF9D174D),
            MobileBankingOption("NAGAD", "Nagad", "Instant Transfer", "🔥", 0xFFF7941D, 0xFFC2410C),
            MobileBankingOption("ROCKET", "Rocket", "DBBL Mobile", "🚀", 0xFF8C3494, 0xFF581C87),
            MobileBankingOption("UPAY", "Upay", "UCB Digital", "💳", 0xFF0072BC, 0xFF1E3A8A)
        )
    ),
    INDIA(
        countryCode = "IN",
        countryName = "India",
        flagEmoji = "🇮🇳",
        currencyCode = "INR",
        currencySymbol = "₹",
        defaultLanguageCode = "HI",
        bdtConversionRate = 0.70,
        mobileBankingOptions = listOf(
            MobileBankingOption("PAYTM", "Paytm", "Wallet / UPI", "📲", 0xFF00B9F1, 0xFF0369A1),
            MobileBankingOption("PHONEPE", "PhonePe", "Instant UPI", "💜", 0xFF5F259F, 0xFF3B0764),
            MobileBankingOption("GPAY_IN", "Google Pay", "UPI Transfer", "⚡", 0xFF1A73E8, 0xFF1E3A8A),
            MobileBankingOption("UPI", "BHIM UPI", "Direct Bank", "🏦", 0xFF16A34A, 0xFF14532D)
        )
    ),
    PAKISTAN(
        countryCode = "PK",
        countryName = "Pakistan",
        flagEmoji = "🇵🇰",
        currencyCode = "PKR",
        currencySymbol = "Rs",
        defaultLanguageCode = "UR",
        bdtConversionRate = 2.30,
        mobileBankingOptions = listOf(
            MobileBankingOption("JAZZCASH", "JazzCash", "Mobile Account", "❤️", 0xFFDC2626, 0xFF7F1D1D),
            MobileBankingOption("EASYPAISA", "Easypaisa", "Instant Wallet", "💚", 0xFF16A34A, 0xFF14532D),
            MobileBankingOption("SADAPAY", "SadaPay", "Digital Bank", "💎", 0xFF0D9488, 0xFF115E59),
            MobileBankingOption("NAYAPAY", "NayaPay", "Fast Transfer", "🧡", 0xFFEA580C, 0xFF9A3412)
        )
    ),
    INDONESIA(
        countryCode = "ID",
        countryName = "Indonesia",
        flagEmoji = "🇮🇩",
        currencyCode = "IDR",
        currencySymbol = "Rp",
        defaultLanguageCode = "ID",
        bdtConversionRate = 130.0,
        mobileBankingOptions = listOf(
            MobileBankingOption("DANA", "DANA", "Dompet Digital", "💙", 0xFF118EEA, 0xFF1E3A8A),
            MobileBankingOption("OVO", "OVO", "Instant Cash", "💜", 0xFF4C3494, 0xFF3B0764),
            MobileBankingOption("GOPAY", "GoPay", "Gojek Wallet", "💚", 0xFF00AED6, 0xFF0E7490),
            MobileBankingOption("SHOPEEPAY", "ShopeePay", "Fast Payout", "🧡", 0xFFEE4D2D, 0xFF9A3412)
        )
    ),
    SAUDI_ARABIA(
        countryCode = "SA",
        countryName = "Saudi Arabia",
        flagEmoji = "🇸🇦",
        currencyCode = "SAR",
        currencySymbol = "SR",
        defaultLanguageCode = "AR",
        bdtConversionRate = 0.032,
        mobileBankingOptions = listOf(
            MobileBankingOption("STCPAY", "STC Pay", "Saudi Wallet", "💜", 0xFF4F008C, 0xFF3B0764),
            MobileBankingOption("URPAY", "urpay", "Al Rajhi Digital", "💙", 0xFF0284C7, 0xFF075985),
            MobileBankingOption("ALRAJHI", "Al Rajhi", "Bank Transfer", "🏦", 0xFF1D4ED8, 0xFF1E3A8A),
            MobileBankingOption("BINANCE_SA", "Binance Pay", "USDT Instant", "🪙", 0xFFEAB308, 0xFFA16207)
        )
    ),
    BRAZIL(
        countryCode = "BR",
        countryName = "Brazil",
        flagEmoji = "🇧🇷",
        currencyCode = "BRL",
        currencySymbol = "R$",
        defaultLanguageCode = "PT",
        bdtConversionRate = 0.048,
        mobileBankingOptions = listOf(
            MobileBankingOption("PIX", "Pix", "Instant Key", "⚡", 0xFF00BFA5, 0xFF0F766E),
            MobileBankingOption("PICPAY", "PicPay", "Digital Wallet", "💚", 0xFF11C76F, 0xFF15803D),
            MobileBankingOption("PAGBANK", "PagBank", "Fast Payout", "💛", 0xFFD97706, 0xFF92400E),
            MobileBankingOption("MERCADOPAGO_BR", "MercadoPago", "Instant", "💙", 0xFF009EE3, 0xFF0369A1)
        )
    ),
    SPAIN_LATAM(
        countryCode = "ES",
        countryName = "Spain & LatAm",
        flagEmoji = "🇪🇸",
        currencyCode = "USD",
        currencySymbol = "$",
        defaultLanguageCode = "ES",
        bdtConversionRate = 0.0085,
        mobileBankingOptions = listOf(
            MobileBankingOption("NEQUI", "Nequi", "Mobile Wallet", "💙", 0xFF009EE3, 0xFF0369A1),
            MobileBankingOption("MERCADOPAGO", "MercadoPago", "Digital Account", "⚡", 0xFF2563EB, 0xFF1E3A8A),
            MobileBankingOption("PAYPAL_ES", "PayPal", "Global Email", "🌐", 0xFF0070BA, 0xFF1E3A8A),
            MobileBankingOption("BINANCE_ES", "Binance Pay", "USDT Crypto", "🪙", 0xFFD97706, 0xFF92400E)
        )
    ),
    GLOBAL_US(
        countryCode = "US",
        countryName = "Global (USD)",
        flagEmoji = "🇺🇸",
        currencyCode = "USD",
        currencySymbol = "$",
        defaultLanguageCode = "EN",
        bdtConversionRate = 0.0085,
        mobileBankingOptions = listOf(
            MobileBankingOption("PAYPAL", "PayPal", "Instant Email", "🌐", 0xFF0070BA, 0xFF1E3A8A),
            MobileBankingOption("BINANCE", "Binance Pay", "USDT / UID", "🪙", 0xFFD97706, 0xFF92400E),
            MobileBankingOption("CASHAPP", "Cash App", "\$Cashtag", "💵", 0xFF16A34A, 0xFF14532D),
            MobileBankingOption("PAYEER", "Payeer", "Global Wallet", "💳", 0xFF0284C7, 0xFF075985)
        )
    );

    fun convertFromBdt(bdtAmount: Double): Double {
        val raw = bdtAmount * bdtConversionRate
        return (raw * 100.0).roundToLong() / 100.0
    }

    fun oneAdRewardLocal(): Double = convertFromBdt(10.0)

    fun oneAdConversionBadge(): String {
        val localAmt = "%.2f".format(oneAdRewardLocal())
        return "$countryName ($currencySymbol$localAmt / Ad)"
    }

    companion object {
        fun fromCountryCode(code: String): CountryRegion =
            entries.find { it.countryCode.equals(code, ignoreCase = true) } ?: BANGLADESH

        fun fromLanguage(lang: AppLanguage): CountryRegion = when (lang) {
            AppLanguage.BENGALI -> BANGLADESH
            AppLanguage.HINDI -> INDIA
            AppLanguage.URDU -> PAKISTAN
            AppLanguage.INDONESIAN -> INDONESIA
            AppLanguage.ARABIC -> SAUDI_ARABIA
            AppLanguage.PORTUGUESE -> BRAZIL
            AppLanguage.SPANISH -> SPAIN_LATAM
            AppLanguage.ENGLISH -> GLOBAL_US
        }

        fun detectFromDevice(context: Context): CountryRegion {
            return try {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                val simCountry = tm?.simCountryIso?.uppercase(Locale.ROOT).orEmpty()
                val netCountry = tm?.networkCountryIso?.uppercase(Locale.ROOT).orEmpty()
                val localeCountry = Locale.getDefault().country.uppercase(Locale.ROOT)
                val code = listOf(simCountry, netCountry, localeCountry).firstOrNull { it.length == 2 } ?: "BD"
                fromCountryCode(code)
            } catch (_: Throwable) {
                BANGLADESH
            }
        }
    }
}
