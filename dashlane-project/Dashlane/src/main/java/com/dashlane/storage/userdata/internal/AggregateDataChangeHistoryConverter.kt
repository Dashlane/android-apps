package com.dashlane.storage.userdata.internal

import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.toCommonDataIdentifierAttrs
import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.vault.util.desktopId
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType



fun AggregateDataChangeHistory.toDataChangeHistory(): VaultItem<SyncObject.DataChangeHistory> {
    val changeSetForSyncList = changeSetList.map { changeSet ->

        
        val filtered = changeSetChangeList
            .filter { it.changeSetUID == changeSet.uid && it.changedProperty.isNotSemanticallyNull() }

        
        
        changeSet.toChangeSet(filtered.toChangedProperties(), filtered.toPropertyValueMap())
    }

    
    return dataChangeHistory.toDataChangeHistory(changeSetForSyncList)
}



fun VaultItem<SyncObject.DataChangeHistory>.toAggregateDataChangeHistory(): AggregateDataChangeHistory {

    val dataChangeHistoryForDb = toDataChangeHistoryForDb()

    val changeSetList: MutableList<ChangeSetForDb> = mutableListOf()
    val changeSetChangeList: MutableList<ChangeSetChangeForDb> = mutableListOf()

    syncObject.changeSets?.forEach { changeSetForSync ->
        changeSetChangeList.addAll(changeSetForSync.toChangeSetChangeList())
        changeSetList.add(changeSetForSync.toChangeSetForDb(dataChangeHistoryForDb))
    }

    return AggregateDataChangeHistory(dataChangeHistoryForDb, changeSetList, changeSetChangeList)
}



fun VaultItem<SyncObject.DataChangeHistory>.toDataChangeHistoryForDb() =
    DataChangeHistoryForDb(
        dataIdentifier = toCommonDataIdentifierAttrs(),
        objectUID = syncObject.objectId ?: "",
        objectTitle = syncObject.objectTitle,
        objectTypeId = dataTypeForTransactionType(syncObject.objectType)
    )

private fun dataTypeForTransactionType(transactionType: String?): Int =
    transactionType?.uppercase()
        ?.let { SyncObjectType.forTransactionTypeOrNull(it) }
        ?.desktopId ?: 0



fun DataChangeHistoryForDb.toDataChangeHistory(changeSets: List<SyncObject.DataChangeHistory.ChangeSet>):
        VaultItem<SyncObject.DataChangeHistory> {
    val from = this
    val syncObject = SyncObject.DataChangeHistory {
        id = from.dataIdentifier.uid
        anonId = from.dataIdentifier.anonymousUID
        objectId = from.objectUID
        objectType = transactionTypeForDesktopId(from.objectTypeId)
        objectTitle = from.objectTitle
        this.changeSets = changeSets
    }
    return VaultItem(from.dataIdentifier, syncObject)
}

private fun transactionTypeForDesktopId(desktopId: Int) =
    SyncObjectTypeUtils.valueFromDesktopIdIfExist(desktopId)
        ?.transactionType



fun SyncObject.DataChangeHistory.ChangeSet.toChangeSetForDb(dataChangeHistory: DataChangeHistoryForDb): ChangeSetForDb {
    val old = this
    return ChangeSetForDb(
        uid = old.id ?: "",
        dataChangeHistoryUID = dataChangeHistory.dataIdentifier.uid,
        modificationTimestampSeconds = old.modificationDate,
        user = old.user,
        platform = old.platform,
        deviceName = old.deviceName,
        isRemoved = old.removed ?: false
    )
}



private fun ChangeSetForDb.toChangeSet(
    changedProperties: List<String>,
    propertyValueMap: Map<String, String?>
): SyncObject.DataChangeHistory.ChangeSet {
    val old = this
    return SyncObject.DataChangeHistory.ChangeSet {
        id = old.uid
        deviceName = old.deviceName
        platform = old.platform
        removed = old.isRemoved
        user = old.user
        modificationDate = old.modificationTimestampSeconds
        this.changedProperties = changedProperties
        currentData = propertyValueMap.mapValues { (_, value) -> value ?: "" }
    }
}



fun SyncObject.DataChangeHistory.ChangeSet.toChangeSetChangeList(): List<ChangeSetChangeForDb> {
    val propertyIdMap = currentData?.mapValues { (_, value) -> value.ifEmpty { null } } ?: emptyMap()
    return propertyIdMap.map {
        ChangeSetChangeForDb(
            uid = generateUniqueIdentifier(),
            changeSetUID = id ?: "",
            changedProperty = it.key,
            currentValue = it.value,
            isSavedFromJava = true
        )
    }
}


private fun List<ChangeSetChangeForDb>.toPropertyValueMap(): Map<String, String?> =
    this.associate { it.changedProperty to it.currentValue }



private fun List<ChangeSetChangeForDb>.toChangedProperties(): List<String> = this.map { it.changedProperty }