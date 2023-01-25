package com.dashlane.sync.repositories.strategies

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.server.api.endpoints.sync.SyncDownloadTransaction
import com.dashlane.server.api.endpoints.sync.SyncUploadService
import com.dashlane.server.api.endpoints.sync.SyncUploadTransaction
import com.dashlane.server.api.time.toInstant
import com.dashlane.sync.repositories.ServerCredentials
import com.dashlane.sync.util.SyncLogs
import javax.inject.Inject



class SyncServicesImpl @Inject constructor(
    private val downloadService: SyncDownloadService,
    private val uploadService: SyncUploadService,
    private val syncLogs: SyncLogs
) : SyncServices {

    override suspend fun download(
        serverCredentials: ServerCredentials,
        request: SyncDownloadService.Request
    ): SyncDownloadService.Data {
        syncLogs.onDownloadRequest(request.timestamp.toInstant(), false, request.needsKeys)

        val response = try {
                downloadService.execute(
                    serverCredentials.toUserAuthorization(),
                    request
                )
            } catch (e: Exception) {
                syncLogs.onDownloadError(e)
                throw e
            }

        val responseData = response.data

        responseData.log()

        return responseData
    }

    override suspend fun upload(
        serverCredentials: ServerCredentials,
        request: SyncUploadService.Request
    ): SyncUploadService.Data {
        syncLogs.onUploadRequest(
            updateCount = request.transactions.count { it.action == SyncUploadTransaction.Action.BACKUP_EDIT },
            deleteCount = request.transactions.count { it.action == SyncUploadTransaction.Action.BACKUP_REMOVE }
        )

        val response = try {
            uploadService.execute(
                serverCredentials.toUserAuthorization(),
                request
            )
        } catch (e: Exception) {
            syncLogs.onUploadError(e)
            throw e
        }

        val responseData = response.data

        syncLogs.onUploadResponse(
            responseData.timestamp.toInstant()
        )

        return responseData
    }

    private fun SyncDownloadService.Data.log() {
        syncLogs.onDownloadResponse(
            timestamp.toInstant(),
            transactions.count { it.action == SyncDownloadTransaction.Action.BACKUP_EDIT },
            transactions.count { it.action == SyncDownloadTransaction.Action.BACKUP_REMOVE }
        )
    }
}

fun ServerCredentials.toUserAuthorization() =
    Authorization.User(login, accessKey, secretKey)
