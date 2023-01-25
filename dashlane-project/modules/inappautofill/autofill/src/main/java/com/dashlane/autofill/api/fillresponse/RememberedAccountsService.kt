package com.dashlane.autofill.api.fillresponse

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.vault.summary.SummaryObject



interface RememberedAccountsService {
    suspend fun fetchRememberedAccounts(formSource: AutoFillFormSource): List<SummaryObject.Authentifiant>?
    suspend fun isAccountRemembered(formSource: AutoFillFormSource, authentifiantId: String): Boolean
}
