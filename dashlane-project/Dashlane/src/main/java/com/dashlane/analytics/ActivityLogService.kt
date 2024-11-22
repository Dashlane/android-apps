package com.dashlane.analytics

import com.dashlane.featureflipping.FeatureFlip.ENCRYPTED_AUDIT_LOGS
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.service.ActivityLogService
import com.dashlane.hermes.service.ActivityLogServiceImpl
import com.dashlane.network.ServerUrlOverride
import com.dashlane.nitro.Nitro
import com.dashlane.nitro.logs.EncryptedActivityLogsService
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.endpoints.teams.StoreActivityLogs
import kotlinx.coroutines.CoroutineDispatcher
import java.util.Optional
import javax.inject.Provider

class ActivityLogService(
    nitro: Nitro,
    private val userFeaturesChecker: Provider<UserFeaturesChecker>,
    storeActivityLogs: StoreActivityLogs,
    serverUrlOverride: Optional<ServerUrlOverride>,
    ioDispatcher: CoroutineDispatcher
) : ActivityLogService {
    private val encryptedLogService =
        EncryptedActivityLogsService(nitro, serverUrlOverride, ioDispatcher)
    private val logService = ActivityLogServiceImpl(storeActivityLogs, ioDispatcher)

    private val activityLogService: ActivityLogService
        get() = if (userFeaturesChecker.get().has(ENCRYPTED_AUDIT_LOGS)) {
            encryptedLogService
        } else {
            logService
        }

    override suspend fun sendLogs(
        authorization: Authorization.User,
        logBatch: List<ActivityLog>
    ) = activityLogService.sendLogs(authorization, logBatch)
}