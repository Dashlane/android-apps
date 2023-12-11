package com.dashlane.autofill.fillresponse.filler

import com.dashlane.autofill.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

internal interface Filler {
    fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean
}