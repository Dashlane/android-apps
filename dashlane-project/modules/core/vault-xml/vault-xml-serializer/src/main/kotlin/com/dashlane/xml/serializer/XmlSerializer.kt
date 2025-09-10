package com.dashlane.xml.serializer

import com.dashlane.xml.XmlBackup
import com.dashlane.xml.XmlTransaction
import java.io.StringWriter
import java.io.Writer

interface XmlSerializer {
    fun serializeTransaction(transaction: XmlTransaction): String =
        StringWriter().also { serializeTransaction(transaction, it) }.buffer.toString()

    fun serializeBackup(backup: XmlBackup): String =
        StringWriter().also { serializeBackup(backup, it) }.buffer.toString()

    fun serializeTransaction(transactionXmlTransaction: XmlTransaction, writer: Writer)

    fun serializeBackup(backupXml: XmlBackup, writer: Writer)

    companion object : XmlSerializer by XmlSerializerImpl()
}