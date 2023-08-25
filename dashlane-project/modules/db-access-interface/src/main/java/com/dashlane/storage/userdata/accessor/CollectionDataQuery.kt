package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

interface CollectionDataQuery : DataQuery<SummaryObject.Collection, CollectionFilter> {
    fun queryByName(
        name: String,
        filter: CollectionFilter = createFilter()
    ): VaultItem<SyncObject.Collection>?

    fun queryById(id: String): VaultItem<SyncObject.Collection>?

    fun queryByIds(ids: List<String>): List<VaultItem<SyncObject.Collection>>
}