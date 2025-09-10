package com.dashlane.xml

data class XmlTransaction(val node: XmlData.ObjectNode) {
    val type
        get() = node.type
    val data
        get() = node.data
}