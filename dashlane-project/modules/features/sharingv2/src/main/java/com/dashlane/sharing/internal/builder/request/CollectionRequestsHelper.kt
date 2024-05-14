package com.dashlane.sharing.internal.builder.request

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.server.api.endpoints.sharinguserdevice.UserCollectionUpload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupCollectionInvite
import com.dashlane.server.api.pattern.AliasFormat
import com.dashlane.server.api.pattern.UserIdFormat
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.UserToInvite
import com.dashlane.sharing.util.SharingCryptographyHelper
import javax.inject.Inject

class CollectionRequestsHelper @Inject constructor(
    private val sharingCryptography: SharingCryptographyHelper
) {

    internal fun createUserCollectionUpload(
        user: UserToInvite,
        collectionId: String,
        collectionKey: CryptographyKey.Raw32,
        isAuthor: Boolean
    ): UserCollectionUpload {
        val encryptedCollectionKey = user.publicKey?.let {
            sharingCryptography.generateGroupKeyEncrypted(collectionKey, user.publicKey)
        }
        val userId = user.userId
        val alias = user.alias
        val proposeSignature = if (userId.isEmpty()) {
            sharingCryptography.generateProposeSignature(alias, collectionKey)
        } else {
            sharingCryptography.generateProposeSignature(userId, collectionKey)
        }
        val acceptSignature = if (isAuthor) {
            sharingCryptography.generateAcceptationSignature(
                collectionId,
                collectionKey.toByteArray()
            )
        } else {
            null
        }
        return UserCollectionUpload(
            alias = AliasFormat(alias),
            permission = user.permission,
            proposeSignature = proposeSignature,
            login = UserIdFormat(userId),
            collectionKey = encryptedCollectionKey,
            proposeSignatureUsingAlias = userId.isEmpty(),
            acceptSignature = acceptSignature
        )
    }

    internal fun createUserGroupCollectionInvite(
        collectionKey: CryptographyKey.Raw32,
        collectionId: String,
        login: String,
        group: GroupToInvite
    ): UserGroupCollectionInvite? {
        val encryptedCollectionKey = sharingCryptography.generateGroupKeyEncrypted(
            collectionKey,
            group.userGroup.publicKey
        ) ?: return null
        val groupId = group.userGroup.groupId
        val proposeSignature = sharingCryptography.generateProposeSignature(
            groupId,
            collectionKey
        )
        val userGroupPrivateKey = sharingCryptography.getUserGroupPrivateKey(group.userGroup, login)
        val acceptSignature = sharingCryptography.generateAcceptationSignature(
            collectionId,
            collectionKey.toByteArray(),
            userGroupPrivateKey
        )
        return UserGroupCollectionInvite(
            groupUUID = UuidFormat(groupId),
            permission = group.permission,
            proposeSignature = proposeSignature,
            collectionKey = encryptedCollectionKey,
            acceptSignature = acceptSignature
        )
    }
}