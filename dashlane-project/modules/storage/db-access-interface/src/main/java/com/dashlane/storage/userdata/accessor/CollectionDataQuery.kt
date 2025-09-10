package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

interface CollectionDataQuery : DataQuery<SummaryObject.Collection, CollectionFilter> {
    suspend fun queryByName(
        name: String,
        filter: CollectionFilter = createFilter(),
    ): VaultItem<SyncObject.Collection>?

    suspend fun queryById(id: String): VaultItem<SyncObject.Collection>?

    suspend fun queryByIds(ids: List<String>): List<VaultItem<SyncObject.Collection>>

    suspend fun queryVaultItemsWithCollectionId(id: String): List<SummaryObject>
}