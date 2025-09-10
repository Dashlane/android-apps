package com.dashlane.xml.serializer

import com.dashlane.xml.XmlBackup
import com.dashlane.xml.XmlTransaction
import java.io.Reader

interface XmlDeserializer {
    fun deserializeTransaction(xml: String): XmlTransaction =
        deserializeTransaction(xml.reader())

    fun deserializeBackup(xml: String): XmlBackup =
        deserializeBackup(xml.reader())

    fun deserializeTransaction(reader: Reader): XmlTransaction

    fun deserializeBackup(reader: Reader): XmlBackup

    companion object : XmlDeserializer by XmlDeserializerImpl()
}