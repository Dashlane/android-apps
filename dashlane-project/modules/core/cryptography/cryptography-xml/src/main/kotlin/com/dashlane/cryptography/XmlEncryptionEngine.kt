package com.dashlane.cryptography

import com.dashlane.xml.XmlBackup
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.serializer.XmlSerialization
import com.dashlane.xml.serializer.XmlSerializer
import okio.buffer
import java.io.IOException
import java.io.Writer

interface XmlEncryptionEngine : AutoCloseable {
    fun encryptXmlTransaction(
        xml: XmlTransaction,
        encryptionSink: EncryptionSink,
        compressed: Boolean = true,
    )

    fun encryptXmlBackup(
        xml: XmlBackup,
        encryptionSink: EncryptionSink,
        compressed: Boolean = true,
    )
}

fun XmlEncryptionEngine.encryptXmlTransactionToBase64String(xml: XmlTransaction, compressed: Boolean = true): EncryptedBase64String =
    runEncryptionToBase64 { encryptXmlTransaction(xml, it, compressed) }

@Throws(IOException::class)
fun XmlEncryptionEngine.encryptXmlTransactionToFile(
    xml: XmlTransaction,
    outputFile: EncryptedFile,
    compressed: Boolean = true,
    fsync: Boolean = false,
) {
    outputFile.asEncryptionSink(fsync).use { encryptionSink ->
        encryptXmlTransaction(xml, encryptionSink, compressed)
    }
}

fun XmlEncryptionEngine.encryptXmlBackupToBase64String(xml: XmlBackup, compressed: Boolean = true): EncryptedBase64String =
    runEncryptionToBase64 { encryptXmlBackup(xml, it, compressed) }

@Throws(IOException::class)
fun XmlEncryptionEngine.encryptXmlBackupToFile(
    xml: XmlBackup,
    outputFile: EncryptedFile,
    compressed: Boolean = true,
    fsync: Boolean = false,
) {
    outputFile.asEncryptionSink(fsync).use { encryptionSink ->
        encryptXmlBackup(xml, encryptionSink, compressed)
    }
}

fun EncryptionEngine.forXml(serializer: XmlSerializer = XmlSerialization): XmlEncryptionEngine =
    XmlEncryptionEngineImpl(this, serializer)

private class XmlEncryptionEngineImpl(
    private val encryptionEngine: EncryptionEngine,
    private val xmlSerializer: XmlSerializer,
) : XmlEncryptionEngine, AutoCloseable by encryptionEngine {
    override fun encryptXmlTransaction(xml: XmlTransaction, encryptionSink: EncryptionSink, compressed: Boolean) =
        encryptionEngine.encryptUtf8(encryptionSink, compressed) { writer ->
            xmlSerializer.serializeTransaction(xml, writer)
        }

    override fun encryptXmlBackup(xml: XmlBackup, encryptionSink: EncryptionSink, compressed: Boolean) =
        encryptionEngine.encryptUtf8(encryptionSink, compressed) { writer ->
            xmlSerializer.serializeBackup(xml, writer)
        }
}

private inline fun EncryptionEngine.encryptUtf8(
    encryptionSink: EncryptionSink,
    compressed: Boolean,
    crossinline block: (Writer) -> Unit,
) = encrypt(encryptionSink, compressed).use { sink ->
    sink.buffer().outputStream().writer().use(block)
}