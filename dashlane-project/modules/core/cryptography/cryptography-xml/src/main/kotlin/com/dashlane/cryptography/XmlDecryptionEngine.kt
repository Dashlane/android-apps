package com.dashlane.cryptography

import com.dashlane.xml.XmlBackup
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.serializer.XmlDeserializer
import com.dashlane.xml.serializer.XmlException
import com.dashlane.xml.serializer.XmlSerialization
import okio.buffer
import java.io.IOException
import java.io.Reader

interface XmlDecryptionEngine : AutoCloseable {
    @Throws(CryptographyException::class, XmlException::class)
    fun decryptToXmlTransaction(
        decryptionSource: DecryptionSource,
        compressed: Boolean = true,
    ): XmlTransaction

    @Throws(CryptographyException::class, XmlException::class)
    fun decryptToXmlBackup(
        decryptionSource: DecryptionSource,
        compressed: Boolean = true,
    ): XmlBackup
}

fun XmlDecryptionEngine.decryptBase64ToXmlTransactionOrNull(string: EncryptedBase64String, compressed: Boolean = true): XmlTransaction? =
    try {
        decryptBase64ToXmlTransaction(string, compressed)
    } catch (e: CryptographyException) {
        null
    } catch (e: XmlException) {
        null
    }

@Throws(CryptographyException::class, XmlException::class)
fun XmlDecryptionEngine.decryptBase64ToXmlTransaction(string: EncryptedBase64String, compressed: Boolean = true): XmlTransaction =
    decryptToXmlTransaction(string.asBase64DecryptionSource(), compressed)

@Throws(CryptographyException::class, XmlException::class, IOException::class)
fun XmlDecryptionEngine.decryptFileToXmlTransaction(inputFile: EncryptedFile, compressed: Boolean = true): XmlTransaction =
    decryptToXmlTransaction(inputFile.asDecryptionSource(), compressed)

fun XmlDecryptionEngine.decryptBase64ToXmlBackupOrNull(string: EncryptedBase64String, compressed: Boolean = true): XmlBackup? =
    try {
        decryptBase64ToXmlBackup(string, compressed)
    } catch (e: CryptographyException) {
        null
    } catch (e: XmlException) {
        null
    }

@Throws(CryptographyException::class, XmlException::class)
fun XmlDecryptionEngine.decryptBase64ToXmlBackup(string: EncryptedBase64String, compressed: Boolean = true): XmlBackup =
    decryptToXmlBackup(string.asBase64DecryptionSource(), compressed)

@Throws(CryptographyException::class, XmlException::class, IOException::class)
fun XmlDecryptionEngine.decryptFileToXmlBackup(inputFile: EncryptedFile, compressed: Boolean = true): XmlBackup =
    decryptToXmlBackup(inputFile.asDecryptionSource(), compressed)

fun DecryptionEngine.forXml(deserializer: XmlDeserializer = XmlSerialization): XmlDecryptionEngine =
    XmlDecryptionEngineImpl(this, deserializer)

private class XmlDecryptionEngineImpl(
    private val decryptionEngine: DecryptionEngine,
    private val xmlDeserializer: XmlDeserializer,
) : XmlDecryptionEngine, AutoCloseable by decryptionEngine {
    override fun decryptToXmlTransaction(decryptionSource: DecryptionSource, compressed: Boolean): XmlTransaction =
        decryptionEngine.decryptToUtf8(decryptionSource, compressed) { reader ->
            xmlDeserializer.deserializeTransaction(reader)
        }

    override fun decryptToXmlBackup(decryptionSource: DecryptionSource, compressed: Boolean): XmlBackup =
        decryptionEngine.decryptToUtf8(decryptionSource, compressed) { reader ->
            xmlDeserializer.deserializeBackup(reader)
        }
}

private inline fun <T> DecryptionEngine.decryptToUtf8(
    decryptionSource: DecryptionSource,
    compressed: Boolean,
    crossinline block: (Reader) -> T,
): T = decrypt(decryptionSource, compressed) { source ->
    source.buffer().inputStream().reader().use(block)
}