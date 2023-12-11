package com.dashlane.autofill.createaccount.domain

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface AutofillCreateAccountResultHandler {
    fun onFinishWithResult(result: VaultItem<SyncObject.Authentifiant>)

    fun onError(error: AutofillCreateAccountErrors)

    fun onCancel()
}