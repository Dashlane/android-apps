package com.dashlane.hermes.service

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.endpoints.teams.InvalidActivityLog

interface ActivityLogService {
    suspend fun sendLogs(
        authorization: Authorization.User,
        logBatch: List<ActivityLog>,
    ): List<InvalidActivityLog>
}