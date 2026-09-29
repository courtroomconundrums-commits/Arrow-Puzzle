package com.example.domain

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToLong

/**
 * Represents the user's Country / Region, default language, currency symbol,
 * exchange rate from Bangladesh Taka (BDT ৳), and country-specific local mobile banking methods.
 *
 * Base reward rule:
 * - 1 Video Ad Watch = BDT 10.00 (৳ 10.00)
 * - Converted automatically to the user's country currency:
 *   - Bangladesh (BDT ৳): 1 Ad = ৳ 10.00 (Rate: 1.00)
 *   - India (INR ₹): 1 Ad = ₹ 7.10 (Rate: 0.71 INR per 1 BDT)
 *   - Global / USA / English (USD $): 1 Ad = $ 0.08 (Rate: 0.0084 USD per 1 BDT)
 *   - Indonesia (IDR Rp): 1 Ad = Rp 1,325.00 (Rate: 132.50 IDR per 1 BDT)
 *   - Pakistan (PKR Rs): 1 Ad = Rs 23.30 (Rate: 2.33 PKR per 1 BDT)
 *   - Saudi Arabia / MENA (SAR ﷼): 1 Ad = ﷼ 0.31 (Rate: 0.031 SAR per 1 BDT)
 *   - Brazil (BRL R$): 1 Ad = R$ 0.48 (Rate: 0.048 BRL per 1 BDT)
 *   - Spain / LatAm (USD $): 1 Ad = $ 0.08 (Rate: 0.0084 USD per 1 BDT)
 */
