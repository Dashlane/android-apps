package com.dashlane.autofill.emptywebsitewarning.domain

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface EmptyWebsiteWarningDialogResponse {

    fun onAutofillResult(result: VaultItem<SyncObject.Authentifiant>)

    fun onNoResult()
}