package com.dashlane.cryptography

interface Cryptography {
    fun createDecryptionEngine(key: CryptographyKey): DecryptionEngine

    fun createKwc3EncryptionEngine(
        key: CryptographyKey.Password,
    ): EncryptionEngine

    fun createKwc5EncryptionEngine(
        key: CryptographyKey.Raw32,
    ): EncryptionEngine

    @Throws(CryptographyInvalidSaltLengthException::class)
    fun createFlexibleArgon2dEncryptionEngine(
        key: CryptographyKey.Password,
        salt: CryptographyFixedSalt,
        keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Argon2d = CryptographyMarker.Flexible.KeyDerivation.Default.argon2d,
    ): EncryptionEngine

    @Throws(CryptographyInvalidSaltLengthException::class)
    fun createFlexibleArgon2iEncryptionEngine(
        key: CryptographyKey.Password,
        salt: CryptographyFixedSalt,
        keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Argon2i = CryptographyMarker.Flexible.KeyDerivation.Default.argon2i,
    ): EncryptionEngine

    @Throws(CryptographyInvalidSaltLengthException::class)
    fun createFlexiblePbkdf2EncryptionEngine(
        key: CryptographyKey.Password,
        salt: CryptographyFixedSalt,
        keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Pbkdf2 = CryptographyMarker.Flexible.KeyDerivation.Default.pbkdf2,
    ): EncryptionEngine

    fun createFlexibleNoDerivationEncryptionEngine(
        key: CryptographyKey.Raw32,
    ): EncryptionEngine

    fun createFlexibleNoDerivation64EncryptionEngine(
        key: CryptographyKey.Raw64,
    ): EncryptionEngine
}

@Throws(CryptographyFixedSaltRequiredException::class, CryptographyUnsupportedException::class)
fun Cryptography.createEncryptionEngine(
    marker: CryptographyMarker,
    key: CryptographyKey,
    salt: CryptographyFixedSalt? = null,
): EncryptionEngine = when (marker) {
    is CryptographyMarker.Kwc3 ->
        createKwc3EncryptionEngine(key.requireInstanceOf())
    is CryptographyMarker.Kwc5 ->
        createKwc5EncryptionEngine(key.requireInstanceOf())
    is CryptographyMarker.Flexible ->
        when (val cipher = marker.cipher) {
            is CryptographyMarker.Flexible.Cipher.Aes256 -> {
                when (cipher.mode) {
                    CryptographyMarker.Flexible.Cipher.Aes256.Mode.GCM ->
                        throw CryptographyUnsupportedException("Unsupported cipher mode GCM $marker.")
                    CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC ->
                        when (val keyDerivation = marker.keyDerivation) {
                            is CryptographyMarker.Flexible.KeyDerivation.Argon2d ->
                                createFlexibleArgon2dEncryptionEngine(key.requireInstanceOf(), salt.requireNotNull(), keyDerivation)
                            is CryptographyMarker.Flexible.KeyDerivation.Argon2i ->
                                createFlexibleArgon2iEncryptionEngine(key.requireInstanceOf(), salt.requireNotNull(), keyDerivation)
                            is CryptographyMarker.Flexible.KeyDerivation.Pbkdf2 ->
                                createFlexiblePbkdf2EncryptionEngine(key.requireInstanceOf(), salt.requireNotNull(), keyDerivation)
                            CryptographyMarker.Flexible.KeyDerivation.None ->
                                createFlexibleNoDerivationEncryptionEngine(key.requireInstanceOf())
                        }
                    CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC_64 -> {
                        when (marker.keyDerivation) {
                            CryptographyMarker.Flexible.KeyDerivation.None ->
                                createFlexibleNoDerivation64EncryptionEngine(key.requireInstanceOf())
                            else ->
                                throw CryptographyUnsupportedException("Unsupported key derivation with CBC-HMAC-64 cipher mode $marker.")
                        }
                    }
                }
            }
        }
}

