package com.dashlane.item.v3.repositories

import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface GeneratedPasswordRepository {
    suspend fun updateGeneratedPassword(
        data: CredentialFormData,
        itemToSave: VaultItem<SyncObject>
    ): Boolean
}