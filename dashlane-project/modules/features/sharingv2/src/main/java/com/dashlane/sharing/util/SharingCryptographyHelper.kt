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
import com.dashlane.cryptography.generateSharingKeysOrNull
import com.dashlane.cryptography.signRsaToBase64StringOrNull
import com.dashlane.cryptography.verifySignatureRsaBase64OrDefault
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.sharing.SharingKeysHelper
import com.dashlane.sharing.model.getCollectionDownload
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.getUserGroupMember
import com.dashlane.sharing.model.isCollectionAcceptedOrPending
import com.dashlane.sharing.model.isUserAcceptedOrPending
import com.dashlane.sharing.model.isUserGroupAcceptedOrPending
import com.dashlane.util.generateUniqueIdentifier
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import javax.inject.Inject

class SharingCryptographyHelper @Inject constructor(
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

    fun decryptGroupKey(
        encryptedGroupKey: String?,
        privateKey: SharingKeys.Private?
    ): CryptographyKey.Raw32? {
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

    fun getUserGroupPrivateKey(userGroup: UserGroup, login: String): SharingKeys.Private? {
        val userGroupKey = getUserGroupKey(userGroup, login) ?: return null
        val privateKeyEncrypted = userGroup.privateKey
        return cryptography.createDecryptionEngine(userGroupKey)
            .use { decryptionEngine ->
                decryptionEngine.decryptBase64ToUtf8StringOrNull(
                    privateKeyEncrypted.asEncryptedBase64(),
                    compressed = false
                )
            }
            ?.let(SharingKeys::Private)
    }

    fun getCollectionPrivateKey(
        collection: Collection,
        myUserGroups: List<UserGroup>,
        login: String
    ): SharingKeys.Private? {
        val privateKeyEncrypted = collection.privateKey
        val collectionKey = getCollectionKey(collection, myUserGroups, login) ?: return null
        return cryptography.createDecryptionEngine(collectionKey)
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

    fun newCollectionSharingKey() = sharingCryptography.generateSharingKeysOrNull()

    fun encryptItemKey(data: CryptographyKey.Raw32, key: CryptographyKey.Raw32) =
        cryptography.createKwc5EncryptionEngine(key)
            .use { encryptionEngine ->
                encryptionEngine.encryptByteArrayToBase64String(
                    data.toByteArray(),
                    compressed = false
                )
            }.value

    fun encryptPrivateKey(data: SharingKeys.Private, key: CryptographyKey.Raw32) =
        cryptography.createKwc5EncryptionEngine(key)
            .use { encryptionEngine ->
                encryptionEngine.encryptUtf8ToBase64String(
                    data.value,
                    compressed = false
                )
            }.value

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
            sharingCryptography.signRsaToBase64StringOrNull(
                acceptSignatureToEncrypt,
                privateKey
            )?.value
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
        } else {
            sharingCryptography.verifySignatureRsaBase64OrDefault(
                acceptSignatureToEncrypt,
                acceptSignatureEncrypted.asSharingSignatureBase64(),
                signaturePublicKey
            )
        }
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
        } else {
            hmacSha256(proposeSignatureToEncrypt, groupKey) == proposeSignatureEncrypted
        }
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

    fun getItemGroupKeyFromCollection(
        itemGroup: ItemGroup,
        collection: Collection,
        myUserGroups: List<UserGroup>,
        userId: String
    ): CryptographyKey.Raw32? {
        val collectionPrivateKey = getCollectionPrivateKey(collection, myUserGroups, userId) ?: return null
        val collectionDownload = itemGroup.getCollectionDownload(collection.uuid) ?: return null
        return decryptGroupKey(collectionDownload.itemGroupKey, collectionPrivateKey)
    }

    fun getItemGroupKeyFromUserGroup(
        itemGroup: ItemGroup,
        userGroup: UserGroup,
        userId: String
    ): CryptographyKey.Raw32? {
        val userGroupPrivateKey = getUserGroupPrivateKey(userGroup, userId) ?: return null
        val userGroupMember = itemGroup.getUserGroupMember(userGroup.groupId) ?: return null
        return decryptGroupKey(userGroupMember.groupKey, userGroupPrivateKey)
    }

    fun getItemGroupKeyFromUser(
        itemGroup: ItemGroup,
        userId: String
    ): CryptographyKey.Raw32? {
        val userDownload = itemGroup.getUser(userId) ?: return null
        return decryptGroupKey(userDownload.groupKey, userPrivateKey)
    }

    fun getItemGroupKey(
        itemGroup: ItemGroup,
        userId: String,
        myUserGroups: List<UserGroup>,
        myCollections: List<Collection>,
    ): CryptographyKey.Raw32? {
        val myUserGroupIdInItemGroup =
            itemGroup.groups?.map { it.groupId }?.intersect(myUserGroups.map { it.groupId }.toSet()) ?: emptySet()

        val myCollectionIdsInItemGroup =
            itemGroup.collections?.map { it.uuid }?.intersect(myCollections.map { it.uuid }.toSet()) ?: emptySet()

        if (itemGroup.isUserAcceptedOrPending(userId)) {
            getItemGroupKeyFromUser(itemGroup, userId)?.also { return it }
        }
        if (myUserGroupIdInItemGroup.isNotEmpty()) {
            myUserGroups.filter { it.groupId in myUserGroupIdInItemGroup }.forEach { userGroup ->
                if (itemGroup.isUserGroupAcceptedOrPending(userGroup.groupId)) {
                    getItemGroupKeyFromUserGroup(itemGroup, userGroup, userId)?.also { return it }
                }
            }
        }
        if (myCollectionIdsInItemGroup.isNotEmpty()) {
            myCollections.filter { it.uuid in myCollectionIdsInItemGroup }.forEach { collection ->
                if (itemGroup.isCollectionAcceptedOrPending(collection.uuid)) {
                    getItemGroupKeyFromCollection(itemGroup, collection, myUserGroups, userId)?.also { return it }
                }
            }
        }
        return null
    }

    fun getUserGroupKey(userGroup: UserGroup, login: String): CryptographyKey.Raw32? {
        val userDownload = userGroup.getUser(login) ?: return null
        return decryptGroupKey(userDownload.groupKey, userPrivateKey)
    }

    fun getCollectionKeyFromUser(
        collection: Collection,
        login: String
    ): CryptographyKey.Raw32? {
        val userCollectionDownload = collection.getUser(login) ?: return null
        return decryptGroupKey(userCollectionDownload.collectionKey, userPrivateKey)
    }

    fun getCollectionKeyFromUserGroup(
        collection: Collection,
        myUserGroups: List<UserGroup>,
        login: String
    ): CryptographyKey.Raw32? {
        val userGroupCollectionDownloads = collection.userGroups ?: return null
        val ids =
            userGroupCollectionDownloads.map { it.uuid } intersect myUserGroups.map { it.groupId }
                .toSet()
        userGroupCollectionDownloads.filter { it.uuid in ids }
            .forEach { userGroupCollectionDownload ->
                val userGroup = myUserGroups.find { it.groupId == userGroupCollectionDownload.uuid }
                    ?: return@forEach
                val privateKey = getUserGroupPrivateKey(userGroup, login) ?: return@forEach
                decryptGroupKey(
                    userGroupCollectionDownload.collectionKey,
                    privateKey
                )?.also { return it }
            }
        return null
    }

    private fun getCollectionKey(
        collection: Collection,
        myUserGroups: List<UserGroup>,
        login: String
    ): CryptographyKey.Raw32? {
        return getCollectionKeyFromUser(collection, login) ?: getCollectionKeyFromUserGroup(
            collection,
            myUserGroups,
            login
        )
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