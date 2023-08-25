package com.dashlane.autofill.api.request.save

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.util.time.toMonthOrNull
import com.dashlane.util.time.toYearOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.Month
import java.time.Year
import java.time.ZoneId

internal class SaveCreditCardRequest(
    summary: AutoFillHintSummary,
    usageLog: AutofillAnalyzerDef.IAutofillUsageLog,
    databaseAccess: AutofillAnalyzerDef.DatabaseAccess
) : SaveRequest(summary, usageLog, databaseAccess) {

    override fun execute(
        context: Context,
        coroutineScope: CoroutineScope,
        saveCallback: SaveCallback,
        hasInline: Boolean
    ) {
        var number: String? = null
        var expireMonth: Month? = null
        var expireYear: Year? = null
        var securityCode: String? = null

        summary.entries.forEach {
            when {
                number == null && it.hasHint(AutoFillHint.CREDIT_CARD_NUMBER) -> {
                    number = it.autoFillValueString
                }
                securityCode == null && it.hasHint(AutoFillHint.CREDIT_CARD_SECURITY_CODE) -> {
                    securityCode = it.autoFillValueString
                }
                expireYear == null && it.hasHint(AutoFillHint.CREDIT_CARD_EXPIRATION_YEAR) -> {
                    expireYear = it.autoFillValueString?.toYearOrNull()
                }
                expireMonth == null && it.hasHint(AutoFillHint.CREDIT_CARD_EXPIRATION_MONTH) -> {
                    expireMonth = it.autoFillValueString?.convertToMonth()
                }
                (expireMonth == null || expireYear == null) &&
                        it.hasOneOfHints(arrayOf(AutoFillHint.CREDIT_CARD_EXPIRATION_DATE)) -> {
                    val expireDate = it.autoFillValueTimestamp
                    if (expireDate > 0) {
                        val dateTime =
                            Instant.ofEpochMilli(expireDate).atZone(ZoneId.systemDefault())
                        expireYear = dateTime.year.let(Year::of)
                        expireMonth = dateTime.month
                    }
                }
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            val creditCard = databaseAccess.saveCreditCard(number, securityCode, expireMonth, expireYear)
            if (creditCard != null) {
                saveCallback.onSuccess(isUpdate = false, vaultItem = creditCard)
            } else {
                notifyLogout(saveCallback, hasInline)
            }
        }
    }

    override fun notifyLogout(
        callback: SaveCallback,
        forKeyboard: Boolean
    ) {
        super.notifyLogout(callback)
    }
}

private fun String.convertToMonth(): Month? {
    
    val monthInt = toIntOrNull()
    return if (monthInt == null) {
        when {
            
            startsWith("Jul", true) || startsWith("Juil", true) -> Month.JULY
            
            startsWith("Ju", true) -> Month.JUNE
            
            startsWith("Au", true) -> Month.AUGUST
            
            startsWith("J", true) -> Month.JANUARY
            
            startsWith("F", true) -> Month.FEBRUARY
            
            startsWith("Mar", true) -> Month.MARCH
            
            startsWith("A", true) -> Month.APRIL
            
            startsWith("M", true) -> Month.MAY
            
            startsWith("S", true) -> Month.SEPTEMBER
            
            startsWith("O", true) -> Month.OCTOBER
            
            startsWith("N", true) -> Month.NOVEMBER
            
            startsWith("D", true) -> Month.DECEMBER
            else -> null 
        }
    } else {
        monthInt.toMonthOrNull()
    }
}
