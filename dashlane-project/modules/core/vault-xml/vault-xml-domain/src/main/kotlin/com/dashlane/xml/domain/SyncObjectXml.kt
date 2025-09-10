package com.dashlane.xml.domain

import com.dashlane.xml.XmlData
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.XmlTypeException

fun XmlTransaction.toObject(): SyncObject {
    val syncObjectType = SyncObjectType.forXmlNameOrNull(type)
        ?: throw XmlTypeException("Unrecognized $type node")
    return syncObjectType.create(node.data)
}

fun XmlTransaction.toObject(syncObjectType: SyncObjectType): SyncObject {
    if (type != syncObjectType.xmlObjectName) {
        throw XmlTypeException("Expected ${syncObjectType.xmlObjectName} node but was $type node")
    }
    return syncObjectType.create(node.data)
}

fun SyncObject.toTransaction(): XmlTransaction =
    XmlTransaction(XmlData.ObjectNode(objectType.xmlObjectName, data))