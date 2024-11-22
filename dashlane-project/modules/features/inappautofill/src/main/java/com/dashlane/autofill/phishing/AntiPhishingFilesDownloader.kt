package com.dashlane.autofill.phishing

import android.content.Context
import com.dashlane.autofill.phishing.model.PhishingDomainList
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.SharingCryptography
import com.dashlane.cryptography.SharingKeys
import com.dashlane.cryptography.asEncryptedFile
import com.dashlane.cryptography.asSharingEncryptedBase64
import com.dashlane.cryptography.decryptFile
import com.dashlane.cryptography.decryptRsaPkcs1OaepPaddingBase64OrNull
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.canUseAntiPhishing
import com.dashlane.network.webservices.DownloadFileService
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.file.GetFileMetaService
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.sharing.SharingKeysHelper
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.OutputStream
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okio.buffer
import okio.sink

class AntiPhishingFilesDownloader @Inject constructor(
    private val sessionManager: SessionManager,
    private val downloadService: DownloadFileService,
    private val getFileService: GetFileMetaService,
    private val preferencesManager: PreferencesManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val cryptography: Cryptography,
    private val sharingKeysHelper: SharingKeysHelper,
    private val sharingCryptography: SharingCryptography,
    @ApplicationCoroutineScope private val appCoroutineScope: CoroutineScope,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) {
    
    private var localModelVersion: Double? = null
    private var localDenyListVersion: Double? = null
    private var localAllowListVersion: Double? = null

    
    private val modelNameWithVersion: String
        get() = "$PHISHING_MODEL_FILE_NAME.v$localModelVersion"
    private val denyFileNameWithVersion: String
        get() = "$PHISHING_EXCLUSION_FILE_NAME.v$localDenyListVersion"
    private val allowFileNameWithVersion: String
        get() = "$PHISHING_ALLOW_FILE_NAME.v$localAllowListVersion"

    private suspend fun initLocalVersionIfNeeded() {
        
        if (localModelVersion == null) {
            localModelVersion = getHighestLocalVersion(PHISHING_MODEL_FILE_NAME) ?: 0.0
        }
        if (localDenyListVersion == null) {
            localDenyListVersion = getHighestLocalVersion(PHISHING_EXCLUSION_FILE_NAME) ?: 0.0
        }
        if (localAllowListVersion == null) {
            localAllowListVersion = getHighestLocalVersion(PHISHING_ALLOW_FILE_NAME) ?: 0.0
        }
    }

    suspend fun downloadFilePhishingFiles() {
        runIfPhishdroidEnabled {
            if (preferencesManager[sessionManager.session?.username].latestCheckPhishingModelVersion.plus(Duration.ofDays(1))
                    .isAfter(Instant.now())
            ) {
                return
            }
            val session = sessionManager.session ?: return

            appCoroutineScope.launch(ioDispatcher) {
                try {
                    initLocalVersionIfNeeded()

                    val data = getFileService.execute(
                        session.authorization,
                        GetFileMetaService.Request(
                            mapOf(
                                PHISHING_MODEL_FILE_NAME to localModelVersion!!,
                                PHISHING_EXCLUSION_FILE_NAME to localDenyListVersion!!,
                                PHISHING_ALLOW_FILE_NAME to localAllowListVersion!!
                            )
                        )
                    )
                    preferencesManager[sessionManager.session?.username].latestCheckPhishingModelVersion = Instant.now()
                    data.data.fileInfos.entries
                        .filter { it.value.status == GetFileMetaService.Data.FileInfo.Status.UPDATE_AVAILABLE }
                        .forEach {
                            when (it.key) {
                                PHISHING_MODEL_FILE_NAME -> {
                                    localModelVersion = it.value.revision!!
                                    downloadFile(it.value, modelNameWithVersion)
                                }
                                PHISHING_EXCLUSION_FILE_NAME -> {
                                    localDenyListVersion = it.value.revision!!
                                    downloadFile(it.value, denyFileNameWithVersion)
                                }
                                PHISHING_ALLOW_FILE_NAME -> {
                                    localAllowListVersion = it.value.revision!!
                                    downloadFile(it.value, allowFileNameWithVersion)
                                }
                            }
                        }
                } catch (e: Exception) {
                } finally {
                    removeOldFiles(listOf(modelNameWithVersion, denyFileNameWithVersion, allowFileNameWithVersion))
                }
            }
        }
    }

    private fun decipherFile(encryptedFile: File, key: String, outputStream: OutputStream) {
        val privateKey = sharingKeysHelper.privateKey?.let(SharingKeys::Private)
            ?: throw Exception("[PhishDroid] Can't find user's private key")
        val decryptedKey = sharingCryptography.decryptRsaPkcs1OaepPaddingBase64OrNull(
            key.asSharingEncryptedBase64(),
            privateKey
        ) ?: throw Exception("[PhishDroid] Error deciphering key")
        CryptographyKey.ofBytes64(decryptedKey)
            .use { cryptographyKey -> cryptography.createDecryptionEngine(cryptographyKey) }
            .use { decryptionEngine ->
                decryptionEngine.decryptFile(encryptedFile.asEncryptedFile()) { source ->
                    outputStream.sink().buffer().use { sink ->
                        sink.writeAll(source)
                    }
                }
            }
    }

    private suspend fun downloadFile(value: GetFileMetaService.Data.FileInfo, fileName: String) {
        if (value.url == null) {
            throw Exception("[PhishDroid] URL is null")
        }
        val responseBody: ResponseBody = downloadService.execute(value.url!!)
        getSecuredFile(fileName).outputStream().use { fileOut ->
            fileOut.write(responseBody.bytes())
        }
        getFile(fileName).outputStream().use { fileOut ->
            decipherFile(getSecuredFile(fileName), value.key!!, fileOut)
        }
    }

    private fun getFile(fileName: String): File {
        File(context.filesDir, PHISHING_FILES_FOLDER_NAME).also {
            if (!it.exists() && !it.mkdir()) {
                throw Exception("[PhishDroid] Can't create folder")
            }
            return File(it.path, fileName)
        }
    }

    private fun getSecuredFile(fileName: String): File {
        File(context.filesDir, PHISHING_FILES_FOLDER_NAME).also {
            if (!it.exists() && !it.mkdir()) {
                throw Exception("[PhishDroid] Can't create folder")
            }
            return File(it.path, "$fileName$ENCRYPTED_SUFFIX")
        }
    }

    suspend fun getPhishingModelFile(): File? =
        runIfPhishdroidEnabled {
            runCatching {
                withContext(ioDispatcher) {
                    initLocalVersionIfNeeded()
                    getFile(modelNameWithVersion)
                        .takeIf { it.exists() && it.isFile }
                }
            }.getOrNull()
        }

    suspend fun getPhishingDenyList(): PhishingDomainList? =
        runIfPhishdroidEnabled {
            runCatching {
                withContext(ioDispatcher) {
                    initLocalVersionIfNeeded()
                    getFile(denyFileNameWithVersion).readText().let { json ->
                        Gson().fromJson(json, PhishingDomainList::class.java)
                    }
                }
            }.getOrNull()
        }

    suspend fun getPhishingAllowList(): PhishingDomainList? =
        runIfPhishdroidEnabled {
            runCatching {
                withContext(ioDispatcher) {
                    initLocalVersionIfNeeded()
                    getFile(allowFileNameWithVersion).readText().let { json ->
                        Gson().fromJson(json, PhishingDomainList::class.java)
                    }
                }
            }.getOrNull()
        }

    private fun removeOldFiles(usedFileNames: List<String>) {
        File(context.filesDir, PHISHING_FILES_FOLDER_NAME).also {
            if (it.exists()) {
                it.listFiles()?.forEach { file ->
                    if (!usedFileNames.contains(file.name) && !file.delete()) {
                    }
                }
            }
        }
    }

    private suspend fun getHighestLocalVersion(fileName: String): Double? =
        runCatching {
            withContext(ioDispatcher) {
                File(context.filesDir, PHISHING_FILES_FOLDER_NAME)
                    
                    .listFiles { _, name ->
                        name.startsWith(fileName) && name.endsWith(ENCRYPTED_SUFFIX).not()
                    }
                    
                    ?.filter { file -> file.exists() }
                    
                    ?.mapNotNull { file ->
                        file.name.split(".v").last().toDoubleOrNull()
                    }
                    
                    ?.maxOrNull()
            }
        }.getOrNull()

    private inline fun <T> runIfPhishdroidEnabled(block: () -> T?): T? {
        if (!userFeaturesChecker.canUseAntiPhishing() || !preferencesManager[sessionManager.session?.username].isAntiPhishingEnable) {
            return null
        }
        return block()
    }

    companion object {
        private const val PHISHING_FILES_FOLDER_NAME = "phishdroid"
        private const val PHISHING_MODEL_FILE_NAME = "catfish_model.tflite"
        private const val PHISHING_EXCLUSION_FILE_NAME = "phishing_urls_test.json"
        private const val PHISHING_ALLOW_FILE_NAME = "allow-list-prod.json"
        private const val ENCRYPTED_SUFFIX = ".encrypted"
    }
}