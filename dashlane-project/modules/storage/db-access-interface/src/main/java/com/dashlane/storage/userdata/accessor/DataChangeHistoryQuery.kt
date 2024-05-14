package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.DataChangeHistoryFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface DataChangeHistoryQuery {
    fun query(filter: DataChangeHistoryFilter): VaultItem<SyncObject.DataChangeHistory>?
}