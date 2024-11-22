package com.dashlane.autofill.fillresponse.filler

import android.view.View
import com.dashlane.autofill.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.util.getBestEntry
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal open class CreditCardFiller(private val autofillValueFactory: AutofillValueFactory) : Filler {

    override fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean {
        val creditCardItemToFill = item as CreditCardItemToFill
        val cardNumberFieldFound = setCardNumber(dataSetBuilder, summary, creditCardItemToFill, requireLock)
        val securityCodeFieldFound = setSecurityCode(dataSetBuilder, summary, creditCardItemToFill, requireLock)
        val expirationDateFieldFound = setExpirationDate(dataSetBuilder, summary, creditCardItemToFill, requireLock)
        val postalCodeFieldFound = setPostalCode(dataSetBuilder, summary, creditCardItemToFill, requireLock)
        return cardNumberFieldFound || securityCodeFieldFound || expirationDateFieldFound || postalCodeFieldFound
    }

    private fun setCardNumber(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: CreditCardItemToFill,
        requireLock: Boolean
    ): Boolean {
        val value = item.vaultItem?.syncObject?.cardNumber?.takeUnless { requireLock }?.toString() ?: ""
        return fillIfExist(dataSetBuilder, summary, value, AutoFillHint.CREDIT_CARD_NUMBER)
    }

    private fun setSecurityCode(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: CreditCardItemToFill,
        requireLock: Boolean
    ): Boolean {
        val value = item.vaultItem?.syncObject?.securityCode?.takeUnless { requireLock }?.toString() ?: ""
        return fillIfExist(dataSetBuilder, summary, value, AutoFillHint.CREDIT_CARD_SECURITY_CODE)
    }

    protected fun setExpirationDate(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: CreditCardItemToFill,
        requireLock: Boolean
    ): Boolean {
        val month = if (requireLock) Month.JANUARY else item.vaultItem?.syncObject?.expireMonth
        val year = if (requireLock) Year.of(2016) else item.vaultItem?.syncObject?.expireYear
        if (month == null || year == null || year.value !in 2000..3000) {
            
            return false
        }
        var hasValueSet = false
        val yearMonth = YearMonth.of(year.value, month)
        summary.entries.forEach { entry ->
            val fieldFound = when {
                entry.hasHint(AutoFillHint.CREDIT_CARD_EXPIRATION_DATE) ->
                    fillDate(dataSetBuilder, entry, yearMonth)
                entry.hasHint(AutoFillHint.CREDIT_CARD_EXPIRATION_MONTH) ->
                    fillWithValue(dataSetBuilder, entry, month.value, yearMonth)
                entry.hasHint(AutoFillHint.CREDIT_CARD_EXPIRATION_YEAR) ->
                    fillWithValue(dataSetBuilder, entry, year.value, yearMonth)
                entry.hasHint(AutoFillHint.CREDIT_CARD_EXPIRATION_DAY) ->
                    fillWithValue(
                        dataSetBuilder,
                        entry,
                        yearMonth.minusMonths(1).lengthOfMonth(),
                        yearMonth
                    ) 
                else -> false
            }
            hasValueSet = hasValueSet || fieldFound
        }
        return hasValueSet
    }

    private fun setPostalCode(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: CreditCardItemToFill,
        requireLock: Boolean
    ): Boolean {
        val value = if (requireLock) "" else item.zipCode
        return fillIfExist(dataSetBuilder, summary, value, AutoFillHint.POSTAL_CODE)
    }

    private fun fillWithValue(
        dataSetBuilder: DatasetWrapperBuilder,
        entry: AutoFillHintSummary.Entry,
        value: Int,
        yearMonth: YearMonth
    ): Boolean {
        return when (entry.autoFillType) {
            View.AUTOFILL_TYPE_TEXT -> {
                dataSetBuilder.setValue(entry.id, autofillValueFactory.forText(value.toString()))
                true
            }
            View.AUTOFILL_TYPE_LIST -> fillWithValueFromList(dataSetBuilder, entry, value)
            View.AUTOFILL_TYPE_DATE -> fillDate(dataSetBuilder, entry, yearMonth)
            else -> false
        }
    }

    private fun fillDate(
        dataSetBuilder: DatasetWrapperBuilder,
        entry: AutoFillHintSummary.Entry,
        yearMonth: YearMonth
    ): Boolean {
        when (entry.autoFillType) {
            View.AUTOFILL_TYPE_DATE -> {
                val value = yearMonth.atDay(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                dataSetBuilder.setValue(entry.id, autofillValueFactory.forDate(value))
            }
            View.AUTOFILL_TYPE_TEXT -> {
                
                
                val textDate = DateTimeFormatter.ofPattern("MM/yy").format(yearMonth)
                dataSetBuilder.setValue(entry.id, autofillValueFactory.forText(textDate))
            }
            else -> return false
        }
        return true
    }

    private fun fillWithValueFromList(
        dataSetBuilder: DatasetWrapperBuilder,
        entry: AutoFillHintSummary.Entry,
        value: Int
    ): Boolean {
        val index = entry.autoFillOptions.indexOfFirst {
            it == value.toString() || 
                    (value <= 12 && it.startsWith("%02d".format(value))) || 
                    (value > 1000 && it == (value % 100).toString()) 
        }
        return when {
            index != -1 -> {
                dataSetBuilder.setValue(entry.id, autofillValueFactory.forList(index))
                true
            }
            value in (1..entry.autoFillOptions.size) -> {
                dataSetBuilder.setValue(entry.id, autofillValueFactory.forList(value - 1))
                true
            }
            else -> false
        }
    }

    private fun fillIfExist(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        value: String?,
        vararg hintsName: String
    ): Boolean {
        if (value == null) {
            return false
        }
        val entry = summary.getBestEntry { entry ->
            
            hintsName.firstOrNull { entry.hasHint(it) } != null
        } ?: return false
        dataSetBuilder.setValue(entry.id, autofillValueFactory.forText(value))
        return true
    }
}