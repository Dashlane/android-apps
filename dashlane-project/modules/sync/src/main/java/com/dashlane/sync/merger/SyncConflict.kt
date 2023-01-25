package com.dashlane.sync.merger

import com.dashlane.xml.XmlData
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import javax.inject.Inject
import kotlin.jvm.Throws



class SyncConflict @Inject constructor() {

    

    @Throws(DuplicateException::class)
    fun mergeConflicting(
        syncObjectType: SyncObjectType,
        backupSyncObject: SyncObject,
        outgoingSyncObject: SyncObject,
        incomingSyncObject: SyncObject
    ): SyncObject {
        val backupXmlObject = backupSyncObject.toTransaction().data
        val outgoingXmlObject = outgoingSyncObject.toTransaction().data
        val incomingXmlObject = incomingSyncObject.toTransaction().data

        
        val data = (incomingXmlObject + outgoingXmlObject).toMutableMap()

        
        val conflictingFields = syncObjectType.conflictingFields
        conflictingFields.forEach { key ->
            val remoteValue = incomingXmlObject[key]
            val backupValue = backupXmlObject[key]
            val modifiedRemotely = remoteValue != backupValue
            if (modifiedRemotely) {
                val localValue = outgoingXmlObject[key]
                val modifiedLocally = localValue != backupValue
                if (modifiedLocally) throw DuplicateException("Conflict on $key")

                if (remoteValue == null) {
                    data.remove(key)
                } else {
                    data[key] = remoteValue
                }
            }
        }

        val node = XmlData.ObjectNode(syncObjectType.xmlObjectName, data)
        return XmlTransaction(node).toObject()
    }

    

    internal class DuplicateException(
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message, cause)
}
