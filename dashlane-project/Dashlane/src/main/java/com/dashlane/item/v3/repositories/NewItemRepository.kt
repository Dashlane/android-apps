package com.dashlane.item.v3.repositories

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface NewItemRepository {
    suspend fun performAdditionalSteps(itemToSave: VaultItem<SyncObject>)
}