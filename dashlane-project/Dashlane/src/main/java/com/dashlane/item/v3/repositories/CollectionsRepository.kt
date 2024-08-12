package com.dashlane.item.v3.repositories

import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

interface CollectionsRepository {
    suspend fun getCollections(item: SummaryObject): List<CollectionData>
    suspend fun saveCollections(item: VaultItem<SyncObject>, data: CredentialFormData)
}