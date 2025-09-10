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
import com.dashlane.server.api.endpoints.teams.Properties
import com.dashlane.server.api.time.toInstant
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.shouldCaptureActivityLog
import com.dashlane.util.inject.OptionalProvider
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
) {
    
    
    
    private var lastSentActivityLog: ActivityLog? = null

    fun sendCollectionCreatedActivityLog(collection: SummaryObject.Collection) {
        sendActivityLog(
            summary = collection,
            logType = ActivityLog.LogType.USER_CREATED_COLLECTION,
            properties = Properties(collectionName = collection.name),
        )
    }

    fun sendCollectionDeletedActivityLog(collection: SummaryObject.Collection) {
        sendActivityLog(
            summary = collection,
            logType = ActivityLog.LogType.USER_DELETED_COLLECTION,
            properties = Properties(collectionName = collection.name),
        )
    }

    fun sendCollectionRenamedActivityLog(collection: SummaryObject.Collection, previousName: String?) {
        sendActivityLog(
            summary = collection,
            logType = ActivityLog.LogType.USER_RENAMED_COLLECTION,
            properties = Properties(collectionName = collection.name, oldCollectionName = previousName),
        )
    }

    fun sendAddItemToCollectionActivityLog(collection: SummaryObject.Collection, item: SummaryObject) {
        if (item is SummaryObject.Authentifiant) {
            sendActivityLog(
                summary = collection,
                logType = ActivityLog.LogType.USER_ADDED_CREDENTIAL_TO_COLLECTION,
                properties = Properties(
                    collectionName = collection.name,
                    domainUrl = item.urlDomain ?: "",
                ),
            )
        }
    }

    fun sendRemoveItemFromCollectionActivityLog(collection: SummaryObject.Collection, item: SummaryObject) {
        if (item is SummaryObject.Authentifiant) {
            sendActivityLog(
                summary = collection,
                logType = ActivityLog.LogType.USER_REMOVED_CREDENTIAL_FROM_COLLECTION,
                properties = Properties(
                    collectionName = collection.name,
                    domainUrl = item.urlDomain ?: "",
                ),
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
            Action.EXCLUDE_ITEM_FROM_PASSWORD_HEALTH,
            Action.ADD_CUSTOM_FIELD,
            Action.DELETE_CUSTOM_FIELD,
            Action.EDIT_CUSTOM_FIELD,
            Action.CONNECT,
            Action.DISPLAY_QR_CODE,
            ->
                
                return
        }
        val properties = Properties(domainUrl = authentifiant.urlDomain ?: "")

        if (logType == lastSentActivityLog?.logType &&
            properties == lastSentActivityLog?.properties &&
            Instant.now().isBefore(lastSentActivityLog?.dateTime?.toInstant()?.plusSeconds(5))
        ) {
            
            return
        }

        sendActivityLog(
            summary = authentifiant,
            logType = logType,
            properties = properties,
        )
    }

    private fun sendActivityLog(
        summary: SummaryObject,
        logType: ActivityLog.LogType,
        properties: Properties,
    ) {
        val teamSpaceAccessor = teamSpaceAccessorProvider.get() ?: return
        if (!summary.shouldCaptureActivityLog(teamSpaceAccessor)) return

        val log = activityLog(
            logType = logType,
            properties = properties,
        )
        logRepository.queueEvent(log)
        lastSentActivityLog = log
    }
}