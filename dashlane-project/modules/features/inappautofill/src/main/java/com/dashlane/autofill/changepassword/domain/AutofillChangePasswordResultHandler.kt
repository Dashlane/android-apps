package com.dashlane.autofill.changepassword.domain

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface AutofillChangePasswordResultHandler {
    fun onFinishWithResult(
        result: VaultItem<SyncObject.Authentifiant>,
        oldItem: VaultItem<SyncObject.Authentifiant>
    )

    fun onError(error: AutofillChangePasswordErrors)

    fun onCancel()
}