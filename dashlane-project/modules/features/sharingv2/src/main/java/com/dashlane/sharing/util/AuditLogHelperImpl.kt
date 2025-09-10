package com.dashlane.sharing.util

import com.dashlane.nitro.Nitro
import com.dashlane.nitro.Nitro.NitroInitializationException
import com.dashlane.nitro.hilt.NitroProxy
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.logs.EncryptAuditLogDetailsBatchWithTeamKey.Request
import com.dashlane.server.api.endpoints.logs.EncryptAuditLogDetailsBatchWithTeamKey.Request.DetailsToEncryptBatch
import com.dashlane.server.api.endpoints.logs.EncryptAuditLogDetailsBatchWithTeamKey.Request.DetailsToEncryptBatch.DetailsToEncrypt
import com.dashlane.server.api.endpoints.logs.EncryptAuditLogDetailsBatchWithTeamKey.Request.DetailsToEncryptBatch.DetailsToEncrypt.Key.DOMAIN
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails.Type.AUTHENTIFIANT
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.exceptions.DashlaneApiHttp400BusinessException
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.navigationUrl
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class AuditLogHelperImpl @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val teamSpaceAccessor: OptionalProvider<TeamSpaceAccessor>,
    @NitroProxy private val nitroProxy: Nitro,
) : AuditLogHelper {
    @Throws(NitroInitializationException::class, DashlaneApiHttp400BusinessException::class, AuditLogEncryptionException::class)
    override suspend fun buildAuditLogDetails(
        authorization: Authorization.User,
        itemIds: List<String>,
    ): List<Pair<String, AuditLogDetails?>> {
        val summaries = itemIds.mapNotNull {
            val summary = loadSummary(it, vaultDataQuery) ?: return@mapNotNull null
            val captureLog = shouldCaptureAuditLog(summaryObject = summary)
            if (captureLog) summary else null
        }
        if (summaries.isEmpty()) return emptyList()
        return createAuditLogDetails(authorization, summaries)
    }

    @Throws(NitroInitializationException::class, DashlaneApiHttp400BusinessException::class, AuditLogEncryptionException::class)
    override suspend fun buildAuditLogDetail(authorization: Authorization.User, itemId: String, summary: SummaryObject?): AuditLogDetails? {
        val s = summary ?: loadSummary(itemId, vaultDataQuery) ?: return null
        val captureLog = shouldCaptureAuditLog(summaryObject = s)
        if (!captureLog) return null
        return createAuditLogDetails(authorization, listOf(s)).firstOrNull()?.second
    }

    @Throws(NitroInitializationException::class, DashlaneApiHttp400BusinessException::class, AuditLogEncryptionException::class)
    override suspend fun buildAuditLogDetailsForItemGroups(authorization: Authorization.User, itemGroups: List<ItemGroup>) =
        buildAuditLogDetails(authorization, itemGroups.mapNotNull { it.items?.singleOrNull()?.itemId })

    override fun getAuditLogDetailForItemGroup(itemGroup: ItemGroup, auditLogs: List<Pair<String, AuditLogDetails?>>) =
        itemGroup.items?.singleOrNull()?.let { item -> auditLogs.firstOrNull { (itemId, _) -> itemId == item.itemId }?.second }

    private fun shouldCaptureAuditLog(summaryObject: SummaryObject): Boolean {
        
        val itemTeamspace = summaryObject.spaceId?.let { teamSpaceAccessor.get()?.get(it) } ?: return false
        return when {
            
            summaryObject !is SummaryObject.Authentifiant -> false
            
            itemTeamspace !is TeamSpace.Business.Current -> false
            
            !itemTeamspace.isCollectSensitiveDataActivityLogsEnabled -> false
            else -> true
        }
    }

    private suspend fun loadSummary(itemId: String, vaultDataQuery: VaultDataQuery) = vaultDataQuery
        .query(
            vaultFilter {
                specificUid(itemId)
                ignoreUserLock()
                allStatusFilter()
            },
        )?.let { vaultItem ->
            when (vaultItem.syncObjectType) {
                SyncObjectType.AUTHENTIFIANT -> vaultItem.toSummary<SummaryObject.Authentifiant>()
                
                else -> null
            }
        }

    @Throws(NitroInitializationException::class, DashlaneApiHttp400BusinessException::class, AuditLogEncryptionException::class)
    private suspend fun createAuditLogDetails(
        authorization: Authorization.User,
        summaries: List<SummaryObject>,
    ): List<Pair<String, AuditLogDetails>> {
        
        val itemIdsWithEncryptedDomains = linkItemIdsWithEncryptedDomains(authorization, summaries)
        return itemIdsWithEncryptedDomains.map { (itemId, encryptedDomain) ->
            itemId to AuditLogDetails(captureLog = true, type = AUTHENTIFIANT, encryptedDetails = encryptedDomain)
        }
    }

    @Throws(NitroInitializationException::class, DashlaneApiHttp400BusinessException::class, AuditLogEncryptionException::class)
    private suspend fun linkItemIdsWithEncryptedDomains(
        authorization: Authorization.User,
        summaries: List<SummaryObject>,
    ): List<Pair<String, String>> {
        
        val itemIdsWithUniqueIdsAndDomains =
            summaries
                .filter { it is SummaryObject.Authentifiant }
                .mapNotNull { summary ->
                    val domain = (summary as SummaryObject.Authentifiant).navigationUrl?.toUrlOrNull()?.root ?: return@mapNotNull null
                    Triple(summary.id, generateUniqueIdentifier(), domain)
                }
        val detailsToEncrypt = itemIdsWithUniqueIdsAndDomains.map { (_, uniqueId, domain) ->
            DetailsToEncryptBatch(
                id = uniqueId,
                detailsToEncrypt = listOf(
                    DetailsToEncrypt(
                        key = DOMAIN,
                        value = domain,
                    ),
                ),
            )
        }
        if (detailsToEncrypt.isEmpty()) return emptyList()
        val response = nitroProxy
            .execute {
                endpoints.logs.encryptAuditLogDetailsBatchWithTeamKey.execute(
                    userAuthorization = authorization,
                    request = Request(detailsToEncryptBatch = detailsToEncrypt),
                )
            }.data
        
        return try {
            itemIdsWithUniqueIdsAndDomains.map { (itemId, uniqueId, _) ->
                val encryptedDomain = response.encryptedDetailsBatch.first { it.id == uniqueId }.encryptedDetails
                itemId to encryptedDomain
            }
        } catch (e: Exception) {
            throw AuditLogEncryptionException("Can't find itemId in encryption response", e)
        }
    }
}