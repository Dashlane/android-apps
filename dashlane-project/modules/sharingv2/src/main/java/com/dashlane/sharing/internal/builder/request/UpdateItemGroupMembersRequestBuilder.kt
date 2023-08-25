package com.dashlane.sharing.internal.builder.request

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserUpdate
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.internal.model.UserToUpdate
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateItemGroupMembersRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
    private val sessionManager: SessionManager
) {

    private val session: Session?
        get() = sessionManager.session

    @Throws(RequestBuilderException.AcceptUserGroupRequestException::class)
    suspend fun build(
        itemGroup: ItemGroup,
        mMyUserGroupsAcceptedOrPending: List<UserGroup>,
        users: List<UserToUpdate>
    ): UpdateItemGroupMembersService.Request {
        return withContext(defaultCoroutineDispatcher) {
            val login = session?.userId
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("session is null")
            val groupKey: CryptographyKey.Raw32 = sharingCryptography.getGroupKey(
                itemGroup,
                login,
                mMyUserGroupsAcceptedOrPending
            ) ?: throw RequestBuilderException.AcceptItemGroupRequestException("groupKey is null")

            UpdateItemGroupMembersService.Request(
                groupId = UpdateItemGroupMembersService.Request.GroupId(itemGroup.groupId),
                revision = itemGroup.revision,
                users = users.map {
                    createUserUpdate(it, groupKey)
                }
            )
        }
    }

    private fun createUserUpdate(
        userToUpdate: UserToUpdate,
        groupKey: CryptographyKey.Raw32
    ) = UserUpdate(
        permission = userToUpdate.permission,
        userId = UserUpdate.UserId(userToUpdate.userId),
        groupKey = sharingCryptography.generateGroupKeyEncrypted(
            groupKey,
            userToUpdate.publicKey
        ),
        proposeSignature = sharingCryptography.generateProposeSignature(
            userToUpdate.userId,
            groupKey
        )
    )
}