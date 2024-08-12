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
import com.dashlane.teamspaces.isSpaceItem
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.featureflipping.FeatureFlip.VAULT_ACTIVITY_LOGS
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultActivityLogger @Inject constructor(
    private val logRepository: LogRepository,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val userFeaturesChecker: UserFeaturesChecker
) {
    
    
    
    private var lastSentActivityLog: ActivityLog? = null

    fun sendCollectionCreatedActivityLog(collection: SummaryObject.Collection) {
        sendActivityLog(
            summary = collection,
            logType = ActivityLog.LogType.USER_CREATED_COLLECTION,
            properties = ActivityLog.Properties(collection_name = collection.name)
        )
    }

    fun sendCollectionDeletedActivityLog(collection: SummaryObject.Collection) {
        sendActivityLog(
            summary = collection,
            logType = ActivityLog.LogType.USER_DELETED_COLLECTION,
            properties = ActivityLog.Properties(collection_name = collection.name)
        )
    }

    fun sendCollectionRenamedActivityLog(collection: SummaryObject.Collection, previousName: String?) {
        sendActivityLog(
            summary = collection,
            logType = ActivityLog.LogType.USER_RENAMED_COLLECTION,
            properties = ActivityLog.Properties(collection_name = collection.name, old_collection_name = previousName)
        )
    }

    fun sendAddItemToCollectionActivityLog(collection: SummaryObject.Collection, item: SummaryObject) {
        if (item is SummaryObject.Authentifiant) {
            sendActivityLog(
                summary = collection,
                logType = ActivityLog.LogType.USER_ADDED_CREDENTIAL_TO_COLLECTION,
                properties = ActivityLog.Properties(
                    collection_name = collection.name,
                    domain_url = item.urlDomain ?: ""
                )
            )
        }
    }

    fun sendRemoveItemFromCollectionActivityLog(collection: SummaryObject.Collection, item: SummaryObject) {
        if (item is SummaryObject.Authentifiant) {
            sendActivityLog(
                summary = collection,
                logType = ActivityLog.LogType.USER_REMOVED_CREDENTIAL_FROM_COLLECTION,
                properties = ActivityLog.Properties(
                    collection_name = collection.name,
                    domain_url = item.urlDomain ?: ""
                )
            )
        }
    }

    fun sendAuthentifiantActivityLog(vaultItem: VaultItem<*>, action: Action) {
        if (vaultItem.syncObject !is SyncObject.Authentifiant) return
        val authentifiant: SummaryObject.Authentifiant = vaultItem.toSummary()
        val logType = when (action) {
            ADD -> USER_CREATED_CREDENTIAL
            EDIT -> USER_MODIFIED_CREDENTIAL
            DELETE -> USER_DELETED_CREDENTIAL
            Action.ADD_CUSTOM_FIELD,
            Action.DELETE_CUSTOM_FIELD,
            Action.EDIT_CUSTOM_FIELD ->
                
                return
        }
        val properties = ActivityLog.Properties(domain_url = authentifiant.urlDomain ?: "")

        if (logType == lastSentActivityLog?.logType &&
            properties == lastSentActivityLog?.properties &&
            Instant.now().isBefore(lastSentActivityLog?.dateTime?.toInstant()?.plusSeconds(5))
        ) {
            
            debug("Potential duplicate Activity log detected, skipping it...")
            return
        }

        sendActivityLog(
            summary = authentifiant,
            logType = logType,
            properties = properties
        )
    }

    private fun sendActivityLog(
        summary: SummaryObject,
        logType: ActivityLog.LogType,
        properties: ActivityLog.Properties
    ) {
        if (!userFeaturesChecker.has(VAULT_ACTIVITY_LOGS)) return
        val teamSpaceAccessor = teamSpaceAccessorProvider.get() ?: return
        if (!summary.shouldCaptureActivityLog(teamSpaceAccessor)) return

        val log = activityLog(
            logType = logType,
            properties = properties
        )
        logRepository.queueEvent(log)
        lastSentActivityLog = log
    }

    private fun SummaryObject.shouldCaptureActivityLog(teamspaceAccessor: TeamSpaceAccessor) =
        isSpaceItem() && spaceId.isNotSemanticallyNull() && (teamspaceAccessor.get(spaceId) as? TeamSpace.Business)?.isCollectSensitiveDataActivityLogsEnabled ?: false
}