package com.dashlane.autofill.api.rememberaccount.view

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.vault.summary.SummaryObject

sealed class AutofillLinkServiceState {
    object Initial : AutofillLinkServiceState()
    data class OnDataLoaded(val item: SummaryObject.Authentifiant, val formSource: AutoFillFormSource) :
        AutofillLinkServiceState()
}