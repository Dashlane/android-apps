package com.dashlane.xml.serializer

interface XmlSerialization : XmlSerializer, XmlDeserializer {
    companion object : XmlSerialization, XmlSerializer by XmlSerializer, XmlDeserializer by XmlDeserializer
}