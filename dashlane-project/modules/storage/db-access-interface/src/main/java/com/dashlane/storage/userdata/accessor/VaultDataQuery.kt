package com.dashlane.storage.userdata.accessor

import androidx.annotation.Discouraged
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface VaultDataQuery {
    @Discouraged("Use the suspend version instead")
    fun queryLegacy(filter: VaultFilter): VaultItem<SyncObject>?
    suspend fun query(filter: VaultFilter): VaultItem<SyncObject>?

    @Discouraged("Use the suspend version instead")
    fun queryAllLegacy(filter: VaultFilter): List<VaultItem<SyncObject>>
    suspend fun queryAll(filter: VaultFilter): List<VaultItem<SyncObject>>
}