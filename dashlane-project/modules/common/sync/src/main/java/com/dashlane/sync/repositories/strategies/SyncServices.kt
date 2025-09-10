package com.dashlane.sync.repositories.strategies

import com.dashlane.server.api.endpoints.sync.GetLatestContentService
import com.dashlane.server.api.endpoints.sync.UploadContentService
import com.dashlane.sync.repositories.ServerCredentials

interface SyncServices {
    suspend fun download(
        serverCredentials: ServerCredentials,
        request: GetLatestContentService.Request,
    ): GetLatestContentService.Data

    suspend fun upload(
        serverCredentials: ServerCredentials,
        request: UploadContentService.Request,
    ): UploadContentService.Data
}