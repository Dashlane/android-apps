package com.dashlane.sharing.internal.builder.request

import com.dashlane.cryptography.SharingKeys
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.util.AuditLogHelper
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AcceptItemGroupRequestForUserBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
    private val auditLogHelper: AuditLogHelper,
) {
    @Throws(RequestBuilderException.AcceptItemGroupRequestException::class)
    suspend fun buildForUser(
        username: String?,
        authorization: Authorization.User?,
        itemGroup: ItemGroup,
        itemForEmailing: ItemForEmailing,
    ): AcceptItemGroupService.Request {
        username ?: throw RequestBuilderException.AcceptItemGroupRequestException("session is null")
        authorization ?: throw RequestBuilderException.AcceptItemGroupRequestException("authorization is null")
        return withContext(defaultCoroutineDispatcher) {
            val groupKey = sharingCryptography.getItemGroupKeyFromUser(itemGroup, username)
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("Impossible to decrypt the group key")
            val acceptSignature = sharingCryptography.generateAcceptationSignature(
                itemGroup.groupId,
                groupKey.toByteArray(),
            )
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("Impossible to generate acceptSignature")
            AcceptItemGroupService.Request(
                revision = itemGroup.revision,
                groupId = UuidFormat(itemGroup.groupId),
                itemsForEmailing = listOf(itemForEmailing),
                acceptSignature = acceptSignature,
                autoAccept = false,
                userGroupId = null,
                auditLogDetails = itemGroup.items?.singleOrNull()?.let { auditLogHelper.buildAuditLogDetail(authorization, it.itemId) },
            )
        }
    }

    suspend fun buildForUserGroup(
        username: String?,
        authorization: Authorization.User?,
        itemGroup: ItemGroup,
        userGroup: UserGroup,
    ): AcceptItemGroupService.Request = withContext(defaultCoroutineDispatcher) {
        username ?: throw RequestBuilderException.AcceptItemGroupRequestException("session is null")
        authorization ?: throw RequestBuilderException.AcceptItemGroupRequestException("authorization is null")
        val privateKey: SharingKeys.Private? =
            sharingCryptography.getUserGroupPrivateKey(userGroup, username)
        val groupKey =
            sharingCryptography.getItemGroupKeyFromUserGroup(itemGroup, userGroup, username)
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("Impossible to decrypt the group key")
        val acceptSignature = sharingCryptography.generateAcceptationSignature(
            itemGroup.groupId,
            groupKey.toByteArray(),
            privateKey,
        )
            ?: throw RequestBuilderException.AcceptItemGroupRequestException("Impossible to generate acceptSignature")

        AcceptItemGroupService.Request(
            revision = itemGroup.revision,
            groupId = UuidFormat(itemGroup.groupId),
            itemsForEmailing = null,
            acceptSignature = acceptSignature,
            autoAccept = false,
            userGroupId = UuidFormat(userGroup.groupId),
            auditLogDetails = itemGroup.items?.singleOrNull()?.let { auditLogHelper.buildAuditLogDetail(authorization, it.itemId) },
        )
    }
}