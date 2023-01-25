package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.internal.DataChangeHistoryDaoInternal
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DataChangeHistorySaverLegacy @Inject constructor(
    private val dataChangeHistoryDaoInternal: DataChangeHistoryDaoInternal
) : DataChangeHistorySaver {
    override suspend fun save(dataChangeHistory: List<VaultItem<SyncObject.DataChangeHistory>>): List<VaultItem<SyncObject.DataChangeHistory>> {
        return withContext(Dispatchers.IO) {
            dataChangeHistory.filter {
                dataChangeHistoryDaoInternal.save(it)
            }
        }
    }
}
