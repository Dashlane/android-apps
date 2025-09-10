package com.dashlane.autofill.formdetector

import androidx.annotation.IntDef
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

object AutoFillFormType {
    @IntDef(
        UNKNOWN,
        CREDENTIAL,
        CREDIT_CARD,
        EMAIL_ONLY,
        USERNAME_ONLY,
        USERNAME_OR_EMAIL,
        OTP,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class FormType

    const val UNKNOWN = 0
    const val CREDENTIAL = 1
    const val CREDIT_CARD = 2
    const val EMAIL_ONLY = 3
    const val USERNAME_ONLY = 4
    const val USERNAME_OR_EMAIL = 5
    const val OTP = 6

    @FormType
    fun get(focusEntry: AutoFillHintSummary.Entry?, entries: List<AutoFillHintSummary.Entry>): Int {
        if (entries.isEmpty()) {
            return UNKNOWN
        }
        val forPasswordForm = isForPasswordForm(entries)
        focusEntry?.also {
            val forEmail = isForEmail(it)
            val forUsername = isForUsername(it)
            val forOtp = isForOtp(it)
            when {
                forPasswordForm && isForPassword(it) && !forOtp -> return CREDENTIAL
                isForCreditCard(it, entries) -> return CREDIT_CARD
                !forPasswordForm && forEmail && forUsername -> return USERNAME_OR_EMAIL
                !forPasswordForm && forEmail -> return EMAIL_ONLY
                !forPasswordForm && forUsername -> return USERNAME_ONLY
                forOtp && !isForCreditCardForm(entries) -> return OTP
                else -> {
                    
                }
            }
        }
        
        if (forPasswordForm) {
            return CREDENTIAL
        }
        if (isForEmailForm(entries)) {
            return EMAIL_ONLY
        }
        if (isForUsernameForm(entries)) {
            return USERNAME_ONLY
        }
        if (entries.any { isForOtp(it) } && !isForCreditCardForm(entries)) {
            return OTP
        }
        return UNKNOWN
    }

    private fun AutoFillHintSummary.Entry.isCreditCardField(): Boolean {
        return hasOneOfHints(
            arrayOf(
                AutoFillHint.CREDIT_CARD_NUMBER,
                AutoFillHint.CREDIT_CARD_SECURITY_CODE,
                AutoFillHint.CREDIT_CARD_EXPIRATION_DATE,
                AutoFillHint.CREDIT_CARD_EXPIRATION_DAY,
                AutoFillHint.CREDIT_CARD_EXPIRATION_MONTH,
                AutoFillHint.CREDIT_CARD_EXPIRATION_YEAR,
            ),
        )
    }

    private fun isForCreditCard(
        focusEntry: AutoFillHintSummary.Entry,
        entries: List<AutoFillHintSummary.Entry>,
    ): Boolean {
        
        entries.firstOrNull { it.hasHint(AutoFillHint.CREDIT_CARD_NUMBER) } ?: return false

        
        return focusEntry.isCreditCardField()
    }

    private fun isForPasswordForm(entries: List<AutoFillHintSummary.Entry>): Boolean {
        return entries.any { isForPassword(it) }
    }

    private fun isForEmailForm(entries: List<AutoFillHintSummary.Entry>): Boolean {
        return entries.any { isForEmail(it) }
    }

    private fun isForUsernameForm(entries: List<AutoFillHintSummary.Entry>): Boolean {
        return entries.any { isForUsername(it) }
    }

    private fun isForCreditCardForm(entries: List<AutoFillHintSummary.Entry>): Boolean {
        return entries.any { it.isCreditCardField() }
    }

    private fun isForPassword(entry: AutoFillHintSummary.Entry): Boolean {
        return entry.hasOneOfHints(
            arrayOf(
                AutoFillHint.PASSWORD,
                AutoFillHint.CURRENT_PASSWORD,
                AutoFillHint.NEW_PASSWORD,
            ),
        )
    }

    private fun isForEmail(entry: AutoFillHintSummary.Entry): Boolean {
        return entry.hasHint(AutoFillHint.EMAIL_ADDRESS)
    }

    private fun isForUsername(entry: AutoFillHintSummary.Entry): Boolean {
        return entry.hasHint(AutoFillHint.USERNAME)
    }

    private fun isForOtp(entry: AutoFillHintSummary.Entry): Boolean {
        return entry.hasOneOfHints(
            arrayOf(
                AutoFillHint.SMS_OTP_CODE,
                AutoFillHint.SMS_OTP_CODE_1,
                AutoFillHint.SMS_OTP_CODE_2,
                AutoFillHint.SMS_OTP_CODE_3,
                AutoFillHint.SMS_OTP_CODE_4,
                AutoFillHint.SMS_OTP_CODE_5,
                AutoFillHint.SMS_OTP_CODE_6,
                AutoFillHint.SMS_OTP_CODE_7,
                AutoFillHint.SMS_OTP_CODE_8,
                AutoFillHint.SMS_OTP_CODE_9,
            ),
        )
    }
}