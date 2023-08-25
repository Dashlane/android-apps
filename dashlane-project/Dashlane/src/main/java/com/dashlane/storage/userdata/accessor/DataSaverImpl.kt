package com.dashlane.storage.userdata.accessor

import com.dashlane.core.history.AuthentifiantHistoryGenerator
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierDeletedEvent
import com.dashlane.events.PasswordChangedEvent
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.objectType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSaverImpl @Inject constructor(
    private val authentifiantHistoryGenerator: AuthentifiantHistoryGenerator,
    private val dataStorageProvider: DataStorageProvider,
    private val appEvents: AppEvents
) : DataSaver {
    private val dataChangeHistorySaver: DataChangeHistorySaver
        get() = dataStorageProvider.dataChangeHistorySaver
    private val vaultDataQuery: VaultDataQuery
        get() = dataStorageProvider.vaultDataQuery
    private val databaseItemSaver: DatabaseItemSaver
        get() = dataStorageProvider.itemSaver

    override suspend fun <T : SyncObject> save(saveRequest: DataSaver.SaveRequest<T>): List<VaultItem<*>> {
        if (saveRequest.itemsToSave.isEmpty()) return emptyList()
        
        val (regularItems, dataChangeHistories) = saveRequest.splitWithDataChangeHistory()

        if (Thread.interrupted()) {
            return emptyList()
        }

        val dataChangeHistoryItems = dataChangeHistories.itemsToSave.toMutableList()

        val savedRegularItems = when (regularItems.mode) {
            DataSaver.SaveRequest.Mode.INSERT_ONLY -> {
                
                dataChangeHistoryItems.addAll(regularItems.itemsToSave.generateDataChangeHistories())
                
                
                databaseItemSaver.save(regularItems, emptyList())
            }
            DataSaver.SaveRequest.Mode.INSERT_OR_UPDATE -> {
                
                val oldItems = loadDatabaseItems(regularItems.itemsToSave)
                if (Thread.interrupted()) {
                    return emptyList()
                }

                
                dataChangeHistoryItems.addAll(regularItems.itemsToSave.generateDataChangeHistories(oldItems))

                
                val saveResult = databaseItemSaver.save(regularItems, oldItems)

                
                regularItems.itemsToSave.forEach { item ->
                    oldItems.firstOrNull { it.uid == item.uid }?.let { oldItem ->
                        checkPasswordChanged(oldItem, item)
                    }
                    if (item.isDeleted()) {
                        appEvents.post(DataIdentifierDeletedEvent(item.syncObjectType))
                    }
                }
                saveResult
            }
        }

        
        val savedDataChangeHistories = dataChangeHistorySaver.save(dataChangeHistoryItems)

        return savedRegularItems + savedDataChangeHistories
    }

    private fun checkPasswordChanged(oldItem: VaultItem<*>, item: VaultItem<*>) {
        if (item.syncObjectType == SyncObjectType.AUTHENTIFIANT &&
            oldItem.syncObjectType == SyncObjectType.AUTHENTIFIANT &&
            (item.syncObject as SyncObject.Authentifiant).password != (oldItem.syncObject as SyncObject.Authentifiant).password
        ) {
            appEvents.post(PasswordChangedEvent())
        }
    }

    private fun loadDatabaseItems(itemsToSave: List<VaultItem<*>>): List<VaultItem<*>> {
        
        return itemsToSave.groupBy { it.syncObject.objectType }
            .map { (dataType, items) ->
                
                val filter = vaultFilter {
                    ignoreUserLock()
                    specificUid(items.map { it.uid })
                    specificDataType(dataType)
                }
                vaultDataQuery.queryAll(filter)
            }.flatten()
    }

    override suspend fun forceDelete(item: VaultItem<*>): Boolean {
        return databaseItemSaver.delete(item)
    }

    private fun <T : SyncObject> DataSaver.SaveRequest<T>.splitWithDataChangeHistory():
            Pair<DataSaver.SaveRequest<T>, DataSaver.SaveRequest<SyncObject.DataChangeHistory>> {
        val dataChangeHistory: List<VaultItem<SyncObject.DataChangeHistory>> = itemsToSave.mapNotNull { item ->
            val syncObject: SyncObject = item.syncObject
            if (syncObject is SyncObject.DataChangeHistory) {
                @Suppress("UNCHECKED_CAST")
                item as VaultItem<SyncObject.DataChangeHistory>
            } else {
                null
            }
        }
        val leftItems = itemsToSave.filterNot { item -> dataChangeHistory.any { it == item } }
        return copy(itemsToSave = leftItems) to
                DataSaver.SaveRequest(
                    itemsToSave = dataChangeHistory,
                    origin = origin,
                    mode = mode
                )
    }

    private fun <T : SyncObject> List<VaultItem<T>>.generateDataChangeHistories(oldItems: List<VaultItem<*>>? = null):
            List<VaultItem<SyncObject.DataChangeHistory>> {
        return mapNotNull { item ->
            val syncObject: SyncObject = item.syncObject
            @Suppress("UNCHECKED_CAST")
            if (syncObject is SyncObject.Authentifiant) {
                val oldItem = oldItems?.firstOrNull { it.uid == item.uid }
                authentifiantHistoryGenerator.getDataChangeHistoryToSave(
                    oldItem as? VaultItem<SyncObject.Authentifiant>,
                    item as VaultItem<SyncObject.Authentifiant>
                )
            } else {
                null
            }
        }
    }
}