package com.dashlane.autofill.api.internal

import com.dashlane.vault.summary.SummaryObject

interface FetchAccounts {
    fun fetchAccount(authentifiantIds: List<String>): List<SummaryObject.Authentifiant>?
}
