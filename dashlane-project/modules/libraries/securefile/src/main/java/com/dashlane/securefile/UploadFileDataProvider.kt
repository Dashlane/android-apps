package com.dashlane.securefile

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyKeyGenerator
import com.dashlane.cryptography.EncryptedFile
import com.dashlane.cryptography.encryptToFile
import com.dashlane.session.authorization
import com.dashlane.network.webservices.UploadFileService
import com.dashlane.server.api.endpoints.securefile.CommitSecureFileService
import com.dashlane.server.api.endpoints.securefile.GetSecureFileUploadLinkService
import com.dashlane.server.api.endpoints.securefile.exceptions.HardQuotaExceededException
import com.dashlane.server.api.endpoints.securefile.exceptions.MaxContentLengthExceededException
import com.dashlane.server.api.endpoints.securefile.exceptions.SoftQuotaExceededException
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import java.io.InputStream
import javax.inject.Inject

class UploadFileDataProvider @Inject constructor(
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val getSecureFileUploadLinkService: GetSecureFileUploadLinkService,
    private val uploadFileService: UploadFileService,
    private val commitSecureFileService: CommitSecureFileService,
    private val sessionManager: SessionManager,
    private val dataSaver: DataSaver,
    private val cryptography: Cryptography,
    private val keyGenerator: CryptographyKeyGenerator,
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
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        
        getLinkAndUpload(secureFile, secureFileInfo)
    }

    private suspend fun commit(
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        runCatching {
            val session = requireNotNull(sessionManager.session)
            val id = requireNotNull(secureFile.id)
            val secureFileInfoId = requireNotNull(secureFileInfo.syncObject.id)

            commitSecureFileService.execute(
                userAuthorization = session.authorization,
                request = CommitSecureFileService.Request(
                    key = id,
                    secureFileInfoId = secureFileInfoId
                )
            )
        }.onSuccess { response ->
            
            val modifiedDataIdentifier = secureFileInfo.copy(syncState = SyncState.MODIFIED)
            dataSaver.save(modifiedDataIdentifier)
            
            secureFile.encryptedFile!!.value.delete()
            
            presenter.notifyFileUploaded(secureFile, modifiedDataIdentifier)
            val quota = response.data.quota
            presenter.notifyStorageSpaceChanged(quota.remaining, quota.max)
        }.onFailure {
            secureFile.encryptedFile!!.value.delete()
            presenter.notifyFileUploadFailed(secureFile, secureFileInfo)
        }
    }

    private suspend fun getLinkAndUpload(
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        runCatching {
            val session = requireNotNull(sessionManager.session)
            val secureFileInfoId = requireNotNull(secureFileInfo.syncObject.id)
            val contentLength = requireNotNull(secureFile.encryptedFile?.value?.length())

            getSecureFileUploadLinkService.execute(
                userAuthorization = session.authorization,
                request = GetSecureFileUploadLinkService.Request(
                    secureFileInfoId = secureFileInfoId,
                    contentLength = contentLength,
                )
            )
        }.onSuccess { response ->
            val updatedSecureFile = secureFile.copy(
                id = response.data.key,
            )
            val updatedSecureFileInfo = secureFileInfo.syncObject.copy {
                downloadKey = updatedSecureFile.id
            }
            val updatedSecureFileInfoDataIdentifier =
                secureFileInfo.copy(syncObject = updatedSecureFileInfo)

            presenter.notifyFileSpaceReserved(updatedSecureFile, updatedSecureFileInfoDataIdentifier)
            val uploadSuccessful = uploadFile(
                data = response.data,
                secureFile = updatedSecureFile,
                secureFileInfo = updatedSecureFileInfoDataIdentifier
            )
            if (!uploadSuccessful) {
                onLinkAndUploadFailure(secureFile = secureFile, secureFileInfo = secureFileInfo)
            }
        }.onFailure {
            onLinkAndUploadFailure(secureFile = secureFile, secureFileInfo = secureFileInfo, throwable = it)
        }
    }

    private fun onLinkAndUploadFailure(
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>,
        throwable: Throwable? = null,
    ) {
        
        secureFile.encryptedFile?.value?.delete()

        when (throwable) {
            
            is MaxContentLengthExceededException -> presenter.notifyFileSizeLimitExceeded(
                secureFile,
                secureFileInfo
            )
            
            is HardQuotaExceededException,
            is SoftQuotaExceededException -> presenter.notifyMaxStorageSpaceReached(
                secureFile,
                secureFileInfo,
            )
            else -> presenter.notifyFileUploadFailed(secureFile, secureFileInfo)
        }
    }

    private suspend fun uploadFile(
        data: GetSecureFileUploadLinkService.Data,
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ): Boolean {
        val fields: Map<String, String> = data.fields.toMutableMap().apply {
            put("key", data.key)
            put("acl", data.acl)
        }
        val formParts = HashMap<String, RequestBody>()
        fields.forEach {
            formParts[it.key] = it.value.toRequestBody(null)
        }
        val requestFile = ProgressRequestBody(
            presenter,
            secureFile.encryptedFile!!,
            applicationCoroutineScope,
            mainCoroutineDispatcher
        )
        val filePart = MultipartBody.Part.createFormData("file", null, requestFile)

        
        val upload = uploadFileService.execute(data.url, formParts, filePart)
        if (upload.isSuccessful) {
            
            commit(secureFile = secureFile, secureFileInfo = secureFileInfo)
        }
        return upload.isSuccessful
    }

    class ProgressRequestBody(
        val presenter: UploadFileContract.Presenter,
        val encryptedFile: EncryptedFile,
        val applicationCoroutineScope: CoroutineScope,
        val mainCoroutineDispatcher: CoroutineDispatcher
    ) : RequestBody() {

        val file
            get() = encryptedFile.value

        override fun contentType(): MediaType? {
            return null
        }

        override fun writeTo(sink: BufferedSink) {
            UploadProgressSource(
                presenter = presenter,
                source = file.source(),
                contentLength = file.length(),
                applicationCoroutineScope = applicationCoroutineScope,
                mainCoroutineDispatcher = mainCoroutineDispatcher
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
        val contentLength: Long,
        val applicationCoroutineScope: CoroutineScope,
        val mainCoroutineDispatcher: CoroutineDispatcher
    ) : Source by source {
        var totalBytesUploaded = 0L

        override fun read(sink: Buffer, byteCount: Long): Long {
            val readBytes = source.read(sink, byteCount)
            if (readBytes != -1L) {
                totalBytesUploaded += readBytes
            }
            applicationCoroutineScope.launch(mainCoroutineDispatcher) {
                
                presenter.notifyFileUploadProgress(totalBytesUploaded, contentLength)
            }
            return readBytes
        }
    }
}
