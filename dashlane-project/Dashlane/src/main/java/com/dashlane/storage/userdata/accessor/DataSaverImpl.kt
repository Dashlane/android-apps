package com.dashlane.storage.userdata.accessor

import com.dashlane.core.history.AuthentifiantHistoryGenerator
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierDeletedEvent
import com.dashlane.events.PasswordChangedEvent
import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copyWithSpaceId
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.objectType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class DataSaverImpl @Inject constructor(
    private val authentifiantHistoryGenerator: AuthentifiantHistoryGenerator,
    private val vaultDataQuery: VaultDataQuery,
    private val dataChangeHistorySaver: DataChangeHistorySaver,
    private val databaseItemSaver: DatabaseItemSaver,
    private val appEvents: AppEvents,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>
) : DataSaver {
    
    private val _savedItemFlow = MutableSharedFlow<Unit>()
    override val savedItemFlow = _savedItemFlow.asSharedFlow()

    override suspend fun <T : SyncObject> save(saveRequest: DataSaver.SaveRequest<T>): List<VaultItem<*>> {
        if (saveRequest.itemsToSave.isEmpty()) return emptyList()
        
        val (regularItems, dataChangeHistories) = saveRequest.splitWithDataChangeHistory()

        if (Thread.interrupted()) {
            return emptyList()
        }

        val dataChangeHistoryItems = dataChangeHistories.itemsToSave.toMutableList()
        val enforcedSpacedId = teamSpaceAccessorProvider.get()?.enforcedSpace?.teamId

        val savedRegularItems = when (regularItems.mode) {
            DataSaver.SaveRequest.Mode.INSERT_ONLY -> {
                val newRegularItems = regularItems.copyWithNewItemsToSave(
                    itemsToSave = regularItems.itemsToSave.enforceSpaceIfRequired(enforcedSpacedId)
                )
                
                dataChangeHistoryItems.addAll(newRegularItems.itemsToSave.generateDataChangeHistories())
                
                
                databaseItemSaver.save(newRegularItems, emptyList())
            }
            DataSaver.SaveRequest.Mode.INSERT_OR_UPDATE -> {
                
                val (oldItems, newItems) = loadDatabaseItems(regularItems.itemsToSave)

                val newRegularItems = regularItems.copyWithNewItemsToSave(
                    itemsToSave = regularItems.itemsToSave.replaceItems(newItems.enforceSpaceIfRequired(enforcedSpacedId))
                )

                if (Thread.interrupted()) {
                    return emptyList()
                }

                
                dataChangeHistoryItems.addAll(newRegularItems.itemsToSave.generateDataChangeHistories(oldItems))

                
                val saveResult = databaseItemSaver.save(newRegularItems, oldItems)

                
                newRegularItems.itemsToSave.forEach { item ->
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

        _savedItemFlow.emit(Unit)

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

    private fun loadDatabaseItems(itemsToSave: List<VaultItem<*>>): Pair<List<VaultItem<*>>, List<VaultItem<*>>> {
        
        val existingItems = itemsToSave.groupBy { it.syncObject.objectType }
            .map { (dataType, items) ->
                
                val filter = vaultFilter {
                    ignoreUserLock()
                    specificUid(items.map { it.uid })
                    specificDataType(dataType)
                }
                vaultDataQuery.queryAll(filter)
            }.flatten()
        val newItems = itemsToSave.filterExistingItems(existingItems)
        return existingItems to newItems
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
        return copy(itemsToSave = leftItems) to DataSaver.SaveRequest(
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

    private fun <T : SyncObject> List<VaultItem<T>>.enforceSpaceIfRequired(spacedId: String?): List<VaultItem<T>> {
        spacedId ?: return this

        return map { vaultItem ->
            when (vaultItem.syncObjectType) {
                
                
                SyncObjectType.ADDRESS,
                SyncObjectType.AUTHENTIFIANT,
                SyncObjectType.BANK_STATEMENT,
                SyncObjectType.COLLECTION,
                SyncObjectType.COMPANY,
                SyncObjectType.DRIVER_LICENCE,
                SyncObjectType.EMAIL,
                SyncObjectType.FISCAL_STATEMENT,
                SyncObjectType.ID_CARD,
                SyncObjectType.IDENTITY,
                SyncObjectType.PASSKEY,
                SyncObjectType.PASSPORT,
                SyncObjectType.PAYMENT_CREDIT_CARD,
                SyncObjectType.PAYMENT_PAYPAL,
                SyncObjectType.PERSONAL_WEBSITE,
                SyncObjectType.PHONE,
                SyncObjectType.SECURE_NOTE,
                SyncObjectType.SOCIAL_SECURITY_STATEMENT -> vaultItem.copyWithSpaceId(spacedId)
                else -> vaultItem
            }
        }
    }

    private fun <T : SyncObject> DataSaver.SaveRequest<T>.copyWithNewItemsToSave(itemsToSave: List<VaultItem<*>>): DataSaver.SaveRequest<T> = copy(
        itemsToSave = itemsToSave.filterIsInstance<VaultItem<T>>()

    )

    private fun <U : SyncObject, V : SyncObject> List<VaultItem<U>>.filterExistingItems(items: List<VaultItem<V>>): List<VaultItem<U>> {
        val itemIds = items.map { it.uid }
        return this.filterNot { it.uid in itemIds }
    }

    private fun <E : SyncObject> List<VaultItem<E>>.replaceItems(items: List<VaultItem<E>>): List<VaultItem<E>> {
        val itemIds = items.map { it.uid }
        return filterNot { it.uid in itemIds } + items
    }
}
