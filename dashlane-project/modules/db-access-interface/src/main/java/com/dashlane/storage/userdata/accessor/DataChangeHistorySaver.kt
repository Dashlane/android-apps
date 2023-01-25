package com.dashlane.storage.userdata.accessor

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



interface DataChangeHistorySaver {

    

    suspend fun save(dataChangeHistory: List<VaultItem<SyncObject.DataChangeHistory>>): List<VaultItem<SyncObject.DataChangeHistory>>
}