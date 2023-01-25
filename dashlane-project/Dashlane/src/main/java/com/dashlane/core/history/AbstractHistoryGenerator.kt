package com.dashlane.core.history

import com.dashlane.logger.Log
import com.dashlane.logger.v
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.filter.DataChangeHistoryFilter
import com.dashlane.vault.history.title
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.util.get
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType



abstract class AbstractHistoryGenerator<T : SyncObject> {
    abstract val query: DataChangeHistoryQuery

    fun getDataChangeHistoryToSave(
        old: VaultItem<T>?,
        new: VaultItem<T>
    ): VaultItem<SyncObject.DataChangeHistory>? {
        try {
            val dataType = SyncObjectType[new] ?: return null
            val itemUid = new.uid
            var objectHistory = createOrGetDataChangeHistory(dataType, itemUid)

            if (new.isDeleted()) {
                objectHistory = objectHistory.copyWithAttrs { syncState = SyncState.MODIFIED }
            }
            val newChangeSet = newChangeSet(old, new).copy { removed = new.isDeleted() }
            if (newChangeSet.changedProperties?.isNotEmpty() == true || new.isDeleted()) {
                return objectHistory
                    .copy(syncObject = objectHistory.syncObject.copy {
                        objectTitle = newChangeSet.title
                        changeSets = addChangeSet(objectHistory.syncObject.changeSets, newChangeSet)
                    })
                    .copyWithAttrs { syncState = SyncState.MODIFIED }
            }
        } catch (e: Exception) {
            Log.v(e)
        }
        return null
    }

    private fun addChangeSet(
        list: List<SyncObject.DataChangeHistory.ChangeSet>?,
        element: SyncObject.DataChangeHistory.ChangeSet
    ): List<SyncObject.DataChangeHistory.ChangeSet> {
        if (list == null) return listOf(element)
        return list + element
    }

    

    private fun createOrGetDataChangeHistory(objectType: SyncObjectType, objectUid: String):
            VaultItem<SyncObject.DataChangeHistory> {
        val filter = DataChangeHistoryFilter(
            objectUid = objectUid,
            objectType = objectType
        )
        val data = query.query(filter)

        return data ?: createDataChangeHistory(objectType, objectUid)
    }

    private fun createDataChangeHistory(objectType: SyncObjectType, objectUid: String):
            VaultItem<SyncObject.DataChangeHistory> {
        return VaultItem(syncObject = SyncObject.DataChangeHistory {
            this.objectType = objectType.transactionType
            this.objectId = objectUid
        })
    }

    

    abstract fun newChangeSet(oldItem: VaultItem<T>?, newItem: VaultItem<T>): SyncObject.DataChangeHistory.ChangeSet
}
