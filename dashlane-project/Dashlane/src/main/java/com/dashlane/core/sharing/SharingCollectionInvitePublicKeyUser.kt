package com.dashlane.core.sharing

import com.dashlane.session.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteCollectionMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserCollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.sharing.internal.builder.request.InviteCollectionMembersRequestBuilder
import com.dashlane.sharing.internal.model.UserToInvite
import javax.inject.Inject

class SharingCollectionInvitePublicKeyUser @Inject constructor(
    private val inviteCollectionMembersService: InviteCollectionMembersService,
    private val inviteCollectionMembersRequestBuilder: InviteCollectionMembersRequestBuilder
) {

    suspend fun execute(
        session: Session,
        collectionAccepted: List<Collection>,
        myUserGroups: List<UserGroup>,
        usersToRequest: Map<String, List<UserCollectionDownload>>,
        users: List<GetUsersPublicKeyService.Data.Data>
    ): List<Collection> {
        val authorization = session.authorization
        return collectionAccepted.mapNotNull collections@{ collection ->
            val usersToInvite = usersToRequest[collection.uuid]?.mapNotNull users@{ user ->
                val foundUser = users.find { it.login == user.login || it.email == user.login }
                    ?: return@users null
                val publicKey = foundUser.publicKey ?: return@users null
                UserToInvite(
                    user.login,
                    foundUser.email,
                    Permission.ADMIN,
                    publicKey
                )
            } ?: return@collections null
            if (usersToInvite.isEmpty()) return emptyList()
            val request = inviteCollectionMembersRequestBuilder.create(
                collection,
                myUserGroups,
                usersToInvite,
                emptyList()
            )
            inviteCollectionMembersService.execute(
                authorization,
                request
            ).data.collections
        }.flatten()
    }
}
