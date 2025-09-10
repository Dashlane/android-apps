package com.dashlane.xml.serializer

import com.dashlane.xml.XmlBackup
import com.dashlane.xml.XmlData
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.toListNode
import java.io.Writer

internal class XmlSerializerImpl : XmlSerializer {
    override fun serializeTransaction(transactionXmlTransaction: XmlTransaction, writer: Writer) =
        writer.serializeXml(transactionXmlTransaction.node)

    override fun serializeBackup(backupXml: XmlBackup, writer: Writer) =
        writer.serializeXml(backupXml.nodes.toListNode())

    private fun Writer.serializeXml(node: XmlData) {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        write("<root>\n")
        writeXmlData(1, node)
        write("</root>\n")
    }
}

private fun Writer.writeXmlData(indent: Int, node: XmlData, key: String? = null) {
    when (node) {
        is XmlData.ItemNode -> writeXmlDataElement(indent, node, key)
        is XmlData.ItemNodeObfuscated -> writeXmlDataElement(indent, node, key)
        is XmlData.ListNode -> writeXmlDataList(indent, node, key)
        is XmlData.CollectionNode -> writeXmlDataCollection(indent, node, key)
        is XmlData.ObjectNode -> writeXmlDataObject(indent, node, key)
    }
}

private fun Writer.writeXmlDataElement(indent: Int, node: XmlData.ItemNodeObfuscated, key: String?) =
    writeXmlDataElement(indent, node.value.toString(), key)

private fun Writer.writeXmlDataElement(indent: Int, node: XmlData.ItemNode, key: String?) =
    writeXmlDataElement(indent, node.value, key)

private fun Writer.writeXmlDataElement(indent: Int, valueRaw: String, key: String?) =
    writeElement(indent, XmlName.Node.ITEM, key) {
        val value = valueRaw.filterValidXmlCharacters()
        write("<![CDATA[")
        if (value.contains("]]>")) {
            write(value.replace("]]>", "]]]]><![CDATA[>"))
        } else {
            write(value)
        }
        write("]]>")
    }

private fun Writer.writeXmlDataList(indent: Int, node: XmlData.ListNode, key: String?) =
    writeElement(indent, XmlName.Node.LIST, key) {
        val value = node.value
        if (value.isNotEmpty()) {
            write("\n")
            value.forEach {
                writeXmlData(indent + 1, it)
            }
            indent(indent)
        }
    }

private fun Writer.writeXmlDataCollection(indent: Int, node: XmlData.CollectionNode, key: String?) =
    writeElement(indent, XmlName.Node.COLLECTION, key) {
        val value = node.value
        if (value.isNotEmpty()) {
            write("\n")
            value.forEach { (key, value) ->
                writeXmlData(indent + 1, value, key)
            }
            indent(indent)
        }
    }

private fun Writer.writeXmlDataObject(indent: Int, node: XmlData.ObjectNode, key: String?) =
    writeElement(indent, node.type, key) {
        val data = node.data
        if (data.isNotEmpty()) {
            write("\n")
            data.forEach { (key, value) ->
                writeXmlData(indent + 1, value, key)
            }
            indent(indent)
        }
    }

@Suppress("ThrowsCount")
private inline fun Writer.writeElement(
    indent: Int,
    localName: String,
    keyAttributeValue: String? = null,
    block: Writer.() -> Unit,
) {
    if (!localName.isValidNodeName) {
        throw XmlException("Invalid xml attribute characters")
    }
    if (localName.isEmpty()) {
        throw XmlException("Invalid empty node name")
    }
    indent(indent)
    if (keyAttributeValue != null) {
        if (!keyAttributeValue.isValidAttributeValue) {
            throw XmlException("Invalid xml attribute characters")
        }
        write("<$localName ${XmlName.Attribute.KEY}=\"$keyAttributeValue\">")
    } else {
        write("<$localName>")
    }
    block()
    write("</$localName>\n")
}

private const val INDENT_SIZE = 4

private fun Writer.indent(indent: Int) {
    write(CharArray(indent * INDENT_SIZE) { ' ' })
}

fun String.filterValidXmlCharacters(): String =
    replace(invalidXmlCharacters, "")

private val invalidXmlCharacters =
    Regex("[^\u0009\r\n\u0020-\uD7FF\uE000-\uFFFD\uD800\udc00-\udbff\udfff]")

private val String.isValidNodeName
    get() = validNodeNameRegex.matches(this)

@Suppress("ktlint:standard:max-line-length")
private val validNodeNameRegex =
    Regex(
        "^[:A-Z_a-z\u00C0-\u00D6\u00D8-\u00F6\u00F8\u02FF\u0370\u037D\u037F\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900\uFDCF\uFDF0-\uFFFD\uD800\uDC00-\uDB7F\uDFFF][:A-Z_a-z\u00C0-\u00D6\u00D8-\u00F6\u00F8\u02FF\u0370\u037D\u037F\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900\uFDCF\uFDF0-\uFFFD\uD800\uDC00-\uDB7F\uDFFF\\-.0-9\u00B7\u0300-\u036F\u203F\u2040]*$",
    )

private val String.isValidAttributeValue
    get() = validAttributeValueRegex.matches(this)

private val validAttributeValueRegex =
    Regex("^[\u0009\u000A\u000D\u0020-\uD7FF\uE000-\uFFFD\uD800\udc00-\udbff\udfff&&[^<&\"]]*$")