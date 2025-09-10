package com.dashlane.xml

data class XmlBackup(val nodes: List<XmlData.ObjectNode>) {
    fun toTransactionList(): List<XmlTransaction> =
        nodes.map { XmlTransaction(it) }
}