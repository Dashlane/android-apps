package com.dashlane.cryptography

import java.io.Reader

fun interface XmlArchiveReader {
    fun read(reader: Reader): XmlArchive
}

fun XmlArchiveReader.read(s: String) =
    read(s.reader())

fun XmlArchiveReader(): XmlArchiveReader = XmlArchiveReaderImpl()

@Suppress(
    
    "kotlin:S3776",
)
private class XmlArchiveReaderImpl : XmlArchiveReader {
    override fun read(reader: Reader): XmlArchive =
        reader.useLines { lines ->
            var state = State.HEADER
            var ids: List<String>? = null
            var data: String? = null

            lines.forEach { line ->
                when (state) {
                    State.HEADER -> {
                        checkXmlArchive(line == XmlArchiveTag.HEADER) { "Missing archive header tag." }
                        state = State.CONTENT
                    }
                    State.CONTENT -> {
                        
                        when (line) {
                            XmlArchiveTag.ID_BEGIN ->
                                state = State.ID_BEGIN
                            XmlArchiveTag.DATA_BEGIN ->
                                state = State.DATA_BEGIN
                        }
                    }
                    State.ID_BEGIN -> {
                        if (line == XmlArchiveTag.ID_END) {
                            
                            state = State.CONTENT
                        } else {
                            
                            if (ids == null) {
                                ids =
                                    if (line.isEmpty()) emptyList() else line.split(XmlArchiveTag.ID_DELIMITER)
                                state = State.ID_END
                            }
                        }
                    }
                    State.ID_END -> {
                        checkXmlArchive(line == XmlArchiveTag.ID_END) { "Missing closing tag: ${XmlArchiveTag.ID_END}" }
                        state = State.CONTENT
                    }
                    State.DATA_BEGIN -> {
                        if (line == XmlArchiveTag.DATA_END) {
                            
                            state = State.CONTENT
                        } else {
                            
                            if (data == null) {
                                data = line
                                state = State.DATA_END
                            }
                        }
                    }
                    State.DATA_END -> {
                        checkXmlArchive(line == XmlArchiveTag.DATA_END) { "Missing closing tag: ${XmlArchiveTag.DATA_END}" }
                        state = State.CONTENT
                    }
                }
                if (ids != null && data != null) return@forEach 
            }

            
            
            checkXmlArchive(state == State.CONTENT) { "Invalid XML archive: illegal terminal state $state." }

            XmlArchive(ids, data?.asEncryptedBase64())
        }

    private fun checkXmlArchive(value: Boolean, lazyMessage: () -> Any) {
        if (!value) {
            val message = lazyMessage()
            throw XmlArchiveException(message.toString())
        }
    }

    private enum class State {
        HEADER,

        CONTENT,

        ID_BEGIN,

        ID_END,

        DATA_BEGIN,

        DATA_END,
    }
}