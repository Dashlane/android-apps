package com.dashlane.sharing.util

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.server.api.endpoints.sharinguserdevice.CollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.RsaStatus
import com.dashlane.server.api.endpoints.sharinguserdevice.UserCollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupCollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupMember
import com.dashlane.sharing.model.isAcceptedOrPending
import javax.inject.Inject

class ProposeSignatureVerification @Inject constructor(
    private val sharingCryptography: SharingCryptographyHelper,
) {

    fun verifyProposeSignatureCollection(
        members: List<CollectionDownload>?,
        groupKey: CryptographyKey.Raw32
    ): Boolean {
        if (members == null) {
            
            return true
        }
        runCatching {
            members.forEach {
                verifyProposeSignatureCollectionDownload(groupKey, it)
            }
        }.onFailure { return false }
        return true
    }

    fun verifyProposeSignature(
        members: List<UserDownload>?,
        groupKey: CryptographyKey.Raw32
    ): Boolean {
        if (members == null) {
            
            return true
        }
        runCatching {
            members.forEach {
                verifyProposeSignatureUserDownload(groupKey, it)
            }
        }.onFailure { return false }
        return true
    }

    fun verifyProposeSignatureUserCollection(
        members: List<UserCollectionDownload>?,
        groupKey: CryptographyKey.Raw32
    ): Boolean {
        if (members == null) {
            
            return true
        }
        runCatching {
            members.forEach {
                verifyProposeSignatureUserCollectionDownload(groupKey, it)
            }
        }.onFailure { return false }
        return true
    }

    fun verifyProposeSignatureUserGroupCollection(
        members: List<UserGroupCollectionDownload>?,
        groupKey: CryptographyKey.Raw32
    ): Boolean {
        if (members == null) {
            
            return true
        }
        runCatching {
            members.forEach {
                verifyProposeSignatureUserGroupCollectionDownload(groupKey, it)
            }
        }.onFailure { return false }
        return true
    }

    fun verifyProposeSignatureUserGroup(
        members: List<UserGroupMember>?,
        groupKey: CryptographyKey.Raw32
    ): Boolean {
        if (members == null) {
            
            return true
        }
        runCatching {
            members.forEach {
                verifyProposeSignatureUserGroupMember(groupKey, it)
            }
        }.onFailure { return false }
        return true
    }

    private fun verifyProposeSignatureUserGroupCollectionDownload(
        groupKey: CryptographyKey.Raw32,
        member: UserGroupCollectionDownload
    ) {
        if (!member.status.isAcceptedOrPending) {
            return
        }
        val proposeSignature = member.proposeSignature ?: throw ProposeSignatureInvalidException()
        val result = sharingCryptography.verifyProposeSignature(
            proposeSignature,
            member.uuid,
            groupKey.toByteArray()
        )
        if (!result) {
            throw ProposeSignatureInvalidException()
        }
    }

    private fun verifyProposeSignatureUserCollectionDownload(
        groupKey: CryptographyKey.Raw32,
        member: UserCollectionDownload
    ) {
        if (!member.isAcceptedOrPending) {
            return
        }
        val proposeSignature = member.proposeSignature ?: throw ProposeSignatureInvalidException()
        val result = sharingCryptography.verifyProposeSignature(
            proposeSignature,
            member.login,
            groupKey.toByteArray()
        )
        if (!result) {
            throw ProposeSignatureInvalidException()
        }
    }

    private fun verifyProposeSignatureUserDownload(
        groupKey: CryptographyKey.Raw32,
        member: UserDownload
    ) {
        val memberId = member.userId
        val alias = member.alias
        if (!member.isAcceptedOrPending) {
            return
        }
        
        
        
        val userIdOrAlias = if (shouldVerifySignatureUsingAlias(member)) {
            alias
        } else {
            memberId
        }
        val proposeSignature = member.proposeSignature ?: throw ProposeSignatureInvalidException()
        val result = sharingCryptography.verifyProposeSignature(
            proposeSignature,
            userIdOrAlias,
            groupKey.toByteArray()
        )
        if (!result) {
            throw ProposeSignatureInvalidException()
        }
    }

    private fun verifyProposeSignatureUserGroupMember(
        groupKey: CryptographyKey.Raw32,
        member: UserGroupMember
    ) {
        if (!member.isAcceptedOrPending) {
            return
        }
        val proposeSignature = member.proposeSignature ?: throw ProposeSignatureInvalidException()
        val result = sharingCryptography.verifyProposeSignature(
            proposeSignature,
            member.groupId,
            groupKey.toByteArray()
        )
        if (!result) {
            throw ProposeSignatureInvalidException()
        }
    }

    private fun verifyProposeSignatureCollectionDownload(
        groupKey: CryptographyKey.Raw32,
        member: CollectionDownload
    ) {
        if (!member.status.isAcceptedOrPending) {
            return
        }
        val proposeSignature = member.proposeSignature ?: throw ProposeSignatureInvalidException()
        val result = sharingCryptography.verifyProposeSignature(
            proposeSignature,
            member.uuid,
            groupKey.toByteArray()
        )
        if (!result) {
            throw ProposeSignatureInvalidException()
        }
    }

    private fun shouldVerifySignatureUsingAlias(member: UserDownload): Boolean {
        return member.proposeSignatureUsingAlias == true &&
            (member.rsaStatus == RsaStatus.NOKEY || member.rsaStatus == RsaStatus.PUBLICKEY)
    }
}