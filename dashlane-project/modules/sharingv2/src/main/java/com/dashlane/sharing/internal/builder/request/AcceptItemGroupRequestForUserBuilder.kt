package com.dashlane.sharing.internal.builder.request

import com.dashlane.cryptography.SharingKeys
import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject



class AcceptItemGroupRequestForUserBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
    private val sessionManager: SessionManager
) {

    private val session: Session?
        get() = sessionManager.session

    @Throws(RequestBuilderException.AcceptItemGroupRequestException::class)
    suspend fun buildForUser(
        itemGroup: ItemGroup,
        itemForEmailing: ItemForEmailing
    ): AcceptItemGroupService.Request {
        val login = session?.userId
            ?: throw RequestBuilderException.AcceptItemGroupRequestException("session is null")
        return withContext(defaultCoroutineDispatcher) {
            val groupKey = sharingCryptography.getItemGroupKey(itemGroup, login)
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("Impossible to decrypt the group key")
            val acceptSignature = sharingCryptography.generateAcceptationSignature(
                itemGroup.groupId,
                groupKey.toByteArray()
            )
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("Impossible to generate acceptSignature")
            AcceptItemGroupService.Request(
                revision = itemGroup.revision,
                groupId = AcceptItemGroupService.Request.GroupId(itemGroup.groupId),
                itemsForEmailing = listOf(itemForEmailing),
                acceptSignature = acceptSignature,
                autoAccept = false,
                userGroupId = null
            )
        }
    }

    suspend fun buildForUserGroup(
        itemGroup: ItemGroup,
        userGroup: UserGroup
    ): AcceptItemGroupService.Request {
        return withContext(defaultCoroutineDispatcher) {
            val login = session?.userId
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("session is null")
            val privateKey: SharingKeys.Private? =
                sharingCryptography.getPrivateKey(userGroup, login)
            val groupKey = sharingCryptography.getItemGroupKey(itemGroup, userGroup, privateKey)
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("Impossible to decrypt the group key")
            val acceptSignature = sharingCryptography.generateAcceptationSignature(
                itemGroup.groupId,
                groupKey.toByteArray(),
                privateKey
            )
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("Impossible to generate acceptSignature")

            AcceptItemGroupService.Request(
                revision = itemGroup.revision,
                groupId = AcceptItemGroupService.Request.GroupId(itemGroup.groupId),
                itemsForEmailing = null,
                acceptSignature = acceptSignature,
                autoAccept = false,
                userGroupId = AcceptItemGroupService.Request.UserGroupId(userGroup.groupId)
            )
        }
    }
}