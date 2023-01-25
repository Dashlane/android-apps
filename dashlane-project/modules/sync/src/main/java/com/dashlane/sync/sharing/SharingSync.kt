package com.dashlane.sync.sharing

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sync.SyncDownloadService



interface SharingSync {

    

    suspend fun syncSharing(session: Authorization.User, sharingSummary: SyncDownloadService.Data.SharingSummary)
}

interface SharingComponent {
    val sharingSync: SharingSync
}