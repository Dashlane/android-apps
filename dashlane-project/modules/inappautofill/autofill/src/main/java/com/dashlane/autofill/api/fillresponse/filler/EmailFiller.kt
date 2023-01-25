package com.dashlane.autofill.api.fillresponse.filler

import com.dashlane.autofill.api.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.api.model.EmailItemToFill
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.api.util.getBestEntry
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary



internal class EmailFiller(private val autofillValueFactory: AutofillValueFactory) : Filler {

    override fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean {
        val email = (item as? EmailItemToFill)?.primaryItem ?: return false
        val entry = summary.getBestEntry {
            it.hasOneOfHints(arrayOf(AutoFillHint.EMAIL_ADDRESS, AutoFillHint.USERNAME))
        } ?: return false
        dataSetBuilder.setValue(entry.id, autofillValueFactory.forText(email.email))
        return true
    }
}