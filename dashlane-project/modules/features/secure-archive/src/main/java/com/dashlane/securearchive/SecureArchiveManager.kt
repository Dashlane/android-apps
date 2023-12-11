package com.dashlane.securearchive

import android.content.Context
import android.net.Uri
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyFixedSalt
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.XmlArchive
import com.dashlane.cryptography.XmlArchiveReader
import com.dashlane.cryptography.XmlArchiveWriter
import com.dashlane.cryptography.decryptSecureArchive
import com.dashlane.cryptography.encryptSecureArchive
import com.dashlane.cryptography.forXml
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.AppSync
import com.dashlane.util.FileUtils
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.toVaultItem
import com.dashlane.xml.XmlBackup
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class SecureArchiveManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val mainDataAccessor: MainDataAccessor,
    private val cryptography: Cryptography,
    private val saltGenerator: SaltGenerator,
    private val appSync: AppSync
) {
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    private val fileName: String
        get() {
            val calendar = ZonedDateTime.now(ZoneOffset.UTC)
            return "Vault-${dateFormat.format(calendar)}.dash"
        }

    private val archiveReader = XmlArchiveReader()
    private val archiveWriter = XmlArchiveWriter()

    suspend fun hasData(uri: Uri): Boolean = withContext(Dispatchers.Default) {
        !readSecureArchive(uri)?.data?.value.isNullOrEmpty()
    }

    suspend fun import(uri: Uri, password: String): List<VaultItem<*>> = withContext(Dispatchers.Default) {
        val data = readSecureArchive(uri)!!

        yield()

        val fullBackup = CryptographyKey.ofPassword(password)
            .use { cryptographyKey -> cryptography.createDecryptionEngine(cryptographyKey).forXml() }
            .use { decryptionEngine -> decryptionEngine.decryptSecureArchive(data)!! }

        yield()

        val savedItems = fullBackup.generateNewVaultList()
            .filter { mainDataAccessor.getDataSaver().save(it) }

        appSync.sync()

        savedItems
    }

    suspend fun export(password: String): List<VaultItem<*>> = withContext(Dispatchers.Default) {
        if (password.isEmpty()) throw InvalidPassword()

        val filter = vaultFilter {
            specificDataType(EXPORTABLE_DATA_TYPES)
            onlyShareable()
        }
        val data = mainDataAccessor.getVaultDataQuery().queryAll(filter)

        val (fullBackupXmlNode, idsList) = data.toSyncBackupWithIdsList()

        yield()

        val keyDerivation = CryptographyMarker.Flexible.KeyDerivation.Default.argon2d
        val fixedSalt = CryptographyFixedSalt(saltGenerator.generateRandomSalt(keyDerivation.saltLength))
        val archive = CryptographyKey.ofPassword(password)
            .use { cryptographyKey ->
                cryptography.createFlexibleArgon2dEncryptionEngine(cryptographyKey, fixedSalt, keyDerivation).forXml()
            }
            .use { encryptionEngine ->
                encryptionEngine.encryptSecureArchive(idsList, fullBackupXmlNode)
            }

        yield()

        try {
            FileUtils.writeFileToPublicFolder(context, fileName, "application/octet-stream") { outputStream ->
                outputStream.writer().use { writer ->
                    archiveWriter.write(writer, archive)
                }
            }
        } catch (e: IllegalArgumentException) {
            
            val dir = File(context.cacheDir, "file_provider")
            dir.mkdirs()

            val newFile = File(dir, fileName)
            withContext(Dispatchers.IO) {
                newFile.createNewFile()
                newFile.writer().use { writer ->
                    archiveWriter.write(writer, archive)
                }
            }
            throw FallbackToSharingArchive(data, newFile, e)
        }

        data
    }

    private fun readSecureArchive(uri: Uri): XmlArchive? = tryOrNull {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.reader().use { archiveReader.read(it) }
        }
    }

    private fun XmlBackup.generateNewVaultList(): List<VaultItem<*>> {
        val oldToNewIds = mutableMapOf<String, String>()
        val transactionList = toTransactionList()
        return transactionList.mapNotNull { transactionXml ->
            SyncObjectType.forXmlNameOrNull(transactionXml.type)?.takeUnless {
                    
                    
                    it == SyncObjectType.SETTINGS || it == SyncObjectType.COLLECTION
                }?.let { type ->
                    val syncObject = transactionXml.toObject(type)
                    val newId = generateUniqueIdentifier()
                    
                    syncObject.id?.let { oldToNewIds[it] = newId }
                    syncObject.toVaultItem(
                        overrideUid = newId,
                        overrideAnonymousUid = generateUniqueIdentifier(),
                        syncState = SyncState.MODIFIED
                    )
                }
        } + collectionsWithItemLinkUpdated(transactionList, oldToNewIds)
    }

    @Suppress("UNCHECKED_CAST")
    private fun collectionsWithItemLinkUpdated(
        transactionList: List<XmlTransaction>,
        oldToNewIds: Map<String, String>
    ) = transactionList.mapNotNull { transactionXml ->
        SyncObjectType.forXmlNameOrNull(transactionXml.type)
            ?.takeIf { it == SyncObjectType.COLLECTION }?.let { type ->
                val collection = transactionXml.toObject(type).toVaultItem(
                    overrideUid = generateUniqueIdentifier(),
                    overrideAnonymousUid = generateUniqueIdentifier(),
                    syncState = SyncState.MODIFIED
                ) as VaultItem<SyncObject.Collection>
                collection.copySyncObject {
                    val newVaultItems = vaultItems?.mapNotNull newVaultItem@{ vaultItem ->
                        val newId = vaultItem.id?.let { oldToNewIds[vaultItem.id] }
                        
                            ?: return@newVaultItem null
                        vaultItem.copy { id = newId }
                    }
                    vaultItems = newVaultItems
                }
            }
    }

    private fun List<VaultItem<*>>.toSyncBackupWithIdsList(): Pair<XmlBackup, MutableList<String>> {
        val idsList = mutableListOf<String>()
        val dataList = map {
            idsList.add(it.uid)
            it.syncObject.toTransaction().node
        }
        return XmlBackup(dataList) to idsList
    }

    class InvalidPassword : Exception()

    class FallbackToSharingArchive(
        val data: List<VaultItem<*>>,
        val cacheFile: File,
        val originalException: Exception
    ) : Exception()

    companion object {
        private val EXPORTABLE_DATA_TYPES = setOf(
            SyncObjectType.ADDRESS,
            SyncObjectType.COMPANY,
            SyncObjectType.EMAIL,
            SyncObjectType.IDENTITY,
            SyncObjectType.PERSONAL_WEBSITE,
            SyncObjectType.BANK_STATEMENT,
            SyncObjectType.PHONE,
            SyncObjectType.PAYMENT_PAYPAL,
            SyncObjectType.PAYMENT_CREDIT_CARD,
            SyncObjectType.DRIVER_LICENCE,
            SyncObjectType.FISCAL_STATEMENT,
            SyncObjectType.ID_CARD,
            SyncObjectType.PASSPORT,
            SyncObjectType.SOCIAL_SECURITY_STATEMENT,
            SyncObjectType.SECURE_NOTE,
            SyncObjectType.SECURE_FILE_INFO,
            SyncObjectType.SECURE_NOTE_CATEGORY,
            SyncObjectType.AUTHENTIFIANT,
            SyncObjectType.COLLECTION,
            SyncObjectType.PASSKEY
        )
    }
}