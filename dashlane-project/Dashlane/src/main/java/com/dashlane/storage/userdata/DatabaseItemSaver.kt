package com.dashlane.storage.userdata

import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface DatabaseItemSaver {
    suspend fun delete(item: VaultItem<*>): Boolean
    suspend fun <T : SyncObject> save(saveRequest: DataSaver.SaveRequest<T>, oldItems: List<VaultItem<*>>): List<VaultItem<T>>
}