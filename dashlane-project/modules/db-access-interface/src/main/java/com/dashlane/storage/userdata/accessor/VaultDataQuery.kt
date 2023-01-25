package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



interface VaultDataQuery {
    fun query(filter: VaultFilter): VaultItem<SyncObject>?
    fun queryAll(filter: VaultFilter): List<VaultItem<SyncObject>>
}