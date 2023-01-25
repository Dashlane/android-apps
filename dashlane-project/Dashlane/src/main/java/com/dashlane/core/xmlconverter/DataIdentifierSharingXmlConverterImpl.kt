package com.dashlane.core.xmlconverter

import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.sync.xml.MergeListStrategy
import com.dashlane.sync.xml.mergeInto
import com.dashlane.vault.model.toVaultItem
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import com.dashlane.xml.serializer.XmlSerialization
import javax.inject.Inject

class DataIdentifierSharingXmlConverterImpl @Inject constructor() : DataIdentifierSharingXmlConverter {

    private val xmlMarshaller: XmlSerialization = XmlSerialization

    override fun fromXml(identifier: String, xml: String?): DataIdentifierExtraDataWrapper<out SyncObject>? {
        xml ?: return null
        val transactionXml = runCatching { xmlMarshaller.deserializeTransaction(xml) }.getOrElse { return null }
        val transactionRef = SyncObjectType.forXmlNameOrNull(transactionXml.type) ?: return null
        val vaultItem = transactionXml.toObject(transactionRef).toVaultItem(identifier)
        return DataIdentifierExtraDataWrapper(vaultItem, xml)
    }

    override fun toXml(extraDataWrapper: DataIdentifierExtraDataWrapper<out SyncObject>?): String? {
        extraDataWrapper ?: return null
        val vaultItem = extraDataWrapper.vaultItem
        val itemNode = vaultItem.syncObject.toTransaction().node
        val backupNode = extraDataWrapper.extraData?.let {
            runCatching { xmlMarshaller.deserializeTransaction(it).node }.getOrNull()
        }
        val mergedNode = if (backupNode != null) {
            itemNode.mergeInto(backupNode, MergeListStrategy.KEEP_RICHEST)
        } else {
            itemNode
        }
        return xmlMarshaller.serializeTransaction(XmlTransaction(mergedNode))
    }
}