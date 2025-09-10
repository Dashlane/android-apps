package com.dashlane.wear.credentialmanager

import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.wear.TypeSupportUtil.supportedVaultItemTypes
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class WearDataSaver @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val databaseItemSaver: DatabaseItemSaver,
) : DataSaver {
    override val savedItemFlow: SharedFlow<Unit>
        get() = MutableSharedFlow()

    override suspend fun <T : SyncObject> save(saveRequest: DataSaver.SaveRequest<T>): List<VaultItem<*>> {
        if (saveRequest.itemsToSave.isEmpty()) return emptyList()

        return when (saveRequest.mode) {
            DataSaver.SaveRequest.Mode.INSERT_ONLY -> {
                
                
                emptyList()
            }
            DataSaver.SaveRequest.Mode.INSERT_OR_UPDATE -> {
                handleVaultItemUpdates(saveRequest)
            }
        }
    }

    private suspend fun <T : SyncObject> handleVaultItemUpdates(saveRequest: DataSaver.SaveRequest<T>): List<VaultItem<*>> {
        val itemsToSave = saveRequest.itemsToSave.filter { supportedVaultItemTypes.contains(it.syncObjectType) }

        val filter = vaultFilter {
            ignoreUserLock()
            specificUid(itemsToSave.map { it.uid })
            specificDataType(supportedVaultItemTypes)
        }
        val oldItems = vaultDataQuery.queryAll(filter)
        val oldItemsId = oldItems.map { it.uid }
        val itemsToUpdate = itemsToSave.filter { it.uid in oldItemsId }

        if (itemsToUpdate.isEmpty()) return emptyList()

        return databaseItemSaver.save(
            saveRequest = saveRequest.copy(itemsToSave = itemsToUpdate),
            oldItems = oldItems,
        )
    }

    override suspend fun forceDelete(item: VaultItem<*>): Boolean {
        throw UnsupportedOperationException()
    }
}