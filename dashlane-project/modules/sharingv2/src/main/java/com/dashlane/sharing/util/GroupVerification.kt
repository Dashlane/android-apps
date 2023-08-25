package com.dashlane.sharing.util

import androidx.annotation.VisibleForTesting
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.SharingKeys
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.RsaStatus
import com.dashlane.server.api.endpoints.sharinguserdevice.Status
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupMember
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAcceptedOrPending

class GroupVerification(
    private val sharingCryptography: SharingCryptographyHelper,
    private val currentUserId: String
) {
    fun isValid(group: UserGroup): Boolean {
        if (group.revision < 1) {
            return false
        }
        val groupId = group.groupId
        val userDownloads = group.users
        val decryptGroupKey: CryptographyKey.Raw32? = try {
            getDecryptGroupKeyAsIndividual(userDownloads, groupId)
        } catch (ex: KeyNotFoundException) {
            return false 
        }
        return if (decryptGroupKey == null) {
            
            true
        } else {
            verifyProposeSignature(userDownloads, decryptGroupKey.toByteArray())
        }
    }

    
    
    fun isValid(group: ItemGroup, myUserGroups: List<UserGroup>): Boolean {
        if (group.revision < 1) {
            return false
        }
        val groupId = group.groupId
        val userDownloads = group.users
        val groupMembers = group.groups
        val decryptGroupKey: CryptographyKey.Raw32? = try {
            getDecryptGroupKey(userDownloads, groupMembers, myUserGroups, groupId)
        } catch (ex: KeyNotFoundException) {
            return false 
        }
        return if (decryptGroupKey == null) {
            
            true
        } else {
            verifyProposeSignature(userDownloads, decryptGroupKey.toByteArray()) &&
                verifyProposeSignatureUserGroup(groupMembers, decryptGroupKey.toByteArray())
        }
    }

    @Throws(KeyNotFoundException::class)
    private fun getDecryptGroupKey(
        userDownloads: List<UserDownload>?,
        groupMembers: List<UserGroupMember>?,
        myUserGroups: List<UserGroup>,
        groupId: String
    ): CryptographyKey.Raw32? {
        val decryptGroupKeyAsIndividual = getDecryptGroupKeyAsIndividual(userDownloads, groupId)
        val decryptGroupKeyAsUserGroup: CryptographyKey.Raw32? =
            groupMembers?.let { getDecryptKeyFromGroup(it, myUserGroups, groupId) }
        return decryptGroupKeyAsUserGroup ?: decryptGroupKeyAsIndividual
    }

    private fun getDecryptGroupKeyAsIndividual(
        userDownloads: List<UserDownload>?,
        groupId: String
    ): CryptographyKey.Raw32? {
        return userDownloads?.let { getDecryptKeyFromUsers(it, groupId) }
    }

    @Throws(KeyNotFoundException::class)
    private fun getDecryptKeyFromGroup(
        groupMembers: List<UserGroupMember>,
        myUserGroups: List<UserGroup>,
        groupId: String
    ): CryptographyKey.Raw32? {
        groupMembers.forEach { userGroupMember ->
            val myUserGroup = myUserGroups.find { userGroup ->
                userGroup.groupId == userGroupMember.groupId
            } ?: return@forEach
            val meInUserGroup = myUserGroup.getUser(currentUserId)
            if (
                userGroupMember.isAcceptedOrPending &&
                meInUserGroup?.isAccepted == true
            ) {
                return getGroupKey(userGroupMember, myUserGroup, groupId)
                    ?: throw KeyNotFoundException() 
            }
        }
        return null
    }

    @Throws(KeyNotFoundException::class)
    private fun getDecryptKeyFromUsers(
        userDownloads: List<UserDownload>,
        groupId: String
    ): CryptographyKey.Raw32? {
        return userDownloads.find { it.userId == currentUserId }?.let {
            getGroupKey(it, groupId)
                ?: throw KeyNotFoundException() 
        }
    }

    private fun getGroupKey(userDownload: UserDownload, groupId: String): CryptographyKey.Raw32? {
        val encryptedGroupKey = userDownload.groupKey ?: return null
        val groupKey = sharingCryptography.decryptGroupKey(encryptedGroupKey) ?: return null
        
        return if (!verifyAcceptSignature(userDownload, groupId, groupKey.toByteArray())) {
            null 
        } else {
            groupKey
        }
    }

    private fun getGroupKey(
        userGroupMember: UserGroupMember,
        myUserGroup: UserGroup,
        itemGroupId: String
    ): CryptographyKey.Raw32? {
        val groupKey = userGroupMember.groupKey ?: return null
        val privateKey: SharingKeys.Private? =
            sharingCryptography.getPrivateKey(myUserGroup, currentUserId)
        val decryptGroupKeyFromUserGroup =
            sharingCryptography.decryptGroupKey(groupKey, privateKey) ?: return null
        
        val publicKeyString = myUserGroup.publicKey
        val publicKey = SharingKeys.Public(publicKeyString)
        return if (!verifyAcceptSignature(
                userGroupMember,
                itemGroupId,
                decryptGroupKeyFromUserGroup.toByteArray(),
                publicKey
            )
        ) {
            null 
        } else {
            decryptGroupKeyFromUserGroup
        }
    }

    private fun verifyAcceptSignature(
        member: UserDownload,
        groupId: String,
        groupKey: ByteArray
    ): Boolean {
        return (
            Status.ACCEPTED != member.status ||
                sharingCryptography.verifyAcceptationSignature(
                    member.acceptSignature,
                    groupId,
                    groupKey,
                    null
                )
        )
    }

    private fun verifyAcceptSignature(
        member: UserGroupMember,
        groupId: String,
        groupKey: ByteArray,
        publicKey: SharingKeys.Public
    ): Boolean {
        return (
            Status.ACCEPTED != member.status ||
                sharingCryptography.verifyAcceptationSignature(
                    member.acceptSignature,
                    groupId,
                    groupKey,
                    publicKey
                )
        )
    }

    @VisibleForTesting
    fun verifyProposeSignature(members: List<UserDownload>?, groupKey: ByteArray): Boolean {
        if (members == null) {
            
            return true
        }
        return members.find { isMemberProposeSignatureIsInvalid(groupKey, it) } == null
    }

    @VisibleForTesting
    fun verifyProposeSignatureUserGroup(
        members: List<UserGroupMember>?,
        groupKey: ByteArray
    ): Boolean {
        if (members == null) {
            
            return true
        }
        return members.find { isMemberProposeSignatureIsInvalid(groupKey, it) } == null
    }

    private fun isMemberProposeSignatureIsInvalid(
        groupKey: ByteArray,
        member: UserDownload
    ): Boolean {
        val memberId = member.userId
        val alias = member.alias
        if (!member.isAcceptedOrPending) {
            return false
        }
        
        
        
        val userIdOrAlias = if (shouldVerifySignatureUsingAlias(member)) {
            alias
        } else {
            memberId
        }
        return !sharingCryptography.verifyProposeSignature(
            member.proposeSignature!!,
            userIdOrAlias,
            groupKey
        )
    }

    private fun isMemberProposeSignatureIsInvalid(
        groupKey: ByteArray,
        member: UserGroupMember
    ): Boolean {
        if (!member.isAcceptedOrPending) {
            return false
        }
        return !sharingCryptography.verifyProposeSignature(
            member.proposeSignature!!,
            member.groupId,
            groupKey
        )
    }

    private fun shouldVerifySignatureUsingAlias(member: UserDownload): Boolean {
        return member.proposeSignatureUsingAlias == true &&
                (member.rsaStatus == RsaStatus.NOKEY || member.rsaStatus == RsaStatus.PUBLICKEY)
    }

    private class KeyNotFoundException : Exception()
}