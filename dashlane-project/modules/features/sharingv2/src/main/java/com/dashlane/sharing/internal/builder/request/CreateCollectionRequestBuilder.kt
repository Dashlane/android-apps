package com.dashlane.sharing.internal.builder.request

import com.dashlane.server.api.endpoints.sharinguserdevice.CreateCollectionService
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.sharing.exception.RequestBuilderException.CreateCollectionRequestException
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateCollectionRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
    private val collectionRequestsHelper: CollectionRequestsHelper
) {

    @Throws(CreateCollectionRequestException::class)
    suspend fun create(
        collectionName: String,
        teamId: String,
        author: UserToInvite,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>
    ) = withContext(defaultCoroutineDispatcher) {
        
        val collectionId = sharingCryptography.newGroupUid().uppercase()
        
        val collectionKey = sharingCryptography.newGroupKey()
        
        val collectionSharingKey = sharingCryptography.newCollectionSharingKey()
            ?: throw CreateCollectionRequestException("Collection Sharing Keys can't be generated")
        
        val collectionPrivateKeyEncrypted = sharingCryptography.encryptPrivateKey(
            collectionSharingKey.private,
            collectionKey
        )
        val userCollectionUploads =
            users.map { user ->
                
                collectionRequestsHelper.createUserCollectionUpload(
                    user,
                    collectionId,
                    collectionKey,
                    false
                )
            } + collectionRequestsHelper.createUserCollectionUpload(
                author,
                collectionId,
                collectionKey,
                true
            )
        val userGroupCollectionInvites = groups.map { group ->
            
            collectionRequestsHelper.createUserGroupCollectionInvite(
                collectionKey,
                collectionId,
                author.userId,
                group
            ) ?: throw CreateCollectionRequestException("Collection Key can't be encrypted")
        }
        CreateCollectionService.Request(
            collectionId = UuidFormat(collectionId),
            userGroups = userGroupCollectionInvites.takeUnless { it.isEmpty() },
            privateKey = collectionPrivateKeyEncrypted,
            teamId = teamId.toLong(),
            publicKey = collectionSharingKey.public.value,
            users = userCollectionUploads,
            collectionName = collectionName
        )
    }
}