enum class CountryRegion(
    val countryCode: String,
    val countryName: String,
    val flagEmoji: String,
    val defaultLanguageCode: String,
    val currencySymbol: String,
    val currencyCode: String,
    val rateFromBdt: Double,
    val mobileBankingOptions: List<MobileBankingOption>
) {
    BANGLADESH(
        countryCode = "BD",
        countryName = "Bangladesh (বাংলাদেশ)",
        flagEmoji = "🇧🇩",
        defaultLanguageCode = "BN",
        currencySymbol = "৳",
        currencyCode = "BDT",
        rateFromBdt = 1.00,
        mobileBankingOptions = listOf(
            MobileBankingOption(
                id = "NAGAD",
                displayName = "নগদ",
                subLabel = "Nagad",
                iconEmoji = "🔥",
                startColorHex = 0xFFFFF7ED,
                endColorHex = 0xFFFFEDD5,
                textColorHex = 0xFFDC2626
            ),
            MobileBankingOption(
                id = "BKASH",
                displayName = "bKash",
                subLabel = "বিকাশ",
                iconEmoji = "🕊️",
                startColorHex = 0xFFE11D48,
                endColorHex = 0xFFBE185D,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "ROCKET",
                displayName = "রকেট",
                subLabel = "ROCKET",
                iconEmoji = "🚀",
                startColorHex = 0xFF7E22CE,
                endColorHex = 0xFF6B21A8,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "UPAY",
                displayName = "উপায়",
                subLabel = "Upay",
                iconEmoji = "💳",
                startColorHex = 0xFF0284C7,
                endColorHex = 0xFF0369A1,
                textColorHex = 0xFFFDE047
            )
        )
    ),

    INDIA(
        countryCode = "IN",
        countryName = "India (भारत)",
        flagEmoji = "🇮🇳",
        defaultLanguageCode = "HI",
        currencySymbol = "₹",
        currencyCode = "INR",
        rateFromBdt = 0.71,
        mobileBankingOptions = listOf(
            MobileBankingOption(
                id = "PHONEPE",
                displayName = "PhonePe",
                subLabel = "फोनपे UPI",
                iconEmoji = "🟣",
                startColorHex = 0xFF6739B7,
                endColorHex = 0xFF512DA8,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "PAYTM",
                displayName = "Paytm",
                subLabel = "पेटीएम Wallet/UPI",
                iconEmoji = "💠",
                startColorHex = 0xFF00BAF2,
                endColorHex = 0xFF002E6E,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "GPAY",
                displayName = "Google Pay",
                subLabel = "GPay UPI",
                iconEmoji = "🇬",
                startColorHex = 0xFF2563EB,
                endColorHex = 0xFF1D4ED8,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "BHIM_UPI",
                displayName = "BHIM UPI",
                subLabel = "Direct UPI ID",
                iconEmoji = "⚡",
                startColorHex = 0xFFEA580C,
                endColorHex = 0xFF16A34A,
                textColorHex = 0xFFFFFFFF
            )
        )
    ),

    GLOBAL_US(
        countryCode = "US",
        countryName = "English / Global (USA/UK)",
        flagEmoji = "🇺🇸",
        defaultLanguageCode = "EN",
        currencySymbol = "$",
        currencyCode = "USD",
        rateFromBdt = 0.0084,
        mobileBankingOptions = listOf(
            MobileBankingOption(
                id = "PAYPAL",
                displayName = "PayPal",
                subLabel = "Instant Transfer",
                iconEmoji = "🅿️",
                startColorHex = 0xFF2563EB,
                endColorHex = 0xFF1D4ED8,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "CASH_APP",
                displayName = "Cash App",
                subLabel = "Cashtag",
                iconEmoji = "💵",
                startColorHex = 0xFF00D632,
                endColorHex = 0xFF15803D,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "VENMO",
                displayName = "Venmo",
                subLabel = "Mobile Wallet",
                iconEmoji = "💙",
                startColorHex = 0xFF008CFF,
                endColorHex = 0xFF0284C7,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "ZELLE",
                displayName = "Zelle",
                subLabel = "Mobile Banking",
                iconEmoji = "🟣",
                startColorHex = 0xFF6D1ED4,
                endColorHex = 0xFF4C1D95,
                textColorHex = 0xFFFFFFFF
            )
        )
    ),

    INDONESIA(
        countryCode = "ID",
        countryName = "Indonesia",
        flagEmoji = "🇮🇩",
        defaultLanguageCode = "ID",
        currencySymbol = "Rp",
        currencyCode = "IDR",
        rateFromBdt = 132.50,
        mobileBankingOptions = listOf(
            MobileBankingOption(
                id = "DANA",
                displayName = "DANA",
                subLabel = "Dompet Digital",
                iconEmoji = "💙",
                startColorHex = 0xFF118EEA,
                endColorHex = 0xFF0284C7,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "OVO",
                displayName = "OVO",
                subLabel = "OVO Cash",
                iconEmoji = "🟣",
                startColorHex = 0xFF4C3494,
                endColorHex = 0xFF3B0764,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "GOPAY",
                displayName = "GoPay",
                subLabel = "Gojek Wallet",
                iconEmoji = "🟢",
                startColorHex = 0xFF00AA13,
                endColorHex = 0xFF15803D,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "SHOPEEPAY",
                displayName = "ShopeePay",
                subLabel = "Shopee Wallet",
                iconEmoji = "🧡",
                startColorHex = 0xFFEE4D2D,
                endColorHex = 0xFFC2410C,
                textColorHex = 0xFFFFFFFF
            )
        )
    ),

    PAKISTAN(
        countryCode = "PK",
        countryName = "Pakistan (پاکستان)",
        flagEmoji = "🇵🇰",
        defaultLanguageCode = "UR",
        currencySymbol = "Rs",
        currencyCode = "PKR",
        rateFromBdt = 2.33,
        mobileBankingOptions = listOf(
            MobileBankingOption(
                id = "EASYPAISA",
                displayName = "Easypaisa",
                subLabel = "ایزی پیسہ",
                iconEmoji = "🟢",
                startColorHex = 0xFF22C55E,
                endColorHex = 0xFF15803D,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "JAZZCASH",
                displayName = "JazzCash",
                subLabel = "جاز کیش",
                iconEmoji = "🔴",
                startColorHex = 0xFFDC2626,
                endColorHex = 0xFF991B1B,
                textColorHex = 0xFFFDE047
            ),
            MobileBankingOption(
                id = "SADAPAY",
                displayName = "SadaPay",
                subLabel = "سدا پے",
                iconEmoji = "💎",
                startColorHex = 0xFF14B8A6,
                endColorHex = 0xFF0F766E,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "NAYAPAY",
                displayName = "NayaPay",
                subLabel = "نیا پے",
                iconEmoji = "🧡",
                startColorHex = 0xFFF97316,
                endColorHex = 0xFFEA580C,
                textColorHex = 0xFFFFFFFF
            )
        )
    ),

    SAUDI_ARABIA(
        countryCode = "SA",
        countryName = "Saudi Arabia / MENA (السعودية)",
        flagEmoji = "🇸🇦",
        defaultLanguageCode = "AR",
        currencySymbol = "﷼",
        currencyCode = "SAR",
        rateFromBdt = 0.031,
        mobileBankingOptions = listOf(
            MobileBankingOption(
                id = "STC_PAY",
                displayName = "stc pay",
                subLabel = "اس تي سي باي",
                iconEmoji = "🟣",
                startColorHex = 0xFF4F008C,
                endColorHex = 0xFF3B0764,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "URPAY",
                displayName = "urpay",
                subLabel = "يور باي",
                iconEmoji = "🔵",
                startColorHex = 0xFF2563EB,
                endColorHex = 0xFF1E40AF,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "MOBILY_PAY",
                displayName = "Mobily Pay",
                subLabel = "موبايلي باي",
                iconEmoji = "💠",
                startColorHex = 0xFF0284C7,
                endColorHex = 0xFF0369A1,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "PAYPAL_MENA",
                displayName = "PayPal",
                subLabel = "باي بال",
                iconEmoji = "🅿️",
                startColorHex = 0xFF003087,
                endColorHex = 0xFF001C64,
                textColorHex = 0xFFFFFFFF
            )
        )
    ),

    BRAZIL(
        countryCode = "BR",
        countryName = "Brasil",
        flagEmoji = "🇧🇷",
        defaultLanguageCode = "PT",
        currencySymbol = "R$",
        currencyCode = "BRL",
        rateFromBdt = 0.048,
        mobileBankingOptions = listOf(
            MobileBankingOption(
                id = "PIX",
                displayName = "Pix",
                subLabel = "Chave Pix",
                iconEmoji = "💠",
                startColorHex = 0xFF32BCAD,
                endColorHex = 0xFF0D9488,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "PICPAY",
                displayName = "PicPay",
                subLabel = "Carteira Digital",
                iconEmoji = "🟢",
                startColorHex = 0xFF21C25E,
                endColorHex = 0xFF15803D,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "MERCADO_PAGO_BR",
                displayName = "Mercado Pago",
                subLabel = "Conta Digital",
                iconEmoji = "🤝",
                startColorHex = 0xFF009EE3,
                endColorHex = 0xFF0284C7,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "NUBANK",
                displayName = "Nubank",
                subLabel = "NuConta",
                iconEmoji = "🟣",
                startColorHex = 0xFF820AD1,
                endColorHex = 0xFF581C87,
                textColorHex = 0xFFFFFFFF
            )
        )
    ),

    LATAM_SPAIN(
        countryCode = "ES",
        countryName = "España / Latinoamérica",
        flagEmoji = "🇪🇸",
        defaultLanguageCode = "ES",
        currencySymbol = "$",
        currencyCode = "USD",
        rateFromBdt = 0.0084,
        mobileBankingOptions = listOf(
            MobileBankingOption(
                id = "MERCADO_PAGO",
                displayName = "Mercado Pago",
                subLabel = "Billetera Móvil",
                iconEmoji = "💙",
                startColorHex = 0xFF009EE3,
                endColorHex = 0xFF0284C7,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "NEQUI_BIZUM",
                displayName = "Nequi / Bizum",
                subLabel = "Pago Móvil",
                iconEmoji = "⚡",
                startColorHex = 0xFFDB2777,
                endColorHex = 0xFF9D174D,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "YAPE_PLIN",
                displayName = "Yape / Plin",
                subLabel = "Transferencia",
                iconEmoji = "🟣",
                startColorHex = 0xFF7E22CE,
                endColorHex = 0xFF581C87,
                textColorHex = 0xFFFFFFFF
            ),
            MobileBankingOption(
                id = "PAYPAL_ES",
                displayName = "PayPal",
                subLabel = "Cuenta Global",
                iconEmoji = "🅿️",
                startColorHex = 0xFF2563EB,
                endColorHex = 0xFF1D4ED8,
                textColorHex = 0xFFFFFFFF
            )
        )
    );

    /**
     * Converts an amount from Base Bangladesh Taka (BDT ৳) into this country's local currency.
     * Example: 10.00 BDT (1 Ad Watch) ->
     * - Bangladesh (BDT): ৳ 10.00
     * - India (INR): ₹ 7.10
     * - Global / English (USD): $ 0.08
     * - Indonesia (IDR): Rp 1,325.00
     * - Pakistan (PKR): Rs 23.30
     * - Saudi Arabia (SAR): ﷼ 0.31
     * - Brazil (BRL): R$ 0.48
     */
    fun convertFromBdt(bdtAmount: Double): Double {
        if (bdtAmount <= 0.0) return 0.0
        val raw = bdtAmount * rateFromBdt
        val rounded = (raw * 100.0).roundToLong() / 100.0
        return if (rounded < 0.01) 0.01 else rounded
    }

    /**
     * Returns the converted reward for watching 1 Video Ad (Base = 10.00 BDT).
     */
    fun oneAdRewardLocal(): Double = convertFromBdt(BASE_AD_REWARD_BDT)

    /**
     * Returns a human-readable conversion badge showing how 1 Ad (BDT 10 Taka) converts to this currency.
     */
    fun oneAdConversionBadge(): String {
        val localAmt = "%.2f".format(oneAdRewardLocal())
        return if (this == BANGLADESH) {
            "1 Ad = ৳10.00 BDT"
        } else {
            "1 Ad (৳10 BDT) = $currencySymbol$localAmt $currencyCode"
        }
    }

    companion object {
        const val BASE_AD_REWARD_BDT = 10.00

        fun fromCountryCode(code: String): CountryRegion =
            entries.find { it.countryCode.equals(code, ignoreCase = true) } ?: GLOBAL_US

        fun fromLanguage(lang: AppLanguage): CountryRegion =
            when (lang) {
                AppLanguage.ENGLISH -> GLOBAL_US
                AppLanguage.BENGALI -> BANGLADESH
                AppLanguage.HINDI -> INDIA
                AppLanguage.INDONESIAN -> INDONESIA
                AppLanguage.URDU -> PAKISTAN
                AppLanguage.ARABIC -> SAUDI_ARABIA
                AppLanguage.PORTUGUESE -> BRAZIL
                AppLanguage.SPANISH -> LATAM_SPAIN
            }

        /**
         * Automatically detects the user's CountryRegion from:
         * 1. SIM Country ISO (TelephonyManager)
         * 2. Network Country ISO (TelephonyManager)
         * 3. Device TimeZone (e.g. Asia/Dhaka -> BD, Asia/Kolkata -> IN, Asia/Jakarta -> ID, Asia/Karachi -> PK)
         * 4. System Locale Country & Language
         */
        fun detectFromDevice(context: Context): CountryRegion {
            try {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                val simCountry = tm?.simCountryIso?.uppercase(Locale.ROOT)?.trim().orEmpty()
                val netCountry = tm?.networkCountryIso?.uppercase(Locale.ROOT)?.trim().orEmpty()

                for (iso in listOf(simCountry, netCountry)) {
                    mapIsoToRegion(iso)?.let { return it }
                }
            } catch (_: Throwable) {
            }

            // Check TimeZone
            val tzId = TimeZone.getDefault().id.lowercase(Locale.ROOT)
            when {
                tzId.contains("dhaka") || tzId.contains("dacca") -> return BANGLADESH
                tzId.contains("kolkata") || tzId.contains("calcutta") || tzId.contains("mumbai") || tzId.contains("chennai") -> return INDIA
                tzId.contains("jakarta") || tzId.contains("makassar") || tzId.contains("jayapura") || tzId.contains("pontianak") -> return INDONESIA
                tzId.contains("karachi") || tzId.contains("islamabad") -> return PAKISTAN
                tzId.contains("riyadh") || tzId.contains("dubai") || tzId.contains("kuwait") || tzId.contains("qatar") || tzId.contains("cairo") || tzId.contains("baghdad") -> return SAUDI_ARABIA
                tzId.contains("sao_paulo") || tzId.contains("fortaleza") || tzId.contains("manaus") || tzId.contains("recife") -> return BRAZIL
                tzId.contains("madrid") || tzId.contains("mexico") || tzId.contains("bogota") || tzId.contains("lima") || tzId.contains("buenos_aires") || tzId.contains("santiago") -> return LATAM_SPAIN
            }

            // Check System Locale
            val locale = Locale.getDefault()
            mapIsoToRegion(locale.country.uppercase(Locale.ROOT))?.let { return it }
            return when (locale.language.lowercase(Locale.ROOT)) {
                "bn" -> BANGLADESH
                "hi", "ta", "te", "mr", "gu", "kn", "ml", "pa" -> INDIA
                "in", "id" -> INDONESIA
                "ur" -> PAKISTAN
                "ar" -> SAUDI_ARABIA
                "pt" -> BRAZIL
                "es" -> LATAM_SPAIN
                else -> GLOBAL_US
            }
        }

        private fun mapIsoToRegion(iso: String): CountryRegion? {
            return when (iso) {
                "BD" -> BANGLADESH
                "IN" -> INDIA
                "ID", "MY" -> INDONESIA
                "PK" -> PAKISTAN
                "SA", "AE", "QA", "KW", "OM", "BH", "EG", "IQ", "JO", "MA" -> SAUDI_ARABIA
                "BR", "PT" -> BRAZIL
                "ES", "MX", "CO", "AR", "PE", "CL", "EC", "VE" -> LATAM_SPAIN
                "US", "GB", "CA", "AU" -> GLOBAL_US
                else -> null
            }
        }
    }
}
