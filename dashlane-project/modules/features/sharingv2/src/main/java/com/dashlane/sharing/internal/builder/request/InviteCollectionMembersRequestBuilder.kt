package com.dashlane.sharing.internal.builder.request

import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteCollectionMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.exception.RequestBuilderException.InviteCollectionMembersRequestException
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InviteCollectionMembersRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
    private val sessionManager: SessionManager,
    private val collectionRequestsHelper: CollectionRequestsHelper
) {

    private val session: Session?
        get() = sessionManager.session

    private val login: String
        get() = session!!.userId

    @Throws(InviteCollectionMembersRequestException::class)
    suspend fun create(
        collection: Collection,
        myUserGroups: List<UserGroup>,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>
    ) = withContext(defaultCoroutineDispatcher) {
        val collectionKey = sharingCryptography.getCollectionKeyFromUser(collection, login)
            ?: sharingCryptography.getCollectionKeyFromUserGroup(collection, myUserGroups, login)
            ?: throw InviteCollectionMembersRequestException("Collection key can't be retrieved")
        val userCollectionUploads =
            users.map { user ->
                
                collectionRequestsHelper.createUserCollectionUpload(
                    user,
                    collection.uuid,
                    collectionKey,
                    false
                )
            }
        val userGroupCollectionInvites = groups.map { group ->
            
            collectionRequestsHelper.createUserGroupCollectionInvite(
                collectionKey,
                collection.uuid,
                login,
                group
            ) ?: throw InviteCollectionMembersRequestException("Collection Key can't be encrypted")
        }
        InviteCollectionMembersService.Request(
            collectionId = UuidFormat(collection.uuid),
            userGroups = userGroupCollectionInvites.takeUnless { it.isEmpty() },
            users = userCollectionUploads.takeUnless { it.isEmpty() },
            revision = collection.revision
        )
    }
}