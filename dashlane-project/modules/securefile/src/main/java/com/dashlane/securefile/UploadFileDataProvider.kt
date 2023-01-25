package com.dashlane.securefile

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyKeyGenerator
import com.dashlane.cryptography.EncryptedFile
import com.dashlane.cryptography.encryptToFile
import com.dashlane.network.webservices.UploadFileService
import com.dashlane.securefile.services.CommitService
import com.dashlane.securefile.services.GetUploadLinkService
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.provider.BaseDataProvider
import java.io.InputStream
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.Source
import okio.buffer
import okio.source
import javax.inject.Inject



class UploadFileDataProvider @Inject constructor(
    private val uploadLinkService: GetUploadLinkService,
    private val uploadFileService: UploadFileService,
    private val commitService: CommitService,
    private val mainDataAccessor: MainDataAccessor,
    private val cryptography: Cryptography,
    private val keyGenerator: CryptographyKeyGenerator
) : BaseDataProvider<UploadFileContract.Presenter>(), UploadFileContract.DataProvider {

    override suspend fun createSecureFile(
        filename: String,
        inputStream: InputStream,
        encryptedFile: EncryptedFile
    ) = withContext(Dispatchers.Default) {
        keyGenerator.generateRaw32().use { key ->
            encryptToFile(key, encryptedFile, inputStream)
            SecureFile(null, filename, key.toByteArray(), encryptedFile)
        }
    }

    private fun encryptToFile(
        key: CryptographyKey.Raw32,
        encryptedFile: EncryptedFile,
        inputStream: InputStream
    ) {
        cryptography.createFlexibleNoDerivationEncryptionEngine(key).use { encryptionEngine ->
            encryptionEngine.encryptToFile(encryptedFile) { sink ->
                inputStream.source().buffer().use { source ->
                    source.readAll(sink)
                }
            }
        }
    }

    override suspend fun uploadSecureFile(
        username: String,
        uki: String,
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        
        getLinkAndUpload(username, uki, secureFile, secureFileInfo)
    }

    private suspend fun commit(
        username: String,
        uki: String,
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        runCatching {
            commitService.commit(
                username,
                uki,
                secureFile.id!!,
                secureFileInfo.syncObject.id ?: ""
            )
        }.onSuccess { result ->
            
            val modifiedDataIdentifier = secureFileInfo.copy(syncState = SyncState.MODIFIED)
            mainDataAccessor.getDataSaver().save(modifiedDataIdentifier)
            
            secureFile.encryptedFile!!.value.delete()
            
            presenter.notifyFileUploaded(secureFile, modifiedDataIdentifier)
            val quota = result.content?.quota ?: return@onSuccess
            presenter.notifyStorageSpaceChanged(quota.remainingBytes, quota.maxBytes)
        }.onFailure {
            
            secureFile.encryptedFile!!.value.delete()
            presenter.notifyFileUploadFailed(secureFile, secureFileInfo)
        }
    }

    private suspend fun getLinkAndUpload(
        username: String,
        uki: String,
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        runCatching {
            uploadLinkService.createCall(
                username,
                uki,
                secureFile.encryptedFile!!.value.length(),
                secureFileInfo.syncObject.id
            )
        }.onSuccess { getLink ->
            when (getLink.code) {
                200 -> {
                    val updatedSecureFile = secureFile.copy(
                        id = getLink.content?.fileId
                    )
                    val updatedSecureFileInfo = secureFileInfo.syncObject.copy {
                        downloadKey = updatedSecureFile.id
                    }
                    val updatedSecureFileInfoDataIdentifier =
                        secureFileInfo.copy(syncObject = updatedSecureFileInfo)

                    presenter.notifyFileSpaceReserved(updatedSecureFile, updatedSecureFileInfoDataIdentifier)
                    val uploadSuccessful = uploadFile(
                        username,
                        uki,
                        getLink,
                        updatedSecureFile,
                        updatedSecureFileInfoDataIdentifier
                    )
                    if (!uploadSuccessful) {
                        onLinkAndUploadFailure(secureFile, secureFileInfo)
                    }
                }
                403 -> {
                    handleSpaceFullError(secureFile.encryptedFile, getLink, secureFile, secureFileInfo)
                    return
                }
            }
        }.onFailure {
            onLinkAndUploadFailure(secureFile, secureFileInfo)
        }
    }

    private fun handleSpaceFullError(
        encryptedFile: EncryptedFile?,
        getLink: GetUploadLinkService.Response,
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        
        encryptedFile?.value?.delete()

        when (getLink.content?.errorMessage) {
            
            "MAX_CONTENT_LENGTH_EXCEEDED" -> presenter.notifyFileSizeLimitExceeded(
                secureFile,
                secureFileInfo
            )
            
            "QUOTA_EXCEEDED" -> presenter.notifyMaxStorageSpaceReached(
                secureFile,
                secureFileInfo,
                getLink.code,
                getLink.message
            )
            else -> onLinkAndUploadFailure(secureFile, secureFileInfo)
        }
    }

    private fun onLinkAndUploadFailure(
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        
        secureFile.encryptedFile?.value?.delete()
        presenter.notifyFileUploadFailed(secureFile, secureFileInfo)
    }

    private suspend fun uploadFile(
        username: String,
        uki: String,
        response: GetUploadLinkService.Response,
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ): Boolean {
        val content = response.content!!
        val fields = content.fields.apply {
            addProperty("key", content.fileId)
            addProperty("acl", content.acl)
        }
        val formParts = HashMap<String, RequestBody>()
        fields.entrySet().forEach {
            formParts[it.key] = it.value.asString.toRequestBody(null)
        }
        val requestFile = ProgressRequestBody(presenter, secureFile.encryptedFile!!)
        val filePart = MultipartBody.Part.createFormData("file", null, requestFile)

        
        val upload = uploadFileService.execute(content.url, formParts, filePart)
        if (upload.isSuccessful) {
            
            commit(username, uki, secureFile, secureFileInfo)
        }
        return upload.isSuccessful
    }

    

    class ProgressRequestBody(
        val presenter: UploadFileContract.Presenter,
        val encryptedFile: EncryptedFile
    ) : RequestBody() {

        val file
            get() = encryptedFile.value

        override fun contentType(): MediaType? {
            return null
        }

        override fun writeTo(sink: BufferedSink) {
            UploadProgressSource(
                presenter,
                file.source(),
                file.length()
            ).use { source ->
                sink.writeAll(source)
            }
        }

        override fun contentLength(): Long =
            file.length()
    }

    

    class UploadProgressSource(
        val presenter: UploadFileContract.Presenter,
        val source: Source,
        val contentLength: Long
    ) : Source by source {
        var totalBytesUploaded = 0L

        @OptIn(DelicateCoroutinesApi::class)
        override fun read(sink: Buffer, byteCount: Long): Long {
            val readBytes = source.read(sink, byteCount)
            if (readBytes != -1L) {
                totalBytesUploaded += readBytes
            }
            GlobalScope.launch(Dispatchers.Main) {
                
                presenter.notifyFileUploadProgress(totalBytesUploaded, contentLength)
            }
            return readBytes
        }
    }
}