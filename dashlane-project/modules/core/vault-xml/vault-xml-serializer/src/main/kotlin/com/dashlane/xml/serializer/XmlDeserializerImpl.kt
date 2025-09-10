package com.dashlane.xml.serializer

import com.dashlane.xml.XmlBackup
import com.dashlane.xml.XmlData
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.XmlTypeException
import com.dashlane.xml.asListNodeOrNull
import com.dashlane.xml.asObjectNodeOrNull
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.serializer.utils.associateByNotNullKeys
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.Reader
import java.util.Deque
import java.util.LinkedList
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

internal class XmlDeserializerImpl : XmlDeserializer {
    private val saxParser: SAXParser
        get() = SAXParserFactory.newInstance().newSAXParser()

    override fun deserializeTransaction(reader: Reader): XmlTransaction {
        val xmlData = deserializeXml(reader)
        val objectNode = xmlData.asObjectNodeOrNull()
            ?: throw XmlTypeException("Expected object node, but was ${xmlData::class.java.simpleName}")
        return XmlTransaction(objectNode)
    }

    override fun deserializeBackup(reader: Reader): XmlBackup {
        val xmlData = deserializeXml(reader)
        val listNode = xmlData.asListNodeOrNull()
            ?: throw XmlTypeException("Expected list node, but was ${xmlData::class.java.simpleName}")
        return XmlBackup(listNode.filterIsInstance<XmlData.ObjectNode>())
    }

    private fun deserializeXml(reader: Reader): XmlData {
        val xmlReader = saxParser.xmlReader
        val handler = KwXmlHandler()
        xmlReader.contentHandler = handler
        try {
            val source = InputSource(XmlFilterReader(reader))
            xmlReader.parse(source)
        } catch (e: SAXParseException) {
            
            throw e.toXmlException()
        }
        return handler.result
    }

    private class KwXmlHandler : DefaultHandler() {
        val result: XmlData
            get() {
                val head = nodeStack.single() 
                val root = head.children.single() 

                return if (root.name == XmlName.Node.ROOT) {
                    val objectData = root.children.singleOrNull() 
                        ?: root 
                    objectData.toXmlData()
                } else {
                    
                    root.toXmlData()
                }
            }

        private val nodeStack: Deque<XmlDataBuilder> = LinkedList<XmlDataBuilder>().also {
            
            it += XmlDataBuilder("")
        }

        override fun startElement(uri: String?, localName: String, qName: String, attributes: Attributes) {
            nodeStack += XmlDataBuilder(qName, attributes.getValue(XmlName.Attribute.KEY))
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            nodeStack.last.value.append(ch, start, length)
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            val last = nodeStack.removeLast()
            nodeStack.last.children += last
        }
    }

    private class XmlDataBuilder(
        val name: String,
        val key: String? = null,
    ) {
        val children = mutableListOf<XmlDataBuilder>()
        val value = StringBuilder()

        fun toXmlData(parentSyncObjectType: SyncObjectType? = null): XmlData {
            if (XmlName.Node.ITEM == name) {
                val valueString = value.toString().orEmptyIfSemanticallyNull()
                return if (key?.let { parentSyncObjectType?.sensitiveFields?.contains(it) } == true) {
                    
                    XmlData.ItemNodeObfuscated(valueString)
                } else {
                    XmlData.ItemNode(valueString)
                }
            }
            val syncObjectType = SyncObjectType.forXmlNameOrNull(name)
            val valueTransform: (XmlDataBuilder) -> XmlData = { it.toXmlData(syncObjectType) }
            return when (name) {
                XmlName.Node.LIST ->
                    XmlData.ListNode(children.map(valueTransform))
                XmlName.Node.COLLECTION ->
                    XmlData.CollectionNode(children.associateByNotNullKeys(XmlDataBuilder::key, valueTransform))
                else ->
                    XmlData.ObjectNode(
                        name,
                        children.associateByNotNullKeys(XmlDataBuilder::key, valueTransform),
                    )
            }
        }
    }
}

private fun String.orEmptyIfSemanticallyNull(): String =
    if (trim() == "null") "" else this