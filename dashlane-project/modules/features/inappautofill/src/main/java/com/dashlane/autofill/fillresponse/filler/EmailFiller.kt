package com.dashlane.autofill.fillresponse.filler

import com.dashlane.autofill.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.model.EmailItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.util.getBestEntry
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

internal class EmailFiller(private val autofillValueFactory: AutofillValueFactory) : Filler {

    override fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean {
        val emailItemToFill = item as EmailItemToFill
        val entry = summary.getBestEntry {
            it.hasOneOfHints(arrayOf(AutoFillHint.EMAIL_ADDRESS, AutoFillHint.USERNAME))
        } ?: return false
        dataSetBuilder.setValue(entry.id, autofillValueFactory.forText(emailItemToFill.email))
        return true
    }
}