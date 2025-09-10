package com.dashlane.hermes.storage

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyAppKeyStore
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyKeyGenerator
import com.dashlane.cryptography.EncryptedFile
import com.dashlane.cryptography.asEncryptedFile
import com.dashlane.cryptography.decryptFileToUtf8String
import com.dashlane.cryptography.encryptUtf8ToFile
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.ProviderException
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID
import kotlin.random.asKotlinRandom

class SecureFileLogStorage(
    private val logDirectory: File,
    private val cryptographyAppKeyStore: CryptographyAppKeyStore,
) : LogStorage {
    private val cryptography = Cryptography()
    private val keyGenerator = CryptographyKeyGenerator(SecureRandom().asKotlinRandom())
    private val cryptographyKey: CryptographyKey.Raw32 by lazy {
        cryptographyAppKeyStore[KEY_ALIAS] ?: createCryptographyKey(cryptographyAppKeyStore)
    }

    init {
        logDirectory.mkdirs()
        require(logDirectory.isDirectory) { "Log directory should be a directory" }
    }

    override fun storeLog(logItem: LogItem) {
        val encryptedFile = getEncryptedFile(logItem.toLogFileName(), logItem.storageCategory)

        try {
            cryptography.createFlexibleNoDerivationEncryptionEngine(cryptographyKey)
                .use { it.encryptUtf8ToFile(logItem.logContent, encryptedFile) }
        } catch (e: GeneralSecurityException) {
            
            
            
            
            return
        } catch (e: IOException) {
            
            
            
            return
        } catch (e: ProviderException) {
            
            
            
            return
        }
    }

    override fun readLogs(storageCategory: StorageCategory): List<LogItem> {
        val files =
            File(logDirectory, storageCategory.directory).listFiles()
                ?.sortedBy { it.nameWithoutExtension }
                ?: return listOf()
        val logs: ArrayList<LogItem> = arrayListOf()
        val category = storageCategory.logCategory
        files.forEach {
            val fileName = it.nameWithoutExtension.split(LogItem.FILE_NAME_SEPARATOR)
            try {
                val logItem =
                    LogItem(
                        Instant.ofEpochMilli(fileName[0].toLong()),
                        UUID.fromString(fileName[1]),
                        readEncryptedFile(it.asEncryptedFile()),
                        category,
                    )
                logs.add(logItem)
            } catch (e: GeneralSecurityException) {
                
                
                
                
                it.delete()
            } catch (e: IOException) {
                
                
                
                it.delete()
            } catch (e: CryptographyException) {
                
                
                
                it.delete()
            }
        }
        return logs
    }

    override fun deleteLogs(logItems: List<LogItem>) {
        logItems.forEach {
            val encryptedFile = getEncryptedFile(it.toLogFileName(), it.storageCategory)
            encryptedFile.value.delete()
        }
    }

    private fun LogItem.toLogFileName(): String = "${this.fileName}.json"

    private fun readEncryptedFile(encryptedFile: EncryptedFile): String {
        return cryptography.createDecryptionEngine(cryptographyKey)
            .use { it.decryptFileToUtf8String(encryptedFile) }
    }

    private fun createCryptographyKey(cryptographyAppKeyStore: CryptographyAppKeyStore): CryptographyKey.Raw32 {
        val cryptographyKey = keyGenerator.generateRaw32()
        cryptographyAppKeyStore[KEY_ALIAS] = cryptographyKey
        return cryptographyKey
    }

    private fun getEncryptedFile(
        filename: String,
        storageCategory: StorageCategory,
    ) = getFile(filename, storageCategory).asEncryptedFile()

    private fun getFile(
        filename: String,
        storageCategory: StorageCategory,
    ): File = File(File(logDirectory, storageCategory.directory).apply(File::mkdirs), filename)

    companion object {
        private const val KEY_ALIAS = "hermes_log_storage"
    }
}