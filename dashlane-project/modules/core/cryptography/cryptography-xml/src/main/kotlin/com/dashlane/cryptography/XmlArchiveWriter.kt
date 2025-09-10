package com.dashlane.cryptography

import java.io.StringWriter
import java.io.Writer

fun interface XmlArchiveWriter {
    fun write(writer: Writer, archive: XmlArchive)
}

fun XmlArchiveWriter.write(archive: XmlArchive): String =
    StringWriter().also { write(it, archive) }.buffer.toString()

fun XmlArchiveWriter(): XmlArchiveWriter =
    XmlArchiveWriterImpl()

private class XmlArchiveWriterImpl : XmlArchiveWriter {
    override fun write(writer: Writer, archive: XmlArchive) {
        writer.appendLine(XmlArchiveTag.HEADER)
        writer.appendLine(XmlArchiveTag.ID_BEGIN)
        archive.ids?.also { ids ->
            writer.appendLine(ids.joinToString(XmlArchiveTag.ID_DELIMITER))
        }
        writer.appendLine(XmlArchiveTag.ID_END)
        writer.appendLine(XmlArchiveTag.DATA_BEGIN)
        archive.data?.also { data ->
            writer.appendLine(data.value)
        }
        writer.appendLine(XmlArchiveTag.DATA_END)
        writer.appendLine(XmlArchiveTag.FILE_BEGIN)
        writer.appendLine(XmlArchiveTag.FILE_END)
    }
}