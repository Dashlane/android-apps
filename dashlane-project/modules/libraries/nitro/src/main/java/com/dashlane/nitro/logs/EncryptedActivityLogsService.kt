package com.dashlane.nitro.logs

import com.dashlane.hermes.service.ActivityLogService
import com.dashlane.nitro.Nitro
import com.dashlane.nitro.Nitro.NitroInitializationException
import com.dashlane.nitro.hilt.NitroProxy
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.NitroApi
import com.dashlane.server.api.endpoints.logs.StoreAuditLogs
import com.dashlane.server.api.endpoints.logs.exceptions.ClientIdentifierNotFoundException
import com.dashlane.server.api.endpoints.logs.exceptions.ClientSessionNotParsedException
import com.dashlane.server.api.endpoints.logs.exceptions.ClientStateinNotFoundException
import com.dashlane.server.api.endpoints.logs.exceptions.SecureTunnelMustBeReopenedException
import com.dashlane.server.api.endpoints.logs.exceptions.TunnelUuidNotFoundException
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.exceptions.DashlaneApiHttp400BusinessException
import com.dashlane.server.api.exceptions.DashlaneApiIoException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class EncryptedActivityLogsService(
    @NitroProxy private val nitroProxy: Nitro,
    private val ioDispatcher: CoroutineDispatcher,
) : ActivityLogService {
    private var nitroApi: NitroApi? = null

    override suspend fun sendLogs(
        authorization: Authorization.User,
        logBatch: List<ActivityLog>,
    ) = withContext(ioDispatcher) {
        return@withContext sendToNitro(logBatch, authorization).invalidAuditLogs
    }

    @Throws(NitroInitializationException::class)
    private suspend fun sendToNitro(
        logs: List<ActivityLog>,
        authorization: Authorization.User,
    ): StoreAuditLogs.Data {
        val request = StoreAuditLogs.Request(logs)
        
        
        return try {
            nitroProxy.execute<StoreAuditLogs.Data> {
                endpoints.logs.storeAuditLogs.execute(
                    userAuthorization = authorization,
                    request = request,
                )
            }.data
        } catch (e: DashlaneApiHttp400BusinessException) {
            throw handleLogErrors(e)
        }
    }

    private fun handleLogErrors(e: DashlaneApiHttp400BusinessException) = when (e) {
        
        is TunnelUuidNotFoundException,
        is ClientIdentifierNotFoundException,
        is ClientSessionNotParsedException,
        is ClientStateinNotFoundException,
        
        is SecureTunnelMustBeReopenedException,
        -> {
            
            nitroApi = null
                message = "Nitro Tunnel is closed and will be reopened and logs will be sent later",
                throwable = e,
            )
            DashlaneApiIoException("Nitro Tunnel is closed")
        }
        else -> e
    }
}