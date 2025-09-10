package com.dashlane.item.v3.itemtype.creditcard.builder

import javax.inject.Inject
import kotlin.math.min

class CreditCardTypeFormatBuilder @Inject constructor() {
    fun fromNumber(number: String): String =
        getFormat(getCardType(number), number.length)

    fun fromType(type: String?, length: Int): String =
        getFormat(
            cardType = when (type) {
                "PAYMENT_TYPE_VISA" -> CardType.VISA
                "PAYMENT_TYPE_MASTERCARD" -> CardType.MASTERCARD
                "PAYMENT_TYPE_AMEX" -> CardType.AMEX
                "PAYMENT_TYPE_DISCOVER" -> CardType.DISCOVER
                "PAYMENT_TYPE_DINERSCLUB" -> CardType.DINERS_CLUB
                "PAYMENT_TYPE_JCB" -> CardType.JCB
                "PAYMENT_TYPE_CHINA_UNION_PAY" -> CardType.CHINA_UNION_PAY
                "PAYMENT_TYPE_MAESTRO" -> CardType.MAESTRO
                "PAYMENT_TYPE_UATP" -> CardType.UATP
                "PAYMENT_TYPE_DANKORT" -> CardType.DANKORT
                "PAYMENT_TYPE_INTERPAYMENT" -> CardType.INTERPAYMENT
                else -> null
            },
            length = length,
        )

    private fun getFormat(cardType: CardType?, length: Int): String =
        when (cardType) {
            CardType.AMEX -> FOUR_SIX_FIVE
            CardType.DINERS_CLUB -> FOUR_SIX_FOUR
            CardType.UATP -> FOUR_FIVE_FOUR
            CardType.CHINA_UNION_PAY ->
                if (length == 19) {
                    SIX_THIRTEEN
                } else {
                    FOUR_FOUR_FOUR_FOUR
                }

            CardType.MAESTRO ->
                when (length) {
                    13 -> FOUR_FIVE_FOUR
                    15 -> FOUR_SIX_FIVE
                    19 -> FOUR_FOUR_FOUR_FOUR_THREE
                    else -> FOUR_FOUR_FOUR_FOUR
                }

            else -> FOUR_FOUR_FOUR_FOUR
        }

    private fun getCardType(number: String): CardType? {
        if (number.isEmpty()) return null

        return CardType.entries.firstOrNull {
            it.ranges.any { range ->
                val prefix = number.substring(0, min(number.length, range.last.toString().length))
                prefix.toIntOrNull() in range
            }
        }
    }

    private enum class CardType(vararg val ranges: IntRange) {
        VISA(4..4),
        MASTERCARD(51..55, 222100..272099),
        AMEX(34..34, 37..37),
        DISCOVER(6011..6011, 622126..622925, 644..649, 65..65),
        DINERS_CLUB(300..305, 309..309, 36..36, 38..39),
        JCB(3528..3589),
        CHINA_UNION_PAY(62..62),
        MAESTRO(500000..509999, 560000..589999, 600000..699999),
        UATP(1..1),
        DANKORT(5019..5019),
        INTERPAYMENT(636..636),
    }

    private companion object {
        const val FOUR_FOUR_FOUR_FOUR = "#### #### #### ####"
        const val FOUR_FOUR_FOUR_FOUR_THREE = "#### #### #### #### ###"
        const val FOUR_FIVE_FOUR = "#### ##### ####"
        const val FOUR_SIX_FIVE = "#### ###### #####"
        const val FOUR_SIX_FOUR = "#### ###### ####"
        const val SIX_THIRTEEN = "###### #############"
    }
}