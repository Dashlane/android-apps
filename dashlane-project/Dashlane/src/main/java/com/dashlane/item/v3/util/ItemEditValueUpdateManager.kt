package com.dashlane.item.v3.util

import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.item.v3.data.FormData
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface ItemEditValueUpdateManager {
    
    val editedFields: MutableSet<Field>

    fun updateWithData(
        formData: FormData,
        initialVaultItem: VaultItem<SyncObject>
    ): VaultItem<SyncObject>?
}