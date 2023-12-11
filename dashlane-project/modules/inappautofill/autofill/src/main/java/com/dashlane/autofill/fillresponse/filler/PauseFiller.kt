package com.dashlane.autofill.fillresponse.filler

import com.dashlane.autofill.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

internal class PauseFiller(private val autofillValueFactory: AutofillValueFactory) {

    fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary
    ): Boolean {
        summary.entries.map {
            it.id
        }.takeUnless {
            it.isEmpty()
        }?.forEach {
            dataSetBuilder.setValue(it, autofillValueFactory.forText(""))
        } ?: return false

        return true
    }
}