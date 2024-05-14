package com.dashlane.sharing.internal.builder.request

import androidx.annotation.WorkerThread
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.SharingKeys
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateMultipleItemGroupsService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemUpload
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupInvite
import com.dashlane.server.api.endpoints.sharinguserdevice.UserUpload
import com.dashlane.server.api.pattern.AliasFormat
import com.dashlane.server.api.pattern.UserIdFormat
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.sharing.util.generateItemsUpload
import javax.inject.Inject

class CreateItemGroupRequestsHelper @Inject constructor(
    private val sharingCryptography: SharingCryptographyHelper,
    private val sessionManager: SessionManager
) {
    private val session: Session?
        get() = sessionManager.session

    private val login: String
        get() = session!!.userId

    private val alias: String
        get() = session!!.userId

    @Suppress("LongMethod")
    @Throws(RequestBuilderException.CreateItemRequestException::class)
    @WorkerThread
    fun generateItemGroup(
        groupCryptographyKey: CryptographyKey.Raw32 = sharingCryptography.newGroupKey(),
        item: ItemToShare,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
        itemForEmailing: ItemForEmailing,
        auditLogs: AuditLogDetails?
    ): CreateMultipleItemGroupsService.Request.ItemGroup {
        val groupKey: ByteArray = groupCryptographyKey.toByteArray()
        val itemGroupUid = sharingCryptography.newGroupUid()
        val acceptSignature =
            sharingCryptography.generateAcceptationSignature(itemGroupUid, groupKey)
                ?: throw RequestBuilderException.CreateItemRequestException("Impossible to generate the accept signature")
        val userUploads: MutableList<UserUpload> =
            ArrayList(users.size + 1) 
        val myPublicKey: SharingKeys.Public = sharingCryptography.userPublicKey
            ?: throw RequestBuilderException.CreateItemRequestException("Can't get userPublicKey")
        userUploads.add(
            UserUpload(
                userId = UserIdFormat(login),
                alias = AliasFormat(alias),
                permission = Permission.ADMIN,
                proposeSignature = sharingCryptography.generateProposeSignature(login, groupKey),
                acceptSignature = acceptSignature,
                groupKey = sharingCryptography.generateGroupKeyEncrypted(groupKey, myPublicKey)
            )
        )
        for (userToInvite in users) {
            userUploads.add(createUserUpload(groupKey, userToInvite))
        }
        val groupUploads: MutableList<UserGroupInvite> = ArrayList(groups.size)
        for (groupToInvite in groups) {
            val userGroup = groupToInvite.userGroup
            val userGroupId = userGroup.groupId
            val privateKey: SharingKeys.Private? = sharingCryptography.getUserGroupPrivateKey(userGroup, login)
            val acceptSignatureGroup: String = sharingCryptography.generateAcceptationSignature(
                itemGroupUid,
                groupKey,
                privateKey
            )
                ?: throw RequestBuilderException.CreateItemRequestException("Impossible to generate the accept signature for group")
            val groupKeyEncrypted = sharingCryptography.generateGroupKeyEncrypted(
                groupKey,
                userGroup.publicKey
            ) ?: throw RequestBuilderException.CreateItemRequestException("Impossible to generate groupKeyEncrypted")
            groupUploads.add(
                UserGroupInvite(
                    groupId = UuidFormat(userGroupId),
                    permission = groupToInvite.permission,
                    proposeSignature = sharingCryptography.generateProposeSignature(userGroupId, groupKey),
                    acceptSignature = acceptSignatureGroup,
                    groupKey = groupKeyEncrypted
                )
            )
        }
        val itemsUpload: List<ItemUpload> =
            sharingCryptography.generateItemsUpload(
                item,
                groupCryptographyKey
            ) ?: throw RequestBuilderException.CreateItemRequestException("Impossible to generateItemsUpload")
        return CreateMultipleItemGroupsService.Request.ItemGroup(
            groupId = UuidFormat(itemGroupUid),
            users = userUploads,
            groups = groupUploads.takeUnless { it.isEmpty() },
            items = itemsUpload,
            itemsForEmailing = listOf(itemForEmailing),
            auditLogDetails = auditLogs
        )
    }

    private fun createUserUpload(
        groupKey: ByteArray,
        userToInvite: UserToInvite
    ): UserUpload {
        val userId = userToInvite.userId
        val alias = userToInvite.alias
        return if (userToInvite.publicKey == null) {
            UserUpload(
                userId = UserIdFormat(alias),
                alias = AliasFormat(alias),
                permission = userToInvite.permission,
                proposeSignature = sharingCryptography.generateProposeSignature(alias, groupKey),
                acceptSignature = null, 
                groupKey = null,
                proposeSignatureUsingAlias = true
            )
        } else {
            UserUpload(
                userId = UserIdFormat(userId),
                alias = AliasFormat(alias),
                permission = userToInvite.permission,
                proposeSignature = sharingCryptography.generateProposeSignature(alias, groupKey),
                acceptSignature = null, 
                groupKey = sharingCryptography.generateGroupKeyEncrypted(groupKey, userToInvite.publicKey),
                proposeSignatureUsingAlias = false
            )
        }
    }
}