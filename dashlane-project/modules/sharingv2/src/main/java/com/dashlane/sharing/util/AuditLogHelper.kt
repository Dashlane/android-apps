package com.dashlane.sharing.util

import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails.Type
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.isSpaceItem
import com.dashlane.vault.model.navigationUrl
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class AuditLogHelper @Inject constructor(
    private val mainDataAccessor: MainDataAccessor,
    private val teamspaceAccessor: OptionalProvider<TeamspaceAccessor>
) {

    fun buildAuditLogDetails(itemId: String, summary: SummaryObject? = null) =
        (
            summary ?: loadSummary(
            itemId,
            mainDataAccessor.getVaultDataQuery()
        )
        )?.let { summaryObject ->
            val captureLog =
                shouldCaptureAuditLog(
                    summaryObject = summaryObject,
                    teamspace = summaryObject.spaceId?.let { teamspaceAccessor.get()?.get(it) }
                )
            if (captureLog) createAuditLogDetails(summaryObject) else null
        }

    fun buildAuditLogDetails(itemGroup: ItemGroup, summary: SummaryObject? = null) =
        
        itemGroup.items?.singleOrNull()?.let { buildAuditLogDetails(it.itemId, summary) }

    private fun shouldCaptureAuditLog(
        summaryObject: SummaryObject,
        teamspace: Teamspace?
    ): Boolean {
        val isProfessionalSpace =
            summaryObject.isSpaceItem() && summaryObject.spaceId.isNotSemanticallyNull()
        return isProfessionalSpace && teamspace?.isCollectSensitiveDataAuditLogsEnabled ?: false
    }

    private fun loadSummary(itemId: String, vaultDataQuery: VaultDataQuery) = vaultDataQuery.query(
        vaultFilter {
            specificUid(itemId)
            ignoreUserLock()
            allStatusFilter()
        }
    )?.let { vaultItem ->
        when (vaultItem.syncObjectType) {
            SyncObjectType.AUTHENTIFIANT -> vaultItem.toSummary<SummaryObject.Authentifiant>()
            
            else -> null
        }
    }

    private fun createAuditLogDetails(summary: SummaryObject): AuditLogDetails? {
        val type = summary.let {
            when (it) {
                is SummaryObject.Authentifiant -> Type.AUTHENTIFIANT
                else -> return null
            }
        }
        val domain = summary.let {
            if (it is SummaryObject.Authentifiant) {
                it.navigationUrl?.toUrlOrNull()?.root
            } else {
                null
            }
        }
        return AuditLogDetails(captureLog = true, type = type, domain = domain)
    }
}