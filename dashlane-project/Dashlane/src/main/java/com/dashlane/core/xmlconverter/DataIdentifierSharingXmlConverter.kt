package com.dashlane.core.xmlconverter

import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.xml.domain.SyncObject

interface DataIdentifierSharingXmlConverter {
    fun fromXml(identifier: String, xml: String?): DataIdentifierExtraDataWrapper<out SyncObject>?
    fun toXml(extraDataWrapper: DataIdentifierExtraDataWrapper<out SyncObject>?): String?
}