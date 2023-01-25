package com.dashlane.autofill.api.fillresponse.filler

import com.dashlane.autofill.api.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.api.model.AuthentifiantSummaryItemToFill
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.api.util.getBestEntry
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.summary.SummaryObject



internal open class AuthentifiantFiller(private val autofillValueFactory: AutofillValueFactory) : Filler {

    @Suppress("UNCHECKED_CAST")
    override fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean {
        val authentifiant = (item as? AuthentifiantSummaryItemToFill)?.primaryItem ?: return false
        val loginFieldFound = setLogin(dataSetBuilder, summary, authentifiant)
        val passwordFieldFound = setPassword(dataSetBuilder, summary, "")
        return loginFieldFound || passwordFieldFound
    }

    protected fun setLogin(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: SummaryObject.Authentifiant
    ): Boolean {
        val login = item.loginForUi ?: return false

        if (login.contains("@") && setEmailAddress(dataSetBuilder, summary, login)) {
            
            return true
        }
        
        val entry = summary.getBestEntry { it.hasHint(AutoFillHint.USERNAME) }
            ?: summary.getBestEntry { it.hasHint(AutoFillHint.EMAIL_ADDRESS) } 
            ?: return false
        dataSetBuilder.setValue(entry.id, autofillValueFactory.forText(login))
        return true
    }

    private fun setEmailAddress(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        login: String
    ): Boolean {
        val entry = summary.getBestEntry { it.hasHint(AutoFillHint.EMAIL_ADDRESS) } ?: return false
        dataSetBuilder.setValue(entry.id, autofillValueFactory.forText(login))
        return true
    }

    protected fun setPassword(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        password: String,
        oldPassword: String? = null
    ): Boolean {
        val newPasswordEntries = summary.entries.filter { it.hasHint(AutoFillHint.NEW_PASSWORD) }
        val entry = summary.getBestEntry {
            it.hasOneOfHints(arrayOf(AutoFillHint.PASSWORD, AutoFillHint.CURRENT_PASSWORD))
        }
        if (newPasswordEntries.isEmpty() && entry == null) {
            return false
        }
        if (oldPassword != null) {
            if (newPasswordEntries.isNotEmpty()) {
                
                entry?.let { dataSetBuilder.setValue(it.id, autofillValueFactory.forText(oldPassword)) }
                newPasswordEntries.forEach { dataSetBuilder.setValue(it.id, autofillValueFactory.forText(password)) }
                return true
            }
            val otherPasswordEntries = summary.entries.filter {
                it.hasOneOfHints(arrayOf(AutoFillHint.PASSWORD, AutoFillHint.CURRENT_PASSWORD)) && it != entry
            }
            if (otherPasswordEntries.isNotEmpty()) {
                
                entry?.let { dataSetBuilder.setValue(it.id, autofillValueFactory.forText(oldPassword)) }
                otherPasswordEntries.forEach { dataSetBuilder.setValue(it.id, autofillValueFactory.forText(password)) }
                return true
            }
        }
        
        val bestEntry = entry ?: newPasswordEntries.first()
        dataSetBuilder.setValue(bestEntry.id, autofillValueFactory.forText(password))
        return true
    }
}