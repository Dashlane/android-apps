package com.dashlane.cryptography

interface SharingCryptography {
    @Throws(CryptographyException::class)
    fun encryptRsa(
        data: ByteArray,
        publicKey: SharingKeys.Public,
    ): SharingEncryptedByteArray

    @Throws(CryptographyException::class)
    fun decryptRsa(
        data: SharingEncryptedByteArray,
        privateKey: SharingKeys.Private,
    ): ByteArray

    @Throws(CryptographyException::class)
    fun decryptRsaPkcs1OaepPadding(
        data: SharingEncryptedByteArray,
        privateKey: SharingKeys.Private,
    ): ByteArray

    @Throws(CryptographyException::class)
    fun signRsa(
        data: ByteArray,
        privateKey: SharingKeys.Private,
    ): SharingSignatureByteArray

    @Throws(CryptographyException::class)
    fun verifySignatureRsa(
        data: ByteArray,
        signature: SharingSignatureByteArray,
        publicKey: SharingKeys.Public,
    ): Boolean

    @Throws(CryptographyException::class)
    fun generateSharingKeys(): SharingKeys
}

fun SharingCryptography.encryptRsaOrNull(
    data: ByteArray,
    publicKey: SharingKeys.Public,
): SharingEncryptedByteArray? =
    try {
        encryptRsa(data, publicKey)
    } catch (e: CryptographyException) {
        null
    }

fun SharingCryptography.encryptRsaToBase64StringOrNull(
    data: ByteArray,
    publicKey: SharingKeys.Public,
): SharingEncryptedBase64String? =
    try {
        encryptRsaToBase64String(data, publicKey)
    } catch (e: CryptographyException) {
        null
    }

@Throws(CryptographyException::class)
fun SharingCryptography.encryptRsaToBase64String(
    data: ByteArray,
    publicKey: SharingKeys.Public,
): SharingEncryptedBase64String =
    encryptRsa(data, publicKey).value.encodeBase64ToString().asSharingEncryptedBase64()

fun SharingCryptography.decryptRsaOrNull(
    data: SharingEncryptedByteArray,
    privateKey: SharingKeys.Private,
): ByteArray? =
    try {
        decryptRsa(data, privateKey)
    } catch (e: CryptographyException) {
        null
    }

fun SharingCryptography.decryptRsaBase64OrNull(
    dataBase64: SharingEncryptedBase64String,
    privateKey: SharingKeys.Private,
): ByteArray? =
    try {
        decryptRsaBase64(dataBase64, privateKey)
    } catch (e: CryptographyException) {
        null
    }

@Throws(CryptographyException::class)
fun SharingCryptography.decryptRsaBase64(
    dataBase64: SharingEncryptedBase64String,
    privateKey: SharingKeys.Private,
): ByteArray =
    decryptRsa(dataBase64.decodeOrThrow(), privateKey)

fun SharingCryptography.decryptRsaPkcs1OaepPaddingOrNull(
    data: SharingEncryptedByteArray,
    privateKey: SharingKeys.Private,
): ByteArray? =
    try {
        decryptRsaPkcs1OaepPadding(data, privateKey)
    } catch (e: CryptographyException) {
        null
    }

fun SharingCryptography.decryptRsaPkcs1OaepPaddingBase64OrNull(
    dataBase64: SharingEncryptedBase64String,
    privateKey: SharingKeys.Private,
): ByteArray? =
    try {
        decryptRsaPkcs1OaepPaddingBase64(dataBase64, privateKey)
    } catch (e: CryptographyException) {
        null
    }

@Throws(CryptographyException::class)
fun SharingCryptography.decryptRsaPkcs1OaepPaddingBase64(
    dataBase64: SharingEncryptedBase64String,
    privateKey: SharingKeys.Private,
): ByteArray {
    return decryptRsaPkcs1OaepPadding(dataBase64.decodeOrThrow(), privateKey)
}

fun SharingCryptography.signRsaOrNull(
    data: ByteArray,
    privateKey: SharingKeys.Private,
): SharingSignatureByteArray? =
    try {
        signRsa(data, privateKey)
    } catch (e: CryptographyException) {
        null
    }

fun SharingCryptography.signRsaToBase64StringOrNull(
    data: ByteArray,
    privateKey: SharingKeys.Private,
): SharingSignatureBase64String? =
    try {
        signRsaToBase64String(data, privateKey)
    } catch (e: CryptographyException) {
        null
    }

@Throws(CryptographyException::class)
fun SharingCryptography.signRsaToBase64String(
    data: ByteArray,
    privateKey: SharingKeys.Private,
): SharingSignatureBase64String =
    signRsa(data, privateKey).value.encodeBase64ToString().asSharingSignatureBase64()

fun SharingCryptography.verifySignatureRsaOrNull(
    data: ByteArray,
    signature: SharingSignatureByteArray,
    publicKey: SharingKeys.Public,
): Boolean? =
    try {
        verifySignatureRsa(data, signature, publicKey)
    } catch (e: CryptographyException) {
        null
    }

fun SharingCryptography.verifySignatureRsaOrDefault(
    data: ByteArray,
    encryptedData: SharingSignatureByteArray,
    publicKey: SharingKeys.Public,
    default: Boolean = false,
): Boolean =
    try {
        verifySignatureRsa(data, encryptedData, publicKey)
    } catch (e: CryptographyException) {
        default
    }

fun SharingCryptography.verifySignatureRsaBase64OrNull(
    data: ByteArray,
    signature: SharingSignatureBase64String,
    publicKey: SharingKeys.Public,
): Boolean? =
    try {
        verifySignatureRsaBase64(data, signature, publicKey)
    } catch (e: CryptographyException) {
        null
    }

fun SharingCryptography.verifySignatureRsaBase64OrDefault(
    data: ByteArray,
    signature: SharingSignatureBase64String,
    publicKey: SharingKeys.Public,
    default: Boolean = false,
): Boolean =
    try {
        verifySignatureRsaBase64(data, signature, publicKey)
    } catch (e: CryptographyException) {
        default
    }

@Throws(CryptographyException::class)
fun SharingCryptography.verifySignatureRsaBase64(
    data: ByteArray,
    signature: SharingSignatureBase64String,
    publicKey: SharingKeys.Public,
): Boolean =
    verifySignatureRsa(data, signature.decodeOrThrow(), publicKey)

@Throws(CryptographyException::class)
fun SharingCryptography.generateSharingKeysOrNull(): SharingKeys? =
    try {
        generateSharingKeys()
    } catch (e: CryptographyException) {
        null
    }

private fun SharingEncryptedBase64String.decodeOrThrow(): SharingEncryptedByteArray =
    value.decodeBase64ToByteArrayOrThrow().asSharingEncryptedByteArray()

private fun SharingSignatureBase64String.decodeOrThrow(): SharingSignatureByteArray =
    value.decodeBase64ToByteArrayOrThrow().asSharingSignatureByteArray()

private fun String.decodeBase64ToByteArrayOrThrow(): ByteArray =
    decodeBase64ToByteArray()