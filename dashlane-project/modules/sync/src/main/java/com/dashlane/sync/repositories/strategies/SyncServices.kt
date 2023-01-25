package com.dashlane.sync.repositories.strategies

import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.server.api.endpoints.sync.SyncUploadService
import com.dashlane.sync.repositories.ServerCredentials



interface SyncServices {

    

    suspend fun download(
        serverCredentials: ServerCredentials,
        request: SyncDownloadService.Request
    ): SyncDownloadService.Data

    

    suspend fun upload(
        serverCredentials: ServerCredentials,
        request: SyncUploadService.Request
    ): SyncUploadService.Data
}