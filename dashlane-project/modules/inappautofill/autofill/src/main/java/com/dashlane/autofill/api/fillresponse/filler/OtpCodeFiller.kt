package com.dashlane.autofill.api.fillresponse.filler

import com.dashlane.autofill.api.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.model.OtpItemToFill
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.api.util.getBestEntry
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

internal class OtpCodeFiller(private val autofillValueFactory: AutofillValueFactory) : Filler {

    override fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean {
        val otpItemToFill = item as OtpItemToFill
        val fullSmsCodeEntry = summary.getBestEntry { it.hasHint(AutoFillHint.SMS_OTP_CODE) }
        if (fullSmsCodeEntry == null) {
            val toFillLength = otpItemToFill.code.length
            val smsOtpCodePerDigit = AutoFillHint.smsOtpCodePerDigit
            if (toFillLength == 0 || toFillLength > smsOtpCodePerDigit.size) {
                return false
            }
            repeat(toFillLength) { i ->
                val hint = smsOtpCodePerDigit[i]
                val entry = summary.getBestEntry { it.hasHint(hint) } ?: return false
                dataSetBuilder.setValue(entry.id, autofillValueFactory.forText(otpItemToFill.code[i].toString()))
            }
            return true
        } else {
            dataSetBuilder.setValue(fullSmsCodeEntry.id, autofillValueFactory.forText(otpItemToFill.code))
            return true
        }
    }

    fun fillNoValue(dataSetBuilder: DatasetWrapperBuilder, summary: AutoFillHintSummary): Boolean {
        val smsOtpCodePerDigit = AutoFillHint.smsOtpCodePerDigit
        val fullSmsCodeEntry = summary.getBestEntry { entry ->
            entry.hasHint(AutoFillHint.SMS_OTP_CODE) || smsOtpCodePerDigit.any { entry.hasHint(it) }
        } ?: return false
        dataSetBuilder.setValue(fullSmsCodeEntry.id, autofillValueFactory.forText(""))
        return true
    }
}