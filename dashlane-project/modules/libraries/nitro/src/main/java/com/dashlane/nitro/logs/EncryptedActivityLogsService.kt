package com.dashlane.nitro.logs

import com.dashlane.hermes.service.ActivityLogService
import com.dashlane.network.ServerUrlOverride
import com.dashlane.network.webservices.DashlaneUrls.URL_API
import com.dashlane.nitro.Nitro
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.NitroApi
import com.dashlane.server.api.endpoints.logs.StoreAuditLogs
import com.dashlane.server.api.endpoints.logs.exceptions.TunnelUuidNotFoundException
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.exceptions.DashlaneApiIoException
import com.dashlane.util.tryAsSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class EncryptedActivityLogsService(
    private val nitro: Nitro,
    private val serverUrlOverride: Optional<ServerUrlOverride>,
    private val ioDispatcher: CoroutineDispatcher
) : ActivityLogService {

    private var nitroApi: NitroApi? = null

    override suspend fun sendLogs(
        authorization: Authorization.User,
        logBatch: List<ActivityLog>
    ) = withContext(ioDispatcher) {
        return@withContext sendToNitro(logBatch, authorization).invalidAuditLogs
    }

    private suspend fun sendToNitro(
        logs: List<ActivityLog>,
        authorization: Authorization.User
    ): StoreAuditLogs.Data {
        val request = StoreAuditLogs.Request(logs)
        if (nitroApi == null) {
            val nitroUrl = (serverUrlOverride.getOrNull()?.apiUrl ?: URL_API)
                .replaceFirst("/v1/", "/v1-nitro-encryption-service/")
            tryAsSuccess { nitro.authenticate(nitroUrl, true).let { nitroApi = it } }
        }
        
        
        return nitroApi?.let { api ->
            try {
                api.endpoints.logs.storeAuditLogs.execute(authorization, request).data
            } catch (e: TunnelUuidNotFoundException) {
                
                nitroApi = null
                throw DashlaneApiIoException("Nitro Tunnel is closed")
            }
        } ?: throw DashlaneApiIoException("Nitro API not initialized")
    }
}