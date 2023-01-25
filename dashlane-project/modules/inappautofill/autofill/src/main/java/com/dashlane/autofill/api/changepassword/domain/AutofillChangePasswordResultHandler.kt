package com.dashlane.autofill.api.changepassword.domain

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



interface AutofillChangePasswordResultHandler {
    

    fun onFinishWithResult(result: VaultItem<SyncObject.Authentifiant>, oldItem: SyncObject.Authentifiant)

    

    fun onError(error: AutofillChangePasswordErrors)

    

    fun onCancel()
}