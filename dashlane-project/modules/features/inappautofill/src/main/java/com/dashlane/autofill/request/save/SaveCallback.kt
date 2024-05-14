package com.dashlane.autofill.request.save

import com.dashlane.vault.model.VaultItem

interface SaveCallback {
    fun onSuccess(isUpdate: Boolean, vaultItem: VaultItem<*>)

    fun onFailure(message: CharSequence?)
}
