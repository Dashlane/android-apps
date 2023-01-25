package com.dashlane.autofill.api.emptywebsitewarning.domain

import com.dashlane.vault.summary.SummaryObject



interface EmptyWebsiteWarningDialogResponse {

    

    fun onAutofillResult(result: SummaryObject.Authentifiant)

    

    fun onNoResult()
}