package com.dashlane.sharing.internal.builder.request

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateItemGroupMembersService
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

class UpdateItemGroupMembersRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
) {
    @Throws(RequestBuilderException.AcceptUserGroupRequestException::class)
    suspend fun build(
        username: String?,
        itemGroup: ItemGroup,
        mMyUserGroupsAcceptedOrPending: List<UserGroup>,
        users: List<UserToUpdate>,
    ): UpdateItemGroupMembersService.Request {
        return withContext(defaultCoroutineDispatcher) {
            username ?: throw RequestBuilderException.AcceptItemGroupRequestException("session is null")
            val groupKey: CryptographyKey.Raw32 = sharingCryptography.getItemGroupKey(
                itemGroup,
                username,
                mMyUserGroupsAcceptedOrPending,
                emptyList(),
            ) ?: throw RequestBuilderException.AcceptItemGroupRequestException("groupKey is null")

            UpdateItemGroupMembersService.Request(
                groupId = UuidFormat(itemGroup.groupId),
                revision = itemGroup.revision,
                users = users.map {
                    createUserUpdate(it, groupKey)
                },
            )
        }
    }

    private fun createUserUpdate(
        userToUpdate: UserToUpdate,
        groupKey: CryptographyKey.Raw32,
    ) = UserUpdate(
        permission = userToUpdate.permission,
        userId = UserIdFormat(userToUpdate.userId),
        groupKey = sharingCryptography.generateGroupKeyEncrypted(
            groupKey,
            userToUpdate.publicKey,
        ),
        proposeSignature = sharingCryptography.generateProposeSignature(
            userToUpdate.userId,
            groupKey,
        ),
    )
}