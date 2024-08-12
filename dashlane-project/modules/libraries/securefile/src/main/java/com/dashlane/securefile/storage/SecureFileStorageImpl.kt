package com.dashlane.securefile.storage

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.asEncryptedFile
import com.dashlane.cryptography.decryptFile
import com.dashlane.network.tools.authorization
import com.dashlane.network.webservices.DownloadFileService
import com.dashlane.securefile.SecureFile
import com.dashlane.server.api.endpoints.securefile.GetSecureFileDownloadLinkService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import com.dashlane.util.FileUtils
import com.dashlane.util.isSemanticallyNull
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.ForwardingSink
import okio.buffer
import okio.sink
import java.io.File
import java.io.OutputStream
import javax.inject.Inject

class SecureFileStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val getSecureFileDownloadLinkService: GetSecureFileDownloadLinkService,
    private val downloadService: DownloadFileService,
    private val cryptography: Cryptography,
    private val fileUtils: FileUtils
) : SecureFileStorage {

    private val secureFilesDir
        get() = File(context.cacheDir, "secure_files")

    private val fileProviderDir
        get() = File(context.cacheDir, "file_provider")

    private val SecureFile.file
        get() = File(secureFilesDir, uniqueFilename)

    private val SecureFile.fileProviderFile
        get() = File(fileProviderDir, uniqueFilename)

    override suspend fun init() = withContext(Dispatchers.IO) {
        secureFilesDir.mkdirs() && fileProviderDir.mkdirs()
    }

    override suspend fun download(secureFile: SecureFile, progression: SendChannel<Float>) {
        val link = fetchDownloadLink(secureFile.id!!)
        val body = downloadService.execute(link)

        withContext(Dispatchers.IO) {
            body.source().use { source ->
                (
                    object : ForwardingSink(secureFile.file.sink()) {
                        var downloadedSize = 0L

                        override fun write(source: Buffer, byteCount: Long) {
                            super.write(source, byteCount)
                            downloadedSize += byteCount
                            progression.trySend(downloadedSize * 100F / body.contentLength())
                        }
                    }
                    ).buffer().use { sink ->
                        sink.writeAll(source)
                    }
            }
        }
    }

    override fun isDownloaded(secureFile: SecureFile, remoteSize: Long?): Boolean {
        return try {
            if (!secureFile.file.exists()) {
                false
            } else {
                val fileLength = secureFile.file.length()
                val fileSize = remoteSize ?: fileLength
                fileLength == fileSize
            }
        } catch (_: Throwable) {
            false
        }
    }

    override suspend fun decipherToFileProvider(secureFile: SecureFile): File {
        withContext(Dispatchers.IO) { decipher(secureFile) { fileProviderFile.outputStream() } }
        return secureFile.fileProviderFile
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, conditional = true)
    override suspend fun decipherToPublicFolder(secureFile: SecureFile, mimeType: String, localSize: Long?) {
        withContext(Dispatchers.IO) {
            
            
            fileUtils.writeFileToPublicFolder(
                secureFile.fileName,
                mimeType,
                localSize
            ) {
                decipher(secureFile) { it }
            }
        }
    }

    override suspend fun deleteDecipheredFilesCache() {
        fileProviderDir.listFiles()?.forEach { it.delete() }
    }

    override suspend fun deleteCipheredFile(secureFile: SecureFile) {
        secureFile.file.delete()
    }

    private suspend fun fetchDownloadLink(id: String): String {
        try {
            val session = sessionManager.session ?: throw DownloadAccessException()
            val response = getSecureFileDownloadLinkService.execute(
                userAuthorization = session.authorization,
                request = GetSecureFileDownloadLinkService.Request(
                    key = id,
                )
            )

            return response.data.url
        } catch (_: DashlaneApiException) {
            throw DownloadAccessException()
        }
    }

    private fun decipher(
        secureFile: SecureFile,
        targetSupplier: SecureFile.() -> OutputStream
    ) = secureFile.targetSupplier().use { target ->
        decryptFile(secureFile, target)
    }

    private fun decryptFile(secureFile: SecureFile, outputStream: OutputStream) {
        CryptographyKey.ofBytes32(secureFile.key)
            .use { cryptographyKey -> cryptography.createDecryptionEngine(cryptographyKey) }
            .use { decryptionEngine ->
                decryptionEngine.decryptFile(secureFile.file.asEncryptedFile()) { source ->
                    outputStream.sink().buffer().use { sink ->
                        sink.writeAll(source)
                    }
                }
            }
    }

    companion object {
        private val SecureFile.uniqueFilename: String
            get() {
                val fileExtension = fileName.substringAfterLast(".", "")
                val hashcode = key.contentHashCode()
                return if (fileExtension.isSemanticallyNull()) {
                    
                    "$hashcode"
                } else {
                    "$hashcode.$fileExtension"
                }
            }
    }
}
