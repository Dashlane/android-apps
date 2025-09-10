package com.dashlane.cryptography

interface KeyDerivationEngine {
    fun deriveKeyArgon2d(
        key: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int = 32,
    ): ByteArray

    fun deriveKeyArgon2i(
        key: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        derivedKeySizeInByte: Int = 32,
    ): ByteArray

    fun deriveKeyPbkdf2Sha256(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int = 32,
    ): ByteArray

    fun deriveKeyPbkdf2Sha1(
        key: ByteArray,
        salt: ByteArray,
        iterationCount: Int,
        derivedKeySizeInByte: Int = 32,
    ): ByteArray

    fun mutateKey(
        key: ByteArray,
    ): ByteArray
}

fun KeyDerivationEngine.deriveKeyKwc3(
    key: ByteArray,
    salt: ByteArray,
    mutate: Boolean,
): ByteArray =
    deriveKeyPbkdf2Sha1(
        key = if (mutate) mutateKey(key) else key,
        salt = salt,
        iterationCount = 10204,
    )

fun KeyDerivationEngine.deriveKeyArgon2d(
    key: ByteArray,
    keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Argon2d,
    salt: ByteArray,
) = deriveKeyArgon2d(
    key = key,
    salt = salt,
    timeCost = keyDerivation.timeCost,
    memoryCost = keyDerivation.memoryCost,
    parallelism = keyDerivation.parallelism,
)

fun KeyDerivationEngine.deriveKeyArgon2i(
    key: ByteArray,
    keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Argon2i,
    salt: ByteArray,
): ByteArray =
    deriveKeyArgon2i(
        key = key,
        salt = salt,
        timeCost = keyDerivation.timeCost,
        memoryCost = keyDerivation.memoryCost,
        parallelism = keyDerivation.parallelism,
    )

fun KeyDerivationEngine.deriveKeyPbkdf2(
    key: ByteArray,
    keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Pbkdf2,
    salt: ByteArray,
): ByteArray =
    when (keyDerivation.hashMethod) {
        CryptographyMarker.Flexible.KeyDerivation.Pbkdf2.HashMethod.SHA1 ->
            deriveKeyPbkdf2Sha1(
                key = key,
                salt = salt,
                iterationCount = keyDerivation.iterationCount,
            )
        CryptographyMarker.Flexible.KeyDerivation.Pbkdf2.HashMethod.SHA256 ->
            deriveKeyPbkdf2Sha256(
                key = key,
                salt = salt,
                iterationCount = keyDerivation.iterationCount,
            )
    }