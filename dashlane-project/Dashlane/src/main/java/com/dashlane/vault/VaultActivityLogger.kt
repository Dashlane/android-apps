package com.dashlane.vault

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.activityLog
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Action.ADD
import com.dashlane.hermes.generated.definitions.Action.DELETE
import com.dashlane.hermes.generated.definitions.Action.EDIT
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.endpoints.teams.ActivityLog.LogType.USER_CREATED_CREDENTIAL
import com.dashlane.server.api.endpoints.teams.ActivityLog.LogType.USER_DELETED_CREDENTIAL
import com.dashlane.server.api.endpoints.teams.ActivityLog.LogType.USER_MODIFIED_CREDENTIAL
import com.dashlane.server.api.time.toInstant
import com.dashlane.teamspaces.manager.TeamspaceAccessorProvider
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip.VAULT_ACTIVITY_LOGS
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.isSpaceItem
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultActivityLogger @Inject constructor(
    private val logRepository: LogRepository,
    private val teamspaceAccessor: TeamspaceAccessorProvider,
    private val userFeaturesChecker: UserFeaturesChecker
) {
    
    
    
    var lastSentActivityLog: ActivityLog? = null
    fun sendActivityLog(vaultItem: VaultItem<*>, action: Action) {
        if (vaultItem.syncObject !is SyncObject.Authentifiant) return
        val spaceId = vaultItem.syncObject.spaceId
        val authentifiant: SummaryObject.Authentifiant = vaultItem.toSummary()
        val logType = when (action) {
            ADD -> USER_CREATED_CREDENTIAL
            EDIT -> USER_MODIFIED_CREDENTIAL
            DELETE -> USER_DELETED_CREDENTIAL
        }
        sendActivityLog(authentifiant, spaceId, logType)
    }

    private fun sendActivityLog(
        authentifiant: SummaryObject.Authentifiant,
        spaceId: String?,
        logType: ActivityLog.LogType
    ) {
        if (!userFeaturesChecker.has(VAULT_ACTIVITY_LOGS)) return
        val space = spaceId?.let { teamspaceAccessor.get()?.get(it) } ?: return
        if (!authentifiant.shouldCaptureAuditLog(space)) return
        (authentifiant.urlDomain ?: authentifiant.title)?.let {
            val log = activityLog(
                logType = logType,
                properties = ActivityLog.Properties(domain_url = it)
            )
            if (log.logType == lastSentActivityLog?.logType &&
                log.properties == lastSentActivityLog?.properties &&
                log.dateTime.toInstant()
                    .isBefore(lastSentActivityLog?.dateTime?.toInstant()?.plusSeconds(5))
            ) {
                
                debug("Potential duplicate Activity log detected, skipping it...")
                return@let
            }
            logRepository.queueEvent(log)
            lastSentActivityLog = log
        }
    }

    private fun SummaryObject.shouldCaptureAuditLog(teamspace: Teamspace?) =
        isSpaceItem() && spaceId.isNotSemanticallyNull() && teamspace?.isCollectSensitiveDataAuditLogsEnabled ?: false
}