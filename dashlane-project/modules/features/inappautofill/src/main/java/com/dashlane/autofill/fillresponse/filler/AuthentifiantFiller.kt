package com.dashlane.autofill.fillresponse.filler

import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary.Entry
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.util.getClosestEntryOrNull
import com.dashlane.util.isValidEmail

internal open class AuthentifiantFiller(
    private val autofillValueFactory: AutofillValueFactory,
) : Filler {
    override fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean,
    ): Boolean {
        val authentifiantItem = item as AuthentifiantItemToFill
        val focusedEntry = summary.focusEntry
        val hasFilled = when {
            focusedEntry.isForPassword() -> fillPasswordFirst(
                dataSetBuilder = dataSetBuilder,
                summary = summary,
                item = authentifiantItem,
                focusedEntry = focusedEntry,
                requireLock = requireLock,
            )
            else -> fillLoginFirst(
                dataSetBuilder = dataSetBuilder,
                summary = summary,
                item = authentifiantItem,
                focusedEntry = focusedEntry,
                requireLock = requireLock,
            )
        }
        return hasFilled
    }

    private fun fillPasswordFirst(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: AuthentifiantItemToFill,
        focusedEntry: Entry?,
        requireLock: Boolean,
    ): Boolean {
        val filledPassword = setPassword(
            dataSetBuilder = dataSetBuilder,
            summary = summary,
            referenceEntry = focusedEntry,
            item = item,
            requireLock = requireLock,
        )
        val filledLogin = setLogin(
            dataSetBuilder = dataSetBuilder,
            summary = summary,
            item = item,
            referenceEntry = filledPassword ?: focusedEntry,
        )
        return filledPassword != null || filledLogin != null
    }

    private fun fillLoginFirst(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: AuthentifiantItemToFill,
        focusedEntry: Entry?,
        requireLock: Boolean,
    ): Boolean {
        val filledLogin = setLogin(
            dataSetBuilder = dataSetBuilder,
            summary = summary,
            item = item,
            referenceEntry = focusedEntry,
        )
        val filledPassword = setPassword(
            dataSetBuilder = dataSetBuilder,
            summary = summary,
            item = item,
            referenceEntry = filledLogin ?: focusedEntry,
            requireLock = requireLock,
        )
        return filledPassword != null || filledLogin != null
    }

    private fun setLogin(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: AuthentifiantItemToFill,
        referenceEntry: Entry?,
    ): Entry? {
        val login = item.login ?: return null
        return if (login.isValidEmail()) {
            setEmailAddress(
                dataSetBuilder = dataSetBuilder,
                summary = summary,
                login = login,
                referenceEntry = referenceEntry,
            ) ?: setUsername(
                dataSetBuilder = dataSetBuilder,
                summary = summary,
                login = login,
                referenceEntry = referenceEntry,
            )
        } else {
            setUsername(
                dataSetBuilder = dataSetBuilder,
                summary = summary,
                login = login,
                referenceEntry = referenceEntry,
            )
        }
    }

    private fun setEmailAddress(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        login: String,
        referenceEntry: Entry?,
    ): Entry? {
        
        val entry = summary.getClosestEntryOrNull(referenceEntry = referenceEntry) {
            it.hasHint(AutoFillHint.EMAIL_ADDRESS)
        } ?: return null

        dataSetBuilder.setValue(id = entry.id, value = autofillValueFactory.forText(login))
        return entry
    }

    private fun setUsername(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        login: String,
        referenceEntry: Entry?,
    ): Entry? {
        
        val loginEntry = summary.getClosestEntryOrNull(referenceEntry = referenceEntry) {
            it.hasHint(AutoFillHint.USERNAME)
        } ?: summary.getClosestEntryOrNull(referenceEntry = referenceEntry) {
            
            it.hasHint(AutoFillHint.EMAIL_ADDRESS)
        } ?: return null

        dataSetBuilder.setValue(id = loginEntry.id, value = autofillValueFactory.forText(login))
        return loginEntry
    }

    private fun setPassword(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: AuthentifiantItemToFill,
        referenceEntry: Entry?,
        requireLock: Boolean,
    ): Entry? {
        val password = item.vaultItem?.syncObject?.password
            .takeUnless { requireLock }?.toString()
            ?: ""
        val oldPassword = item.oldPassword?.toString()
        val newPasswordEntries = summary.entries.filter { it.hasHint(AutoFillHint.NEW_PASSWORD) }
        val passwordEntry = summary.getClosestEntryOrNull(referenceEntry = referenceEntry) {
            it.hasOneOfHints(arrayOf(AutoFillHint.PASSWORD, AutoFillHint.CURRENT_PASSWORD))
        }
        if (newPasswordEntries.isEmpty() && passwordEntry == null) {
            return null
        }
        if (oldPassword != null) {
            if (newPasswordEntries.isNotEmpty()) {
                
                passwordEntry?.let {
                    dataSetBuilder.setValue(
                        id = it.id,
                        value = autofillValueFactory.forText(oldPassword),
                    )
                }
                newPasswordEntries.forEach {
                    dataSetBuilder.setValue(
                        id = it.id,
                        value = autofillValueFactory.forText(password),
                    )
                }
                return passwordEntry
            }
            val otherPasswordEntries = summary.entries.filter {
                it.hasOneOfHints(
                    arrayOf(
                        AutoFillHint.PASSWORD,
                        AutoFillHint.CURRENT_PASSWORD,
                    ),
                ) && it != passwordEntry
            }
            if (otherPasswordEntries.isNotEmpty()) {
                
                passwordEntry?.let {
                    dataSetBuilder.setValue(
                        id = it.id,
                        value = autofillValueFactory.forText(oldPassword),
                    )
                }
                otherPasswordEntries.forEach {
                    dataSetBuilder.setValue(
                        id = it.id,
                        value = autofillValueFactory.forText(password),
                    )
                }
                return passwordEntry
            }
        }
        
        val bestEntry = passwordEntry ?: newPasswordEntries.first()
        dataSetBuilder.setValue(id = bestEntry.id, value = autofillValueFactory.forText(password))
        return bestEntry
    }

    private fun Entry?.isForPassword() = this?.hasOneOfHints(
        arrayOf(
            AutoFillHint.PASSWORD,
            AutoFillHint.CURRENT_PASSWORD,
        ),
    ) == true
}