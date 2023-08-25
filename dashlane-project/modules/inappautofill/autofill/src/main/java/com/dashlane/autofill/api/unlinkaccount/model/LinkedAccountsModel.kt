package com.dashlane.autofill.api.unlinkaccount.model

import com.dashlane.vault.summary.SummaryObject

data class LinkedAccountsModel(
    val processing: Boolean = false,
    val linkedAccounts: List<SummaryObject.Authentifiant> = listOf(),
    val autoFillFormSourceTitle: String
)