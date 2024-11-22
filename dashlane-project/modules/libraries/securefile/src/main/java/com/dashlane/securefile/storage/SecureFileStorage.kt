package com.dashlane.securefile.storage

import com.dashlane.securefile.SecureFile
import kotlinx.coroutines.channels.SendChannel
import java.io.File

interface SecureFileStorage {
    suspend fun init(): Boolean

    suspend fun download(secureFile: SecureFile, progression: SendChannel<Float>)

    fun isDownloaded(secureFile: SecureFile, remoteSize: Long?): Boolean

    suspend fun decipherToFileProvider(secureFile: SecureFile): File

    suspend fun decipherToPublicFolder(secureFile: SecureFile, mimeType: String, localSize: Long?)

    suspend fun deleteDecipheredFilesCache()
    suspend fun deleteCipheredFile(secureFile: SecureFile)
}

class DownloadAccessException : Exception("Download access error")