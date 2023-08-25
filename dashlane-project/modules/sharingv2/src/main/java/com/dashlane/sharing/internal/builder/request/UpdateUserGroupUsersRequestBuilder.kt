package com.dashlane.sharing.internal.builder.request

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateUserGroupUsersService
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

class UpdateUserGroupUsersRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
    private val sessionManager: SessionManager
) {
    private val session: Session?
        get() = sessionManager.session

    @Throws(RequestBuilderException.AcceptUserGroupRequestException::class)
    suspend fun build(
        userGroup: UserGroup,
        users: List<UserToUpdate>
    ): UpdateUserGroupUsersService.Request {
        return withContext(defaultCoroutineDispatcher) {
            val login = session?.userId
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("session is null")
            val groupKey = sharingCryptography.getUserGroupKey(userGroup, login)
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("groupKey is null")

            UpdateUserGroupUsersService.Request(
                groupId = UpdateUserGroupUsersService.Request.GroupId(userGroup.groupId),
                revision = userGroup.revision,
                users = users.map {
                    createUserUpdate(groupKey, it)
                }
            )
        }
    }

    private fun createUserUpdate(
        groupKey: CryptographyKey.Raw32,
        userToUpdate: UserToUpdate
    ): UserUpdate {
        val sharingCryptography: SharingCryptographyHelper = sharingCryptography
        val userId: String = userToUpdate.userId
        val publicKey: String = userToUpdate.publicKey
        return UserUpdate(
            userId = UserUpdate.UserId(userId),
            proposeSignature = sharingCryptography.generateProposeSignature(userId, groupKey),
            groupKey = sharingCryptography.generateGroupKeyEncrypted(groupKey, publicKey)
        )
    }
}