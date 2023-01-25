package com.dashlane.sharing.util

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyKeyGenerator
import com.dashlane.cryptography.SharingCryptography
import com.dashlane.cryptography.SharingKeys
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.asSharingEncryptedBase64
import com.dashlane.cryptography.asSharingSignatureBase64
import com.dashlane.cryptography.decryptBase64ToByteArrayOrNull
import com.dashlane.cryptography.decryptBase64ToUtf8StringOrNull
import com.dashlane.cryptography.decryptRsaBase64OrNull
import com.dashlane.cryptography.encryptByteArrayToBase64String
import com.dashlane.cryptography.encryptRsaToBase64StringOrNull
import com.dashlane.cryptography.encryptUtf8ToBase64String
import com.dashlane.cryptography.signRsaToBase64StringOrNull
import com.dashlane.cryptography.verifySignatureRsaBase64OrDefault
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupMember
import com.dashlane.sharing.SharingKeysHelper
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.getUserGroupMember
import com.dashlane.sharing.model.isUserAcceptedOrPending
import com.dashlane.sharing.model.isUserGroupAcceptedOrPending
import com.dashlane.util.generateUniqueIdentifier
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import javax.inject.Inject



class SharingCryptographyHelper @Inject internal constructor(
    private val cryptography: Cryptography,
    private val sharingCryptography: SharingCryptography,
    private val sharingKeysHelper: SharingKeysHelper,
    private val cryptographyKeyGenerator: CryptographyKeyGenerator
) {
    val userPublicKey: SharingKeys.Public?
        get() = sharingKeysHelper.publicKey?.let(SharingKeys::Public)

    private val userPrivateKey: SharingKeys.Private?
        get() = sharingKeysHelper.privateKey?.let(SharingKeys::Private)

    fun decryptGroupKey(encryptedGroupKey: String?): CryptographyKey.Raw32? =
        decryptGroupKey(encryptedGroupKey, userPrivateKey)

    fun decryptGroupKey(encryptedGroupKey: String?, privateKey: SharingKeys.Private?): CryptographyKey.Raw32? {
        if (encryptedGroupKey == null || privateKey == null) return null
        val bytes = sharingCryptography.decryptRsaBase64OrNull(
            encryptedGroupKey.asSharingEncryptedBase64(),
            privateKey
        ) ?: return null
        if (bytes.size != 32) return null
        return CryptographyKey.ofBytes32(bytes)
    }

    fun decryptItemKey(encryptedItemKey: String?, groupKey: CryptographyKey.Raw32): ByteArray? {
        if (encryptedItemKey == null) return null
        return cryptography.createDecryptionEngine(groupKey)
            .use { decryptionEngine ->
                decryptionEngine.decryptBase64ToByteArrayOrNull(
                    encryptedItemKey.asEncryptedBase64(),
                    compressed = false
                )
            }
    }

    fun decryptItemContent(encryptedItemContent: String?, itemKey: ByteArray): String? {
        if (itemKey.size != 32) return null
        if (encryptedItemContent == null) return null
        return CryptographyKey.ofBytes32(itemKey)
            .use { cryptographyKey -> cryptography.createDecryptionEngine(cryptographyKey) }
            .use { decryptionEngine ->
                decryptionEngine.decryptBase64ToUtf8StringOrNull(
                    encryptedItemContent.asEncryptedBase64(),
                    compressed = true
                )
            }
    }

    fun getPrivateKey(userGroup: UserGroup?, login: String): SharingKeys.Private? {
        if (userGroup == null) return null
        val userGroupKey = decryptGroupKey(userGroup, login)
        val privateKeyEncrypted = userGroup.privateKey
        if (userGroupKey == null) return null
        return cryptography.createDecryptionEngine(userGroupKey)
            .use { decryptionEngine ->
                decryptionEngine.decryptBase64ToUtf8StringOrNull(
                    privateKeyEncrypted.asEncryptedBase64(),
                    compressed = false
                )
            }
            ?.let(SharingKeys::Private)
    }

    fun encryptItemContent(data: String?, itemKey: ByteArray): String? {
        if (itemKey.size != 32) return null
        if (data == null) return null
        return CryptographyKey.ofBytes32(itemKey)
            .use { cryptographyKey -> cryptography.createKwc5EncryptionEngine(cryptographyKey) }
            .use { encryptionEngine ->
                encryptionEngine.encryptUtf8ToBase64String(
                    data,
                    compressed = true
                )
            }
            .value
    }

    fun encryptItemContent(data: String?, itemKey: CryptographyKey.Raw32): String? {
        if (data == null) return null
        return itemKey
            .use { cryptographyKey -> cryptography.createKwc5EncryptionEngine(cryptographyKey) }
            .use { encryptionEngine ->
                encryptionEngine.encryptUtf8ToBase64String(
                    data,
                    compressed = true
                )
            }
            .value
    }

    fun newGroupKey(): CryptographyKey.Raw32 =
        cryptographyKeyGenerator.generateRaw32()

    fun newGroupUid(): String =
        generateUniqueIdentifier()

    fun encryptItemKey(data: ByteArray?, key: CryptographyKey.Raw32): String =
        cryptography.createKwc5EncryptionEngine(key)
            .use { encryptionEngine ->
                encryptionEngine.encryptByteArrayToBase64String(data!!, compressed = false)
            }
            .value

    fun generateAcceptationSignature(
        groupId: String?,
        groupKey: ByteArray?
    ): String? =
        generateAcceptationSignature(groupId, groupKey, userPrivateKey)

    fun generateAcceptationSignature(
        groupId: String?,
        groupKey: ByteArray?,
        privateKey: SharingKeys.Private?
    ): String? {
        if (privateKey == null) return null
        val acceptSignatureToEncrypt = getAcceptSignatureToEncrypt(groupId, groupKey)
        if (acceptSignatureToEncrypt.isEmpty()) return null
        val signature =
            sharingCryptography.signRsaToBase64StringOrNull(acceptSignatureToEncrypt, privateKey)?.value
        return signature ?: ""
    }

    fun verifyAcceptationSignature(
        acceptSignatureEncrypted: String?,
        groupId: String?,
        groupKey: ByteArray?,
        publicKey: SharingKeys.Public?
    ): Boolean {
        if (acceptSignatureEncrypted == null) return false
        val signaturePublicKey = publicKey ?: userPublicKey ?: return false
        val acceptSignatureToEncrypt = getAcceptSignatureToEncrypt(groupId, groupKey)
        return if (acceptSignatureToEncrypt.isEmpty()) {
            false
        } else sharingCryptography.verifySignatureRsaBase64OrDefault(
            acceptSignatureToEncrypt,
            acceptSignatureEncrypted.asSharingSignatureBase64(),
            signaturePublicKey
        )
    }

    fun generateProposeSignature(userIdOrAlias: String, groupKey: CryptographyKey.Raw32): String {
        val proposeSignatureToEncrypt = getProposeSignatureToEncrypt(userIdOrAlias)
        return hmacSha256(proposeSignatureToEncrypt, groupKey.toByteArray())
    }

    fun generateProposeSignature(userIdOrAlias: String, groupKey: ByteArray): String {
        val proposeSignatureToEncrypt = getProposeSignatureToEncrypt(userIdOrAlias)
        return hmacSha256(proposeSignatureToEncrypt, groupKey)
    }

    fun verifyProposeSignature(
        proposeSignatureEncrypted: String,
        userIdOrAlias: String,
        groupKey: ByteArray
    ): Boolean {
        val proposeSignatureToEncrypt = getProposeSignatureToEncrypt(userIdOrAlias)
        return if (proposeSignatureToEncrypt.isEmpty()) {
            false
        } else hmacSha256(proposeSignatureToEncrypt, groupKey) == proposeSignatureEncrypted
    }

    private fun hmacSha256(data: ByteArray, key: ByteArray): String =
        data.toByteString().hmacSha256(key.toByteString()).base64()

    fun generateGroupKeyEncrypted(groupKey: CryptographyKey.Raw32, publicKey: String): String? =
        generateGroupKeyEncrypted(groupKey.toByteArray(), publicKey.let(SharingKeys::Public))

    fun generateGroupKeyEncrypted(groupKey: ByteArray, publicKey: String): String? =
        generateGroupKeyEncrypted(groupKey, publicKey.let(SharingKeys::Public))

    fun generateGroupKeyEncrypted(groupKey: ByteArray, publicKey: SharingKeys.Public): String? {
        return sharingCryptography.encryptRsaToBase64StringOrNull(groupKey, publicKey)?.value
    }

    fun getUserGroupKey(userGroup: UserGroup, login: String): CryptographyKey.Raw32? =
        getGroupKey(userGroup.getUser(login))

    fun getItemGroupKey(itemGroup: ItemGroup?, login: String): CryptographyKey.Raw32? {
        if (itemGroup == null) return null
        return getGroupKey(itemGroup.getUser(login))
    }

    fun getItemGroupKey(itemGroup: ItemGroup?, userGroup: UserGroup, privateKey: SharingKeys.Private?): CryptographyKey.Raw32? {
        if (itemGroup == null) return null
        return getGroupKey(itemGroup.getUserGroupMember(userGroup.groupId), privateKey)
    }

    fun getGroupKey(itemGroup: ItemGroup, userId: String, myUserGroups: List<UserGroup>?): CryptographyKey.Raw32? {
        var groupKeyDecrypted: CryptographyKey.Raw32? = null

        
        
        if (itemGroup.isUserAcceptedOrPending(userId)) {
            groupKeyDecrypted = getGroupKey(itemGroup.getUser(userId))
        } else if (myUserGroups != null) {
            for (userGroup in myUserGroups) {
                if (itemGroup.isUserGroupAcceptedOrPending(userGroup.groupId)) {
                    val privateKey = getPrivateKey(userGroup, userId)
                    groupKeyDecrypted = getItemGroupKey(itemGroup, userGroup, privateKey)
                    return groupKeyDecrypted
                }
            }
        }
        return groupKeyDecrypted
    }

    private fun getGroupKey(userDownload: UserDownload?): CryptographyKey.Raw32? {
        if (userDownload == null) return null
        return decryptGroupKey(userDownload.groupKey)
    }

    private fun getGroupKey(userGroupMember: UserGroupMember?, privateKey: SharingKeys.Private?): CryptographyKey.Raw32? {
        if (userGroupMember == null) return null
        return decryptGroupKey(userGroupMember.groupKey, privateKey)
    }

    private fun decryptGroupKey(userGroup: UserGroup, login: String): CryptographyKey.Raw32? {
        val userDownload = userGroup.getUser(login) ?: return null
        return decryptGroupKey(userDownload.groupKey, userPrivateKey)
    }

    private fun getAcceptSignatureToEncrypt(groupId: String?, groupKey: ByteArray?): ByteArray {
        if (groupId == null || groupKey == null || groupKey.isEmpty()) {
            return ByteArray(0)
        }
        val base64: String = groupKey.toByteString().base64()
        return (groupId + ACCEPT_SIGNATURE + base64).encodeUtf8().toByteArray()
    }

    private fun getProposeSignatureToEncrypt(userId: String): ByteArray {
        return userId.encodeUtf8().toByteArray()
    }

    companion object {
        private const val ACCEPT_SIGNATURE = "-accepted-"
    }
}