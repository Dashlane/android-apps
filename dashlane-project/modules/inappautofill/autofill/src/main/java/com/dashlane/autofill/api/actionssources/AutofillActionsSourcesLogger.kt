package com.dashlane.autofill.api.actionssources

import com.dashlane.autofill.api.actionssources.model.ActionedFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource



interface AutofillActionsSourcesLogger {
    fun showList(allFormSources: List<ActionedFormSource>)

    fun clickItem(autoFillFormSource: AutoFillFormSource, numberOfItemsInList: Int)
}
