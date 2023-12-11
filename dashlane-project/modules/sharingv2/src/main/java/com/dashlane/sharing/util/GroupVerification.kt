package com.dashlane.sharing.util

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.SharingKeys
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.CollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Status
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupCollectionDownload
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAcceptedOrPending

class GroupVerification(
    private val currentUserId: String,
    private val sharingCryptography: SharingCryptographyHelper,
    private val proposeSignatureVerification: ProposeSignatureVerification
) {

    fun isValid(userGroup: UserGroup): Boolean {
        if (userGroup.revision < 1) {
            return false
        }
        val userDownloads = userGroup.users
        val decryptGroupKey: CryptographyKey.Raw32? = try {
            userGroup.decryptUserGroupKey()
        } catch (ex: Exception) {
            return false 
        }
        return if (decryptGroupKey == null) {
            
            true
        } else {
            proposeSignatureVerification.verifyProposeSignature(userDownloads, decryptGroupKey)
        }
    }

    fun isValid(
        itemGroup: ItemGroup,
        myUserGroups: List<UserGroup>,
        myCollections: List<Collection>
    ): Boolean {
        if (itemGroup.revision < 1) {
            return false
        }
        val userDownloads = itemGroup.users
        val groupMembers = itemGroup.groups
        val collection = itemGroup.collections
        val decryptGroupKey: CryptographyKey.Raw32? = try {
            itemGroup.decryptItemGroupKey(myUserGroups, myCollections)
        } catch (ex: Exception) {
            return false 
        }
        return if (decryptGroupKey == null) {
            
            true
        } else {
            proposeSignatureVerification.verifyProposeSignature(userDownloads, decryptGroupKey) &&
                proposeSignatureVerification.verifyProposeSignatureUserGroup(
                    groupMembers,
                    decryptGroupKey
                ) &&
                proposeSignatureVerification.verifyProposeSignatureCollection(
                    collection,
                    decryptGroupKey
                )
        }
    }

    fun isValid(collection: Collection, myUserGroups: List<UserGroup>): Boolean {
        if (collection.revision < 1) {
            return false
        }
        val users = collection.users
        val userGroups = collection.userGroups
        val decryptCollectionKey: CryptographyKey.Raw32? = try {
            collection.decryptCollectionKey(myUserGroups)
        } catch (ex: Exception) {
            return false 
        }
        return if (decryptCollectionKey == null) {
            
            true
        } else {
            proposeSignatureVerification.verifyProposeSignatureUserCollection(
                users,
                decryptCollectionKey
            ) &&
                proposeSignatureVerification.verifyProposeSignatureUserGroupCollection(
                    userGroups,
                    decryptCollectionKey
                )
        }
    }

    private fun Collection.decryptCollectionKey(
        myUserGroups: List<UserGroup>
    ): CryptographyKey.Raw32? {
        val decryptCollectionKeyAsIndividual =
            decryptCollectionKeyAsIndividual()
        val decryptCollectionKeyAsUserGroup: CryptographyKey.Raw32? =
            decryptCollectionKeyAsUserGroup(myUserGroups)
        return decryptCollectionKeyAsIndividual ?: decryptCollectionKeyAsUserGroup
    }

    private fun UserGroup.decryptUserGroupKey(): CryptographyKey.Raw32? {
        val userDownload = getUser(currentUserId) ?: return null
        val userGroupKey =
            sharingCryptography.getUserGroupKey(this, currentUserId)
                ?: throw KeyNotFoundException.KeyNotFoundUserGroupException.KeyNotFoundUserException()
        return if (!verifyAcceptSignature(
                status = userDownload.status,
                acceptSignatureEncrypted = userDownload.acceptSignature,
                groupId = groupId,
                groupKey = userGroupKey.toByteArray(),
                publicKey = null
            )
        ) {
            throw KeyNotVerifiedException.KeyNotVerifiedUserGroupException.KeyNotVerifiedUserException()
        } else {
            userGroupKey
        }
    }

    private fun ItemGroup.decryptItemGroupKey(
        myUserGroups: List<UserGroup>,
        myCollections: List<Collection>
    ): CryptographyKey.Raw32? {
        val decryptGroupKeyAsIndividual: CryptographyKey.Raw32? =
            decryptItemGroupKeyAsIndividual()
        val decryptGroupKeyAsUserGroup: CryptographyKey.Raw32? =
            decryptItemGroupKeyFromUserGroup(myUserGroups)
        val decryptGroupKeyAsCollection: CryptographyKey.Raw32? =
            decryptItemGroupKeyFromCollection(myUserGroups, myCollections)
        return decryptGroupKeyAsUserGroup
            ?: decryptGroupKeyAsIndividual
            ?: decryptGroupKeyAsCollection
    }

    private fun ItemGroup.decryptItemGroupKeyAsIndividual(): CryptographyKey.Raw32? {
        val userDownload = users?.find { it.userId == currentUserId } ?: return null
        val itemGroupKey =
            sharingCryptography.getItemGroupKeyFromUser(this, currentUserId)
                ?: throw KeyNotFoundException.KeyNotFoundItemGroupException.KeyNotFoundUserException()
        return if (!verifyAcceptSignature(
                status = userDownload.status,
                acceptSignatureEncrypted = userDownload.acceptSignature,
                groupId = groupId,
                groupKey = itemGroupKey.toByteArray(),
                publicKey = null
            )
        ) {
            throw KeyNotVerifiedException.KeyNotVerifiedItemGroupException.KeyNotVerifiedUserException()
        } else {
            itemGroupKey
        }
    }

    private fun Collection.decryptCollectionKeyAsIndividual(): CryptographyKey.Raw32? {
        val userCollectionDownload = users?.find { it.login == currentUserId } ?: return null
        val collectionKey = sharingCryptography.getCollectionKeyFromUser(this, currentUserId)
            ?: throw KeyNotFoundException.KeyNotFoundCollectionException.KeyNotFoundUserException()
        return if (!verifyAcceptSignature(
                status = userCollectionDownload.status,
                acceptSignatureEncrypted = userCollectionDownload.acceptSignature,
                groupId = uuid,
                groupKey = collectionKey.toByteArray(),
                publicKey = null
            )
        ) {
            throw KeyNotVerifiedException.KeyNotVerifiedCollectionException.KeyNotVerifiedUserException()
        } else {
            collectionKey
        }
    }

    private fun Collection.decryptCollectionKeyAsUserGroup(
        myUserGroups: List<UserGroup>
    ): CryptographyKey.Raw32? {
        userGroups?.intersectUserGroupCollectionDownload(myUserGroups)
            ?.forEach { userGroupCollectionDownload ->
                val myUserGroup =
                    myUserGroups.find { userGroup -> userGroup.groupId == userGroupCollectionDownload.uuid }
                        ?: return@forEach
                val meInUserGroup = myUserGroup.getUser(currentUserId) ?: return@forEach
                if (
                    userGroupCollectionDownload.status.isAcceptedOrPending &&
                    meInUserGroup.isAccepted
                ) {
                    val collectionKey = sharingCryptography.getCollectionKeyFromUserGroup(
                        this,
                        myUserGroups,
                        currentUserId
                    )
                        ?: throw KeyNotFoundException.KeyNotFoundCollectionException.KeyNotFoundUserGroupException()

                    if (!verifyAcceptSignature(
                            status = userGroupCollectionDownload.status,
                            acceptSignatureEncrypted = userGroupCollectionDownload.acceptSignature,
                            groupId = uuid,
                            groupKey = collectionKey.toByteArray(),
                            publicKey = SharingKeys.Public(myUserGroup.publicKey)
                        )
                    ) {
                        throw KeyNotVerifiedException.KeyNotVerifiedCollectionException.KeyNotVerifiedUserGroupException()
                    } else {
                        return collectionKey
                    }
                }
            }
        return null
    }

    private fun ItemGroup.decryptItemGroupKeyFromCollection(
        myUserGroups: List<UserGroup>,
        myCollections: List<Collection>
    ): CryptographyKey.Raw32? {
        collections?.intersectCollectionDownload(myCollections)
            ?.forEach { collectionDownload ->
                val myCollection = myCollections.find { collection ->
                    collectionDownload.uuid == collection.uuid
                } ?: return@forEach
                val meInCollection = myCollection.getUser(currentUserId) ?: return@forEach
                if (
                    collectionDownload.status.isAcceptedOrPending &&
                    meInCollection.isAccepted
                ) {
                    val itemGroupKey = sharingCryptography.getItemGroupKeyFromCollection(
                        this,
                        myCollection,
                        myUserGroups,
                        currentUserId
                    )
                        ?: throw KeyNotFoundException.KeyNotFoundItemGroupException.KeyNotFoundCollectionException()
                    if (!verifyAcceptSignature(
                            status = collectionDownload.status,
                            acceptSignatureEncrypted = collectionDownload.acceptSignature,
                            groupId = groupId,
                            groupKey = itemGroupKey.toByteArray(),
                            publicKey = SharingKeys.Public(myCollection.publicKey)
                        )
                    ) {
                        throw KeyNotVerifiedException.KeyNotVerifiedItemGroupException.KeyNotVerifiedCollectionException()
                    } else {
                        return itemGroupKey
                    }
                }
            }
        return null
    }

    private fun ItemGroup.decryptItemGroupKeyFromUserGroup(
        myUserGroups: List<UserGroup>
    ): CryptographyKey.Raw32? {
        groups?.forEach { userGroupMember ->
            val myUserGroup = myUserGroups.find { userGroup ->
                userGroup.groupId == userGroupMember.groupId
            } ?: return@forEach
            val meInUserGroup = myUserGroup.getUser(currentUserId) ?: return@forEach

            if (
                userGroupMember.isAcceptedOrPending &&
                meInUserGroup.isAccepted
            ) {
                val itemGroupKey = sharingCryptography.getItemGroupKeyFromUserGroup(
                    this,
                    myUserGroup,
                    currentUserId
                )
                    ?: throw KeyNotFoundException.KeyNotFoundItemGroupException.KeyNotFoundUserGroupException()
                if (!verifyAcceptSignature(
                        status = userGroupMember.status,
                        acceptSignatureEncrypted = userGroupMember.acceptSignature,
                        groupId = groupId,
                        groupKey = itemGroupKey.toByteArray(),
                        publicKey = SharingKeys.Public(myUserGroup.publicKey)
                    )
                ) {
                    throw KeyNotVerifiedException.KeyNotVerifiedItemGroupException.KeyNotVerifiedUserGroupException()
                } else {
                    return itemGroupKey
                }
            }
        }
        return null
    }

    private fun verifyAcceptSignature(
        status: Status?,
        acceptSignatureEncrypted: String?,
        groupId: String,
        groupKey: ByteArray,
        publicKey: SharingKeys.Public?
    ): Boolean {
        return (
            Status.ACCEPTED != status ||
                sharingCryptography.verifyAcceptationSignature(
                    acceptSignatureEncrypted = acceptSignatureEncrypted,
                    groupId = groupId,
                    groupKey = groupKey,
                    publicKey = publicKey
                )
            )
    }

    private fun List<CollectionDownload>.intersectCollectionDownload(collection: List<Collection>): List<CollectionDownload> {
        val ids = this.map { it.uuid } intersect collection.map { it.uuid }.toSet()
        return this.filter { it.uuid in ids }
    }
}
fun List<UserGroupCollectionDownload>.intersectUserGroupCollectionDownload(userGroup: List<UserGroup>): List<UserGroupCollectionDownload> {
    val ids = this.map { it.uuid } intersect userGroup.map { it.groupId }.toSet()
    return this.filter { it.uuid in ids }
}