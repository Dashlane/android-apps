package com.dashlane.sharing.internal.builder.request

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.SharingKeys
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteMultipleItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupInvite
import com.dashlane.server.api.endpoints.sharinguserdevice.UserInvite
import com.dashlane.server.api.pattern.AliasFormat
import com.dashlane.server.api.pattern.UserIdFormat
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.model.isUserAcceptedOrPending
import com.dashlane.sharing.model.isUserGroupAcceptedOrPending
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InviteMultipleItemGroupMembersRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
) {
    @Throws(RequestBuilderException.InviteItemGroupMembersRequestException::class)
    suspend fun create(
        username: String,
        itemGroups: List<Triple<ItemGroup, List<ItemForEmailing>, AuditLogDetails?>>,
        usersUpload: List<UserToInvite>,
        userGroupsUpload: List<GroupToInvite>,
        myUserGroupsAcceptedOrPending: List<UserGroup>,
    ): InviteMultipleItemGroupMembersService.Request {
        return withContext(defaultCoroutineDispatcher) {
            val requestItemGroups = itemGroups.map { triple ->
                val itemGroup = triple.first
                val itemsForEmailing = triple.second
                val auditLogDetails = triple.third

                val groupCryptographyKey =
                    sharingCryptography.getItemGroupKey(itemGroup, username, myUserGroupsAcceptedOrPending, emptyList())
                        ?: throw RequestBuilderException.InviteItemGroupMembersRequestException("Impossible to decrypt the group key")

                val usersToInvite = usersUpload.mapNotNull {
                    
                    if (itemGroup.isUserAcceptedOrPending(it.userId)) {
                        null
                    } else {
                        createUserInvite(groupCryptographyKey, it)
                    }
                }
                val userGroupsToInvite = userGroupsUpload.mapNotNull {
                    val userGroupId: String = it.userGroup.groupId

                    
                    if (itemGroup.isUserGroupAcceptedOrPending(userGroupId)) {
                        null
                    } else {
                        createUserGroupToInvite(username, groupCryptographyKey, it, itemGroup, userGroupId)
                    }
                }

                if (usersToInvite.isEmpty() && userGroupsToInvite.isEmpty()) {
                    throw RequestBuilderException.InviteItemGroupMembersRequestException("No users or groups to invite")
                }

                InviteMultipleItemGroupMembersService.Request.ItemGroup(
                    itemsForEmailing = itemsForEmailing,
                    groupId = UuidFormat(itemGroup.groupId),
                    auditLogDetails = auditLogDetails,
                    groups = userGroupsToInvite.takeUnless { it.isEmpty() },
                    users = usersToInvite.takeUnless { it.isEmpty() },
                    revision = itemGroup.revision,
                )
            }

            InviteMultipleItemGroupMembersService.Request(itemgroups = requestItemGroups)
        }
    }

    private fun createUserGroupToInvite(
        username: String,
        groupCryptographyKey: CryptographyKey.Raw32,
        it: GroupToInvite,
        itemGroup: ItemGroup,
        userGroupId: String,
    ): UserGroupInvite? {
        val groupKeyByteArray = groupCryptographyKey.toByteArray()

        val privateKey: SharingKeys.Private? =
            sharingCryptography.getUserGroupPrivateKey(it.userGroup, username)

        val acceptSignatureGroup: String = sharingCryptography.generateAcceptationSignature(
            itemGroup.groupId,
            groupKeyByteArray,
            privateKey,
        ) ?: return null

        val groupKeyEncrypted = sharingCryptography.generateGroupKeyEncrypted(
            groupKeyByteArray,
            it.userGroup.publicKey,
        ) ?: return null

        return UserGroupInvite(
            groupId = UuidFormat(userGroupId),
            permission = it.permission,
            proposeSignature = sharingCryptography.generateProposeSignature(
                userGroupId,
                groupKeyByteArray,
            ),
            acceptSignature = acceptSignatureGroup,
            groupKey = groupKeyEncrypted,
        )
    }

    private fun createUserInvite(
        groupKey: CryptographyKey.Raw32,
        userToInvite: UserToInvite,
    ): UserInvite {
        val userId = userToInvite.userId
        val alias = userToInvite.alias
        return if (userToInvite.publicKey == null) {
            UserInvite(
                userId = UserIdFormat(alias),
                alias = AliasFormat(alias),
                permission = userToInvite.permission,
                proposeSignature = sharingCryptography.generateProposeSignature(alias, groupKey),
                groupKey = null,
                proposeSignatureUsingAlias = true,
            )
        } else {
            UserInvite(
                userId = UserIdFormat(userId),
                alias = AliasFormat(alias),
                permission = userToInvite.permission,
                proposeSignature = sharingCryptography.generateProposeSignature(alias, groupKey),
                groupKey = sharingCryptography.generateGroupKeyEncrypted(
                    groupKey,
                    userToInvite.publicKey,
                ),
                proposeSignatureUsingAlias = false,
            )
        }
    }
}