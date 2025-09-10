package com.dashlane.sharing.internal.builder.request

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateUserGroupUsersService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserUpdate
import com.dashlane.server.api.pattern.UserIdFormat
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.internal.model.UserToUpdate
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateUserGroupUsersRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
) {
    @Throws(RequestBuilderException.AcceptUserGroupRequestException::class)
    suspend fun build(
        username: String?,
        userGroup: UserGroup,
        users: List<UserToUpdate>,
    ): UpdateUserGroupUsersService.Request {
        return withContext(defaultCoroutineDispatcher) {
            username ?: throw RequestBuilderException.AcceptItemGroupRequestException("session is null")
            val groupKey = sharingCryptography.getUserGroupKey(userGroup, username)
                ?: throw RequestBuilderException.AcceptItemGroupRequestException("groupKey is null")

            UpdateUserGroupUsersService.Request(
                groupId = UuidFormat(userGroup.groupId),
                revision = userGroup.revision,
                users = users.map {
                    createUserUpdate(groupKey, it)
                },
            )
        }
    }

    private fun createUserUpdate(
        groupKey: CryptographyKey.Raw32,
        userToUpdate: UserToUpdate,
    ): UserUpdate {
        val sharingCryptography: SharingCryptographyHelper = sharingCryptography
        val userId: String = userToUpdate.userId
        val publicKey: String = userToUpdate.publicKey
        return UserUpdate(
            userId = UserIdFormat(userId),
            proposeSignature = sharingCryptography.generateProposeSignature(userId, groupKey),
            groupKey = sharingCryptography.generateGroupKeyEncrypted(groupKey, publicKey),
        )
    }
}