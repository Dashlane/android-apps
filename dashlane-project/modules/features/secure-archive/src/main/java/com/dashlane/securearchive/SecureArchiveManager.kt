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
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.space.SpecificSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.FileUtils
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.tryOrNull
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.XmlBackup
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.toTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
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
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val vaultDataQuery: VaultDataQuery,
    private val secureArchiveDataImporter: SecureArchiveDataImporter,
    private val dataSaver: DataSaver,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val cryptography: Cryptography,
    private val saltGenerator: SaltGenerator,
    private val dataSync: DataSync,
    private val fileUtils: FileUtils
) {
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    private val teamSpaceAccessor
        get() = teamSpaceAccessorProvider.get()

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

    suspend fun import(uri: Uri, password: String): List<VaultItem<*>> =
        withContext(ioDispatcher) {
            val data = readSecureArchive(uri)!!

            yield()

            val fullBackup = CryptographyKey.ofPassword(password)
                .use { cryptographyKey ->
                    cryptography.createDecryptionEngine(cryptographyKey).forXml()
                }
                .use { decryptionEngine -> decryptionEngine.decryptSecureArchive(data)!! }

            yield()

            val savedItems = secureArchiveDataImporter.generateNewVaultList(fullBackup)
                .filter { dataSaver.save(it) }

            dataSync.sync()

            savedItems
        }

    suspend fun export(password: String): List<VaultItem<*>> = withContext(ioDispatcher) {
        if (password.isEmpty()) throw InvalidPassword()

        val filter = vaultFilter {
            specificDataType(EXPORTABLE_DATA_TYPES)
            onlyShareable()
            if (teamSpaceAccessor?.isForcedDomainsEnabled == true) {
                spaceFilter = SpecificSpaceFilter(listOf(TeamSpace.Personal))
            }
        }
        val data = vaultDataQuery.queryAll(filter)

        val (fullBackupXmlNode, idsList) = data.toSyncBackupWithIdsList()

        yield()

        val keyDerivation = CryptographyMarker.Flexible.KeyDerivation.Default.argon2d
        val fixedSalt =
            CryptographyFixedSalt(saltGenerator.generateRandomSalt(keyDerivation.saltLength))
        val archive = CryptographyKey.ofPassword(password)
            .use { cryptographyKey ->
                cryptography.createFlexibleArgon2dEncryptionEngine(
                    cryptographyKey,
                    fixedSalt,
                    keyDerivation
                ).forXml()
            }
            .use { encryptionEngine ->
                encryptionEngine.encryptSecureArchive(idsList, fullBackupXmlNode)
            }

        yield()

        try {
            fileUtils.writeFileToPublicFolder(
                fileName,
                "application/octet-stream"
            ) { outputStream ->
                outputStream.writer().use { writer ->
                    archiveWriter.write(writer, archive)
                }
            }
        } catch (e: IllegalArgumentException) {
            
            val newFile = fileUtils.writeFileToCacheFolder(fileName, "file_provider") { writer ->
                archiveWriter.write(writer, archive)
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