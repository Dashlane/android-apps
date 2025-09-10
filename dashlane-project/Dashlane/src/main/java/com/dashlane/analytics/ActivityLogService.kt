package com.dashlane.analytics

import com.dashlane.hermes.service.ActivityLogService
import com.dashlane.nitro.Nitro
import com.dashlane.nitro.hilt.NitroProxy
import com.dashlane.nitro.logs.EncryptedActivityLogsService
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.teams.ActivityLog
import kotlinx.coroutines.CoroutineDispatcher

class ActivityLogService(
    @NitroProxy nitroProxy: Nitro,
    ioDispatcher: CoroutineDispatcher,
) : ActivityLogService {
    private val encryptedLogService = EncryptedActivityLogsService(
        nitroProxy = nitroProxy,
        ioDispatcher = ioDispatcher,
    )

    override suspend fun sendLogs(
        authorization: Authorization.User,
        logBatch: List<ActivityLog>,
    ) = encryptedLogService.sendLogs(
        authorization = authorization,
        logBatch = logBatch,
    )
}