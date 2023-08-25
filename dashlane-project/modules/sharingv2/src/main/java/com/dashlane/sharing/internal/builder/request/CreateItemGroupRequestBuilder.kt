package com.dashlane.sharing.internal.builder.request

import androidx.annotation.WorkerThread
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.SharingKeys
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemUpload
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupInvite
import com.dashlane.server.api.endpoints.sharinguserdevice.UserUpload
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.sharing.util.generateItemsUpload
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateItemGroupRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
    private val sessionManager: SessionManager
) {
    private val session: Session?
        get() = sessionManager.session

    private val login: String
        get() = session!!.userId

    private val alias: String
        get() = session!!.userId

    @Throws(RequestBuilderException.CreateItemRequestException::class)
    suspend fun create(
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
        item: ItemToShare,
        itemForEmailing: ItemForEmailing,
        auditLogs: AuditLogDetails?
    ): CreateItemGroupService.Request {
        val groupKey = sharingCryptography.newGroupKey()
        return withContext(defaultCoroutineDispatcher) {
            build(groupKey, item, users, groups, itemForEmailing, auditLogs)
        }
    }

    @Suppress("LongMethod")
    @Throws(RequestBuilderException.CreateItemRequestException::class)
    @WorkerThread
    private fun build(
        groupCryptographyKey: CryptographyKey.Raw32,
        item: ItemToShare,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
        itemForEmailing: ItemForEmailing,
        auditLogs: AuditLogDetails?
    ): CreateItemGroupService.Request {
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
                userId = UserUpload.UserId(login),
                alias = UserUpload.Alias(alias),
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
            val privateKey: SharingKeys.Private? = sharingCryptography.getPrivateKey(userGroup, login)
            val acceptSignatureGroup: String = sharingCryptography.generateAcceptationSignature(
                itemGroupUid,
                groupKey,
                privateKey
            ) ?: throw RequestBuilderException.CreateItemRequestException("Impossible to generate the accept signature for group")
            val groupKeyEncrypted = sharingCryptography.generateGroupKeyEncrypted(
                groupKey,
                userGroup.publicKey
            ) ?: throw RequestBuilderException.CreateItemRequestException("Impossible to generate groupKeyEncrypted")
            groupUploads.add(
                UserGroupInvite(
                    groupId = UserGroupInvite.GroupId(userGroupId),
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
        return CreateItemGroupService.Request(
            groupId = CreateItemGroupService.Request.GroupId(itemGroupUid),
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
                userId = UserUpload.UserId(alias),
                alias = UserUpload.Alias(alias),
                permission = userToInvite.permission,
                proposeSignature = sharingCryptography.generateProposeSignature(alias, groupKey),
                acceptSignature = null, 
                groupKey = null,
                proposeSignatureUsingAlias = true
            )
        } else {
            UserUpload(
                userId = UserUpload.UserId(userId),
                alias = UserUpload.Alias(alias),
                permission = userToInvite.permission,
                proposeSignature = sharingCryptography.generateProposeSignature(alias, groupKey),
                acceptSignature = null, 
                groupKey = sharingCryptography.generateGroupKeyEncrypted(groupKey, userToInvite.publicKey),
                proposeSignatureUsingAlias = false
            )
        }
    }
}