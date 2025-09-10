package com.dashlane.sharing.util

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.vault.summary.SummaryObject

interface AuditLogHelper {
    suspend fun buildAuditLogDetails(
        authorization: Authorization.User,
        itemIds: List<String>,
    ): List<Pair<String, AuditLogDetails?>>

    suspend fun buildAuditLogDetail(authorization: Authorization.User, itemId: String, summary: SummaryObject? = null): AuditLogDetails?

    suspend fun buildAuditLogDetailsForItemGroups(
        authorization: Authorization.User,
        itemGroups: List<ItemGroup>,
    ): List<Pair<String, AuditLogDetails?>>

    fun getAuditLogDetailForItemGroup(itemGroup: ItemGroup, auditLogs: List<Pair<String, AuditLogDetails?>>): AuditLogDetails?
}

class AuditLogEncryptionException(override val message: String?, override val cause: Throwable?) : DashlaneApiException(message, cause)