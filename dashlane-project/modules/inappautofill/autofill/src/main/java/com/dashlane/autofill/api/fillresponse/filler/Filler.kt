package com.dashlane.autofill.api.fillresponse.filler

import com.dashlane.autofill.api.fillresponse.DatasetWrapperBuilder
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

internal interface Filler {
    fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean
}