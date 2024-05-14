package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataChangeHistorySaverRaclette @Inject constructor(
    private val databaseItemSaver: Lazy<DatabaseItemSaver>
) : DataChangeHistorySaver {
    override suspend fun save(dataChangeHistory: List<VaultItem<SyncObject.DataChangeHistory>>): List<VaultItem<SyncObject.DataChangeHistory>> {
        if (dataChangeHistory.isEmpty()) return emptyList()
        val saveRequest = DataSaver.SaveRequest(
            itemsToSave = dataChangeHistory
        )
        return databaseItemSaver.get().save(saveRequest, emptyList())
    }
}
