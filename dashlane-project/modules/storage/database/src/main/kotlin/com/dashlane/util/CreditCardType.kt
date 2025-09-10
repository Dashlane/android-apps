package com.dashlane.util

import com.dashlane.xml.domain.SyncObfuscatedValue

enum class CreditCardType(val typeName: String) {
    VISA("PAYMENT_TYPE_VISA"),
    MASTERCARD("PAYMENT_TYPE_MASTERCARD"),
    AMEX("PAYMENT_TYPE_AMEX"),
    DISCOVER("PAYMENT_TYPE_DISCOVER"),
    DINERS_CLUB("PAYMENT_TYPE_DINERSCLUB"),
    JCB("PAYMENT_TYPE_JCB"),
    CHINA_UNION_PAY("PAYMENT_TYPE_CHINA_UNION_PAY"),
    MAESTRO("PAYMENT_TYPE_MAESTRO"),
    UATP("PAYMENT_TYPE_UATP"),
    DANKORT("PAYMENT_TYPE_DANKORT"),
    INTERPAYMENT("PAYMENT_TYPE_INTERPAYMENT"),
    ;

    companion object {
        fun fromCardNumber(cardNumber: SyncObfuscatedValue?): CreditCardType? {
            if (cardNumber == null) return null

            
            val prefix = runCatching {
                cardNumber.substring(0, 6).toInt()
            }.getOrNull() ?: return null

            
            
            val length = cardNumber.length
            return when {
                prefix.isVisa(length) -> VISA
                prefix.isAmex(length) -> AMEX
                prefix.isJcb(length) -> JCB
                prefix.isMasterCard(length) -> MASTERCARD
                prefix.isDinersClub(length) -> DINERS_CLUB
                prefix.isDiscover(length) -> DISCOVER
                prefix.isChinaUnionPay(length) -> CHINA_UNION_PAY
                prefix.isDankort(length) -> DANKORT
                prefix.isUatp(length) -> UATP
                prefix.isInterpayment(length) -> INTERPAYMENT
                prefix.isMaestro(length) -> MAESTRO
                else -> null
            }
        }

        private fun Int.isVisa(length: Int) = this in 400_000..499_999 && length == 16

        private fun Int.isAmex(length: Int) =
            when (this) {
                in 340_000..349_999,
                in 370_000..379_999,
                -> true
                else -> false
            } && length == 15

        private fun Int.isJcb(length: Int) = this in 352_800..358_999 && length in 16..19

        private fun Int.isMasterCard(length: Int) = when (this) {
            in 510_000..559_999,
            in 222_100..272_099,
            -> true
            else -> false
        } && length == 16

        private fun Int.isDinersClub(length: Int): Boolean {
            val range1 = when (this) {
                in 360_000..369_999,
                in 300_000..305_999,
                -> true
                else -> false
            } && length in 14..19
            if (range1) return range1

            return when (this) {
                in 380_000..399_999,
                in 309_500..309_599,
                -> true
                else -> false
            } && length in 16..19
        }

        private fun Int.isDiscover(length: Int) = when (this) {
            in 640_000..659_999,
            in 601_100..601_199,
            in 622_126..622_925,
            in 624_000..626_999,
            in 628_200..628_899,
            -> true
            else -> false
        } && length in 16..19

        private fun Int.isChinaUnionPay(length: Int) = this in 620_000..629_999 && length in 16..19

        private fun Int.isMaestro(length: Int) = when (this) {
            in 500_000..509_999,
            in 560_000..589_999,
            in 600_000..699_999,
            -> true
            else -> false
        } && length in 12..19

        private fun Int.isUatp(length: Int) = this in 100_000..199_999 && length == 15

        private fun Int.isDankort(length: Int) = this in 501_900..501_999 && length == 16

        private fun Int.isInterpayment(length: Int) = this in 636_000..636_999 && length in 16..19
    }
}