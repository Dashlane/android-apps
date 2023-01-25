package com.dashlane.storage.securestorage

import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLogCode119
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.isNotSemanticallyNull
import dagger.Lazy
import javax.inject.Inject

class CryptographyMigrationLoggerImpl @Inject constructor(
    private val sessionManagerLazy: Lazy<SessionManager>,
    private val teamspaceManagerRepositoryLazy: Lazy<TeamspaceManagerRepository>,
    private val bySessionUsageLogRepositoryLazy: Lazy<BySessionRepository<UsageLogRepository>>
) : CryptographyMigrationLogger {

    private val sessionManager
        get() = sessionManagerLazy.get()

    private val teamspaceManager: TeamspaceManager?
        get() = teamspaceManagerRepository[sessionManager.session]
    private val teamspaceManagerRepository
        get() = teamspaceManagerRepositoryLazy.get()

    private val usageLogRepository: UsageLogRepository?
        get() = bySessionUsageLogRepository[sessionManager.session]
    private val bySessionUsageLogRepository
        get() = bySessionUsageLogRepositoryLazy.get()

    override fun logChangeDetected(currentMarker: CryptographyMarker, expectedMarker: CryptographyMarker) {
        log(
            type = UsageLogCode119.Type.CHANGE_DETECTED,
            subType = if (isForcedByTeam()) {
                "tac_setting"
            } else {
                "remote_setting"
            },
            subAction = currentMarker.value,
            cryptoMethod = expectedMarker.value
        )
    }

    override fun logStart(currentMarker: CryptographyMarker, expectedMarker: CryptographyMarker) {
        log(
            type = UsageLogCode119.Type.CRYPTO_MIGRATION,
            action = "start",
            subAction = currentMarker.value,
            cryptoMethod = expectedMarker.value
        )
    }

    override fun logSuccess(currentMarker: CryptographyMarker, expectedMarker: CryptographyMarker) {
        log(
            type = UsageLogCode119.Type.CRYPTO_MIGRATION,
            action = "success",
            subAction = currentMarker.value,
            cryptoMethod = expectedMarker.value
        )
    }

    override fun logErrorDecrypt(expectedPayload: String) {
        logMigrationError("decrypt", expectedPayload)
    }

    override fun logErrorEncrypt(expectedPayload: String) {
        logMigrationError("encrypt", expectedPayload)
    }

    override fun logErrorPayloadNotMatch(expectedPayload: String) {
        logMigrationError("payloadNotMatch", expectedPayload)
    }

    override fun logErrorUnknown(expectedPayload: String) {
        logMigrationError("unknown", expectedPayload)
    }

    private fun isForcedByTeam(): Boolean {
        val teamspaceManager = teamspaceManager ?: return false
        val cryptoUserPayload = teamspaceManager.getFeatureValue(Teamspace.Feature.CRYPTO_FORCED_PAYLOAD)
        
        return cryptoUserPayload.isNotSemanticallyNull()
    }

    private fun logMigrationError(subaction: String, expectedPayload: String) {
        log(
            type = UsageLogCode119.Type.CRYPTO_MIGRATION,
            action = "error",
            subAction = subaction,
            cryptoMethod = expectedPayload
        )
    }

    private fun log(
        type: UsageLogCode119.Type,
        subType: String? = "local",
        action: String? = "error",
        subAction: String? = null,
        cryptoMethod: String? = null
    ) {
        usageLogRepository?.enqueue(
            UsageLogCode119(
                type = type,
                subtype = subType,
                action = action,
                subaction = subAction,
                cryptomethod = cryptoMethod
            )
        )
    }
}