@Suppress("FunctionName")
fun Cryptography(
    cipherFactory: CipherFactory,
    macFactory: MacFactory,
    keyDerivationEngine: KeyDerivationEngine,
    saltGenerator: SaltGenerator,
    ivGenerator: IvGenerator,
    paddingGenerator: PaddingGenerator,
): Cryptography = CryptographyImpl(
    cipherFactory,
    macFactory,
    keyDerivationEngine,
    saltGenerator,
    ivGenerator,
    paddingGenerator,
)

private class CryptographyImpl(
    private val cipherFactory: CipherFactory,
    private val macFactory: MacFactory,
    private val keyDerivationEngine: KeyDerivationEngine,
    private val saltGenerator: SaltGenerator,
    private val ivGenerator: IvGenerator,
    private val paddingGenerator: PaddingGenerator,
) : Cryptography {
    private val decryptionEngineFactory
        get() = DecryptionEngineImpl.Factory(
            cipherFactory,
            macFactory,
            keyDerivationEngine,
        )

    private val kwc3encryptionEngineFactory
        get() = EncryptionEngineKwc3Impl.Factory(
            cipherFactory,
            keyDerivationEngine,
            saltGenerator,
        )

    private val kwc5encryptionEngineFactory
        get() = EncryptionEngineKwc5Impl.Factory(
            cipherFactory,
            macFactory,
            ivGenerator,
            paddingGenerator,
        )

    private val flexibleEncryptionEngineFactory
        get() = EncryptionEngineFlexibleAesCbcHmacImpl.Factory(
            cipherFactory,
            macFactory,
            keyDerivationEngine,
            ivGenerator,
        )

    override fun createDecryptionEngine(key: CryptographyKey): DecryptionEngine =
        decryptionEngineFactory.create(key)

    override fun createKwc3EncryptionEngine(key: CryptographyKey.Password): EncryptionEngine =
        kwc3encryptionEngineFactory.create(key)

    override fun createKwc5EncryptionEngine(key: CryptographyKey.Raw32): EncryptionEngine =
        kwc5encryptionEngineFactory.create(key)

    override fun createFlexibleArgon2dEncryptionEngine(
        key: CryptographyKey.Password,
        salt: CryptographyFixedSalt,
        keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Argon2d,
    ): EncryptionEngine =
        flexibleEncryptionEngineFactory.createArgon2d(key, salt, keyDerivation)

    override fun createFlexibleArgon2iEncryptionEngine(
        key: CryptographyKey.Password,
        salt: CryptographyFixedSalt,
        keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Argon2i,
    ): EncryptionEngine =
        flexibleEncryptionEngineFactory.createArgon2i(key, salt, keyDerivation)

    override fun createFlexiblePbkdf2EncryptionEngine(
        key: CryptographyKey.Password,
        salt: CryptographyFixedSalt,
        keyDerivation: CryptographyMarker.Flexible.KeyDerivation.Pbkdf2,
    ): EncryptionEngine =
        flexibleEncryptionEngineFactory.createPbkdf2(key, salt, keyDerivation)

    override fun createFlexibleNoDerivationEncryptionEngine(key: CryptographyKey.Raw32): EncryptionEngine =
        flexibleEncryptionEngineFactory.createNoDerivation(key)

    override fun createFlexibleNoDerivation64EncryptionEngine(key: CryptographyKey.Raw64): EncryptionEngine =
        flexibleEncryptionEngineFactory.createNoDerivation64(key)
}

@Throws(CryptographyIllegalKeyException::class)
inline fun <reified T : CryptographyKey> CryptographyKey.requireInstanceOf() =
    if (this is T) this else throw CryptographyIllegalKeyException("Expected key of type ${T::class.simpleName}, but was $this.")

@Throws(CryptographyIllegalKeyException::class)
fun CryptographyKey.Type.requireEquals(type: CryptographyKey.Type) {
    if (this != type) throw CryptographyIllegalKeyException("Expected key type $type, but was $this.")
}