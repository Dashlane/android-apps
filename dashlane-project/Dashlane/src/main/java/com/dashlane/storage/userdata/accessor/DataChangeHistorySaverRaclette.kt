package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import dagger.Lazy
import javax.inject.Inject

class DataChangeHistorySaverRaclette @Inject constructor(
    private val dataStorageProvider: Lazy<DataStorageProvider>
) : DataChangeHistorySaver {
    private val databaseItemSaver: DatabaseItemSaver
        get() = dataStorageProvider.get().itemSaver

    override suspend fun save(dataChangeHistory: List<VaultItem<SyncObject.DataChangeHistory>>): List<VaultItem<SyncObject.DataChangeHistory>> {
        if (dataChangeHistory.isEmpty()) return emptyList()
        val saveRequest = DataSaver.SaveRequest(
            itemsToSave = dataChangeHistory
        )
        return databaseItemSaver.save(saveRequest, emptyList())
    }
}
