package com.dashlane.sharing.internal.builder.request

import com.dashlane.server.api.endpoints.sharinguserdevice.AcceptUserGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.ProvisioningMethod
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
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
    private val sessionManager: SessionManager
) {

    private val session: Session?
        get() = sessionManager.session

    private val login: String
        get() = session!!.userId

    @Throws(RequestBuilderException.AcceptUserGroupRequestException::class)
    suspend fun build(userGroup: UserGroup): AcceptUserGroupService.Request {
        return withContext(defaultCoroutineDispatcher) {
            val groupKey = sharingCryptography.getUserGroupKey(userGroup, login)
                ?: throw RequestBuilderException.AcceptUserGroupRequestException("Group key is null.")
            val acceptSignature = sharingCryptography.generateAcceptationSignature(
                userGroup.groupId,
                groupKey.toByteArray()
            )
                ?: throw RequestBuilderException.AcceptUserGroupRequestException("Impossible to find the private key")
            AcceptUserGroupService.Request(
                provisioningMethod = ProvisioningMethod.USER,
                revision = userGroup.revision,
                groupId = UuidFormat(userGroup.groupId),
                acceptSignature = acceptSignature
            )
        }
    }
}
