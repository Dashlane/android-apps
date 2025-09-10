package com.dashlane.sharing.internal.builder.request

import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptUserGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.ProvisioningMethod
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AcceptUserGroupRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
) {
    @Throws(RequestBuilderException.AcceptUserGroupRequestException::class)
    suspend fun build(username: String, userGroup: UserGroup): AcceptUserGroupService.Request {
        return withContext(defaultCoroutineDispatcher) {
            val groupKey = sharingCryptography.getUserGroupKey(userGroup, username)
                ?: throw RequestBuilderException.AcceptUserGroupRequestException("Group key is null.")
            val acceptSignature = sharingCryptography.generateAcceptationSignature(
                userGroup.groupId,
                groupKey.toByteArray(),
            )
                ?: throw RequestBuilderException.AcceptUserGroupRequestException("Impossible to find the private key")
            AcceptUserGroupService.Request(
                provisioningMethod = ProvisioningMethod.USER,
                revision = userGroup.revision,
                groupId = UuidFormat(userGroup.groupId),
                acceptSignature = acceptSignature,
            )
        }
    }
}