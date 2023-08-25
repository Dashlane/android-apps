package com.dashlane.autofill.api.fillresponse.filler

import com.dashlane.autofill.api.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

internal class ViewAllAccountsFiller(private val autofillValueFactory: AutofillValueFactory) {

    fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary
    ): Boolean {
        summary.entries.filter {
            it.hasOneOfHints(arrayOf(AutoFillHint.EMAIL_ADDRESS, AutoFillHint.USERNAME, AutoFillHint.PASSWORD))
        }.map {
            it.id
        }.takeUnless {
            it.isEmpty()
        }?.forEach {
            dataSetBuilder.setValue(it, autofillValueFactory.forText(""))
        } ?: return false

        return true
    }
}