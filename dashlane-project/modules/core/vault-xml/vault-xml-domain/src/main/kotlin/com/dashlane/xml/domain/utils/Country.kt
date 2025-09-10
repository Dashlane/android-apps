package com.dashlane.xml.domain.utils

import java.util.IllformedLocaleException
import java.util.Locale

class Country private constructor(private val countryLocale: Locale) {
    private constructor(isoCountryCode: String) : this(buildLocaleForCountry(isoCountryCode))

    val isoCode: String
        get() = countryLocale.country.uppercase()

    @Suppress("MagicNumber")
    val signature: Int
        get() {
            val isoCode = this.isoCode

            
            val index = LegacyCountryList.isoCodes.indexOf(isoCode)
            if (index >= 0) return index

            check(isoCode.length == 2) { "Unexpected ISO country code length ${isoCode.length}." }
            val char0 = isoCode[0]
            val char1 = isoCode[1]
            check(char0 in 'A'..'Z') { "Unexpected character '$char0' in country code." }
            check(char1 in 'A'..'Z') { "Unexpected character '$char1' in country code." }
            
            
            return -(char1.code shl 8 or char0.code)
        }

    fun getLabel(locale: Locale): String =
        countryLocale.getDisplayCountry(locale)

    override fun hashCode(): Int =
        countryLocale.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Country) return false
        if (countryLocale != other.countryLocale) return false
        return true
    }

    override fun toString(): String =
        "Country(isoCode='$isoCode', label(ENGLISH)=${getLabel(Locale.ENGLISH)})"

    companion object {
        

        val Argentina = Country("AR")
        val Brazil = Country("BR")
        val Canada = Country("CA")
        val Chile = Country("CL")
        val China = Country("CN")
        val Colombia = Country("CO")
        val France = Country("FR")
        val Ireland = Country("IE")
        val Italy = Country("IT")
        val Korea = Country("KR")
        val Germany = Country("DE")
        val Japan = Country("JP")
        val Mexico = Country("MX")
        val Norway = Country("NO")
        val Peru = Country("PE")
        val Portugal = Country("PT")
        val Sweden = Country("SE")
        val Taiwan = Country("TW")
        val UnitedStates = Country("US")
        val UnitedKingdom = Country("GB")

        private val isoCountryCodes: Set<String> =
            Locale.getISOCountries().mapTo(mutableSetOf(), String::uppercase)

        fun forIsoCode(value: String): Country {
            val isoCountryCode = sanitizeIsoCountryCodeArgument(value)
            require(isoCountryCode in isoCountryCodes) { "Unknown country code $value" }
            return Country(isoCountryCode)
        }

        @Suppress("SwallowedException")
        fun forIsoCodeOrNull(value: String): Country? {
            val isoCountryCode = sanitizeIsoCountryCodeArgument(value)
            if (isoCountryCode !in isoCountryCodes) return null
            return try {
                Country(isoCountryCode)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        @Suppress("MagicNumber")
        fun forSignature(value: Int): Country? =
            if (value < 0) {
                require(-value shr 16 == 0) { "Invalid country signature $value" }
                val c0 = (-value) and 0xff
                val c1 = (-value) shr 8
                val isoCountryCode = String(charArrayOf(c0.toChar(), c1.toChar()))
                forIsoCode(isoCountryCode)
            } else {
                require(value in 0 until LegacyCountryList.isoCodes.size) { "Invalid country signature $value" }
                val isoCountryCode = LegacyCountryList.isoCodes[value]
                if (isoCountryCode == null) {
                    null
                } else {
                    forIsoCode(isoCountryCode)
                }
            }

        private fun sanitizeIsoCountryCodeArgument(value: String) =
            value.trim().uppercase()

        private fun buildLocaleForCountry(isoCountryCode: String): Locale =
            try {
                Locale.Builder()
                    .setRegion(isoCountryCode)
                    .build()
            } catch (e: IllformedLocaleException) {
                throw IllegalArgumentException(e)
            }

        fun values(): List<Country> =
            isoCountryCodes.map(::Country)
    }

    private object LegacyCountryList {
        val isoCodes = listOf(
            
            null,
            "US",
            "AG",
            "AI",
            "AS",
            "BB",
            "BM",
            "BS",
            "CA",
            "DM",
            "DO",
            "GD",
            "GU",
            "JM",
            "KN",
            "KY",
            "LC",
            "MP",
            "MS",
            "PR",
            "TC",
            "TT",
            "VC",
            "VG",
            "VI",
            "RU",
            "KZ",
            "EG",
            "ZA",
            "GR",
            "NL",
            "BE",
            "FR",
            "ES",
            "HU",
            "IT",
            "RO",
            "CH",
            "AT",
            "GB",
            "GG",
            "IM",
            "JE",
            "DK",
            "SE",
            "NO",
            "PL",
            "DE",
            "PE",
            "MX",
            "CU",
            "AR",
            "BR",
            "CL",
            "CO",
            "VE",
            "MY",
            "AU",
            "ID",
            "PH",
            "NZ",
            "SG",
            "TH",
            "JP",
            "KR",
            "VN",
            "CN",
            "TR",
            "IN",
            "PK",
            "AF",
            "LK",
            "MM",
            "IR",
            "MA",
            "DZ",
            "TN",
            "LY",
            "GM",
            "SN",
            "MR",
            "ML",
            "GN",
            "CI",
            "BF",
            "NE",
            "TG",
            "BJ",
            "MU",
            "LR",
            "SL",
            "GH",
            "NG",
            "TD",
            "CF",
            "CM",
            "CV",
            "ST",
            "GQ",
            "GA",
            "CG",
            "CD",
            "AO",
            "GW",
            "IO",
            "SC",
            "SD",
            "RW",
            "ET",
            "SO",
            "DJ",
            "KE",
            "TZ",
            "UG",
            "BI",
            "MZ",
            "ZM",
            "MG",
            "RE",
            "TF",
            "YT",
            "ZW",
            "NA",
            "MW",
            "LS",
            "BW",
            "SZ",
            "KM",
            "SH",
            "ER",
            "AW",
            "FO",
            "GL",
            "GI",
            "PT",
            "LU",
            "IE",
            "IS",
            "AL",
            "MT",
            "CY",
            "FI",
            "BG",
            "LT",
            "LV",
            "EE",
            "MD",
            "AM",
            "BY",
            "AD",
            "MC",
            "SM",
            "VA",
            "UA",
            "RS",
            "ME",
            "HR",
            "SI",
            "BA",
            "MK",
            "CZ",
            "SK",
            "LI",
            "FK",
            "BZ",
            "GT",
            "SV",
            "HN",
            "NI",
            "CR",
            "PA",
            "PM",
            "HT",
            "GP",
            "BL",
            "MF",
            "BO",
            "GY",
            "EC",
            "GF",
            "PY",
            "MQ",
            "SR",
            "UY",
            "TL",
            "NF",
            "BN",
            "NR",
            "PG",
            "TO",
            "SB",
            "VU",
            "FJ",
            "PW",
            "WF",
            "CK",
            "NU",
            "WS",
            "KI",
            "NC",
            "TV",
            "PF",
            "TK",
            "FM",
            "MH",
            "KP",
            "HK",
            "MO",
            "KH",
            "LA",
            "BD",
            "TW",
            "MV",
            "LB",
            "JO",
            "SY",
            "IQ",
            "KW",
            "SA",
            "YE",
            "OM",
            "PS",
            "AE",
            "IL",
            "BH",
            "QA",
            "BT",
            "MN",
            "NP",
            "TJ",
            "TM",
            "AZ",
            "GE",
            "KG",
            "UZ",
            "AQ",
            "AX",
            "BV",
            "CC",
            "CX",
            "EH",
            "GS",
            "HM",
            "PN",
            "SJ",
            "UM",
            
            null,
        )
    }
}

internal fun String.toCountryOrNull() =
    Country.forIsoCodeOrNull(this)