package com.dashlane.cryptography

import okio.Buffer
import okio.BufferedSource
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8
import okio.EOFException
import okio.Options

sealed class CryptographyMarker {
    abstract val value: String

    abstract val isSupported: Boolean

    abstract val keyType: CryptographyKey.Type

    abstract val saltLength: Int

    abstract val isFixedSalt: Boolean

    data class Flexible(
        val keyDerivation: KeyDerivation,
        val cipher: Cipher = Cipher.Aes256.Default.cbcHmac,
    ) : CryptographyMarker() {
        override val value
            get() = "$$version$$keyDerivationMarker$$cipherAlgorithmMarker$"

        override val isSupported: Boolean
            get() = when (cipher) {
                is Cipher.Aes256 -> when (cipher.mode) {
                    Cipher.Aes256.Mode.GCM -> false
                    Cipher.Aes256.Mode.CBC_HMAC -> true
                    Cipher.Aes256.Mode.CBC_HMAC_64 ->
                        when (keyDerivation.algorithm) {
                            KeyDerivation.Algorithm.NO_DERIVATION -> true
                            else -> false
                        }
                }
            }

        override val keyType: CryptographyKey.Type
            get() = when (keyDerivation.algorithm) {
                KeyDerivation.Algorithm.PBKDF2,
                KeyDerivation.Algorithm.ARGON2D,
                KeyDerivation.Algorithm.ARGON2I,
                ->
                    CryptographyKey.Type.PASSWORD
                KeyDerivation.Algorithm.NO_DERIVATION ->
                    when (cipher) {
                        is Cipher.Aes256 -> when (cipher.mode) {
                            Cipher.Aes256.Mode.GCM,
                            Cipher.Aes256.Mode.CBC_HMAC,
                            ->
                                CryptographyKey.Type.RAW_32
                            Cipher.Aes256.Mode.CBC_HMAC_64 ->
                                CryptographyKey.Type.RAW_64
                        }
                    }
            }

        override val saltLength: Int
            get() = keyDerivation.saltLength

        override val isFixedSalt: Boolean
            get() = saltLength != 0

        private val version
            get() = 1

        private val keyDerivationMarker
            get() = keyDerivation.marker

        private val cipherAlgorithmMarker
            get() = cipher.marker

        sealed class KeyDerivation {
            abstract val algorithm: Algorithm
            abstract val marker: String
            abstract val saltLength: Int

            data class Argon2d(
                override val saltLength: Int = 16,
                
                val timeCost: Int = 3,
                
                val memoryCost: Int = 1 shl 15,
                val parallelism: Int = 2,
            ) : KeyDerivation() {
                override val algorithm: Algorithm
                    get() = Algorithm.ARGON2D

                override val marker
                    get() = "$algorithmMarker$$saltLength$$timeCost$$memoryCost$$parallelism"

                private val algorithmMarker
                    get() = algorithm.marker
            }

            data class Argon2i(
                override val saltLength: Int = 16,
                
                val timeCost: Int = 3,
                
                val memoryCost: Int = 1 shl 15,
                val parallelism: Int = 2,
            ) : KeyDerivation() {
                override val algorithm: Algorithm
                    get() = Algorithm.ARGON2I

                override val marker
                    get() = "$algorithmMarker$$saltLength$$timeCost$$memoryCost$$parallelism"

                private val algorithmMarker
                    get() = algorithm.marker
            }

            data class Pbkdf2(
                override val saltLength: Int = 16,
                val iterationCount: Int = 200000,
                val hashMethod: HashMethod = HashMethod.SHA256,
            ) : KeyDerivation() {
                override val algorithm: Algorithm
                    get() = Algorithm.PBKDF2

                override val marker
                    get() = "$algorithmMarker$$saltLength$$iterationCount$$hashMethodMarker"

                private val algorithmMarker
                    get() = algorithm.marker

                private val hashMethodMarker
                    get() = hashMethod.marker

                enum class HashMethod(val marker: String) {
                    SHA256("sha256"),
                    SHA1("sha1"),
                    ;

                    companion object {
                        operator fun get(marker: String): HashMethod? =
                            enumValues<HashMethod>().firstOrNull { it.marker == marker }
                    }
                }
            }

            object None : KeyDerivation() {
                override val algorithm: Algorithm
                    get() = Algorithm.NO_DERIVATION
                override val marker: String
                    get() = algorithm.marker
                override val saltLength: Int
                    get() = 0
            }

            enum class Algorithm(val marker: String) {
                PBKDF2("pbkdf2"),
                ARGON2D("argon2d"),
                ARGON2I("argon2i"),
                NO_DERIVATION("noderivation"),
                ;

                companion object {
                    operator fun get(marker: String): Algorithm? =
                        enumValues<Algorithm>().firstOrNull { it.marker == marker }
                }
            }

            object Default {
                val argon2d = Argon2d()
                val argon2i = Argon2i()
                val pbkdf2 = Pbkdf2()
                val none = None
            }
        }

        sealed class Cipher {
            abstract val algorithm: Algorithm
            abstract val marker: String

            data class Aes256(
                val mode: Mode = Mode.CBC_HMAC,
                val ivLength: Int = 16,
            ) : Cipher() {
                override val marker: String
                    get() = "$algorithmMarker$$modeMarker$$ivLength"

                override val algorithm: Algorithm
                    get() = Algorithm.AES_256

                private val algorithmMarker
                    get() = algorithm.marker

                private val modeMarker: String
                    get() = mode.marker

                enum class Mode(val marker: String) {
                    GCM("gcm"),
                    CBC_HMAC("cbchmac"),
                    CBC_HMAC_64("cbchmac64"),
                    ;

                    companion object {
                        operator fun get(marker: String): Mode? =
                            enumValues<Mode>().firstOrNull { it.marker == marker }
                    }
                }

                object Default {
                    val cbcHmac = Aes256()
                    val cbcHmac64 = Aes256(Mode.CBC_HMAC_64)
                }
            }

            enum class Algorithm(val marker: String) {
                AES_256("aes256"),
                ;

                companion object {
                    operator fun get(marker: String): Algorithm? =
                        enumValues<Algorithm>().firstOrNull { it.marker == marker }
                }
            }
        }

        object Defaults {
            val argon2d = Flexible(keyDerivation = KeyDerivation.Default.argon2d)
            val argon2i = Flexible(keyDerivation = KeyDerivation.Default.argon2i)
            val pbkdf2 = Flexible(keyDerivation = KeyDerivation.Default.pbkdf2)
            val noDerivation = Flexible(keyDerivation = KeyDerivation.Default.none)
            val noDerivation64 = Flexible(
                keyDerivation = KeyDerivation.Default.none,
                cipher = Cipher.Aes256(
                    mode = Cipher.Aes256.Mode.CBC_HMAC_64,
                ),
            )
        }
    }

    object Kwc3 : CryptographyMarker() {
        val length: Long
            get() = 4L
        override val value: String
            get() = "KWC3"
        override val isSupported: Boolean
            get() = true
        override val keyType: CryptographyKey.Type
            get() = CryptographyKey.Type.PASSWORD
        override val saltLength: Int
            get() = LEGACY_MARKER_HEADER_DATA_LENGTH
        override val isFixedSalt: Boolean
            get() = false
    }

    object Kwc5 : CryptographyMarker() {
        val length: Long
            get() = 4L
        override val value: String
            get() = "KWC5"
        override val isSupported: Boolean
            get() = true
        override val keyType: CryptographyKey.Type
            get() = CryptographyKey.Type.RAW_32
        override val saltLength: Int
            get() = 0
        override val isFixedSalt: Boolean
            get() = false
    }
}

fun String.toCryptographyMarkerOrNull(): CryptographyMarker? =
    try {
        toCryptographyMarker()
    } catch (e: CryptographyMarkerException) {
        null
    }

fun String.toCryptographyMarker(): CryptographyMarker =
    when (val s = trim()) {
        CryptographyMarker.Kwc3.value -> CryptographyMarker.Kwc3
        CryptographyMarker.Kwc5.value -> CryptographyMarker.Kwc5
        else ->
            try {
                Buffer().apply { writeUtf8(s) }.selectFlexibleMarker()
            } catch (e: EOFException) {
                throw CryptographyMarkerException("Incomplete marker.", e)
            }
    }

@Throws(CryptographyMarkerException::class)
fun EncryptedByteArray.getCryptographyMarker(): CryptographyMarker {
    val buffer = Buffer()
    try {
        return buffer.write(value).selectCryptographyMarker()
    } finally {
        buffer.clear()
    }
}

@Throws(CryptographyMarkerException::class, CryptographyBase64Exception::class)
fun EncryptedBase64String.getCryptographyMarker(): CryptographyMarker {
    val byteString = value.decodeBase64() ?: throw CryptographyBase64Exception("Invalid Base64")
    val buffer = Buffer()
    try {
        return buffer.write(byteString).selectCryptographyMarker()
    } finally {
        buffer.clear()
    }
}

@Throws(CryptographyMarkerException::class)
fun BufferedSource.selectCryptographyMarker(): CryptographyMarker =
    try {
        peekLegacyMarker() ?: selectDefaultFlexibleMarker() ?: selectFlexibleMarker()
    } catch (e: EOFException) {
        throw CryptographyMarkerException("Incomplete marker.", e)
    }

private val legacyMarkerOptions = Options.of(
    CryptographyMarker.Kwc3.value.encodeUtf8(),
    CryptographyMarker.Kwc5.value.encodeUtf8(),
)

private fun BufferedSource.peekLegacyMarker() =
    when (
        peek().apply { skip(LEGACY_MARKER_HEADER_DATA_LENGTH.toLong()) }
            .select(legacyMarkerOptions)
    ) {
        0 -> CryptographyMarker.Kwc3
        1 -> CryptographyMarker.Kwc5
        else -> null
    }

private val defaultFlexibleMarkerOptions = Options.of(
    CryptographyMarker.Flexible.Defaults.argon2d.value.encodeUtf8(),
    CryptographyMarker.Flexible.Defaults.argon2i.value.encodeUtf8(),
    CryptographyMarker.Flexible.Defaults.pbkdf2.value.encodeUtf8(),
    CryptographyMarker.Flexible.Defaults.noDerivation.value.encodeUtf8(),
    CryptographyMarker.Flexible.Defaults.noDerivation64.value.encodeUtf8(),
)

private fun BufferedSource.selectDefaultFlexibleMarker() =
    when (select(defaultFlexibleMarkerOptions)) {
        0 -> CryptographyMarker.Flexible.Defaults.argon2d
        1 -> CryptographyMarker.Flexible.Defaults.argon2i
        2 -> CryptographyMarker.Flexible.Defaults.pbkdf2
        3 -> CryptographyMarker.Flexible.Defaults.noDerivation
        4 -> CryptographyMarker.Flexible.Defaults.noDerivation64
        else -> null
    }

private const val FLEXIBLE_VALUE_MAX_LENGTH = 16L

private const val FLEXIBLE_SEPARATOR = '$'.code.toByte()

private fun BufferedSource.selectFlexibleMarker(): CryptographyMarker {
    readFlexibleMarkerSeparator()

    
    val version = readFlexibleMarkerIntValue()
    if (version != 1) throw CryptographyMarkerException("Unsupported version.")

    readFlexibleMarkerSeparator()

    
    val keyDerivationAlgorithmMarker = readFlexibleMarkerStringValue()
    val keyDerivationAlgorithm =
        CryptographyMarker.Flexible.KeyDerivation.Algorithm[keyDerivationAlgorithmMarker]
            ?: throw CryptographyMarkerException("Unrecognized key derivation algorithm.")

    readFlexibleMarkerSeparator()

    
    val keyDerivation = when (keyDerivationAlgorithm) {
        CryptographyMarker.Flexible.KeyDerivation.Algorithm.PBKDF2 -> {
            val saltLength = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            val iterationCount = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            val hashMethodMarker = readFlexibleMarkerStringValue()
            val hashMethod =
                CryptographyMarker.Flexible.KeyDerivation.Pbkdf2.HashMethod[hashMethodMarker]
                    ?: throw CryptographyMarkerException("Unrecognized hash method.")
            readFlexibleMarkerSeparator()
            CryptographyMarker.Flexible.KeyDerivation.Pbkdf2(
                saltLength,
                iterationCount,
                hashMethod,
            )
        }
        CryptographyMarker.Flexible.KeyDerivation.Algorithm.ARGON2D -> {
            val saltLength = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            val timeCost = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            val memoryCost = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            val parallelism = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            CryptographyMarker.Flexible.KeyDerivation.Argon2d(
                saltLength,
                timeCost,
                memoryCost,
                parallelism,
            )
        }
        CryptographyMarker.Flexible.KeyDerivation.Algorithm.ARGON2I -> {
            val saltLength = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            val timeCost = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            val memoryCost = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            val parallelism = readFlexibleMarkerIntValue()
            readFlexibleMarkerSeparator()
            CryptographyMarker.Flexible.KeyDerivation.Argon2i(
                saltLength,
                timeCost,
                memoryCost,
                parallelism,
            )
        }
        CryptographyMarker.Flexible.KeyDerivation.Algorithm.NO_DERIVATION ->
            CryptographyMarker.Flexible.KeyDerivation.None
    }

    
    val cipherAlgorithmMarker = readFlexibleMarkerStringValue()
    val cipherAlgorithm =
        CryptographyMarker.Flexible.Cipher.Algorithm[cipherAlgorithmMarker]
            ?: throw CryptographyMarkerException("Unrecognized cipher algorithm.")

    readFlexibleMarkerSeparator()

    
    val cipher = when (cipherAlgorithm) {
        CryptographyMarker.Flexible.Cipher.Algorithm.AES_256 -> {
            val cipherModeMarker = readFlexibleMarkerStringValue()
            val cipherMode =
                CryptographyMarker.Flexible.Cipher.Aes256.Mode[cipherModeMarker]
                    ?: throw CryptographyMarkerException("Unrecognized cipher mode.")
            readFlexibleMarkerSeparator()
            val ivLength = readFlexibleMarkerIntValue()
            CryptographyMarker.Flexible.Cipher.Aes256(
                cipherMode,
                ivLength,
            )
        }
    }

    readFlexibleMarkerSeparator()

    return CryptographyMarker.Flexible(
        keyDerivation,
        cipher,
    )
}

private fun BufferedSource.readFlexibleMarkerSeparator() {
    if (readByte() != FLEXIBLE_SEPARATOR) throw CryptographyMarkerException("Expected flexible separator.")
}

private fun BufferedSource.readFlexibleMarkerIntValue(): Int {
    val s = readFlexibleMarkerStringValue()
    return try {
        s.toInt()
    } catch (e: NumberFormatException) {
        throw CryptographyMarkerException("Invalid integer value.", e)
    }
}

private fun BufferedSource.readFlexibleMarkerStringValue(): String {
    val i = indexOf(FLEXIBLE_SEPARATOR, 0, FLEXIBLE_VALUE_MAX_LENGTH)
    if (i == -1L) throw CryptographyMarkerException("Flexible separator not found.")
    return readUtf8(i)
}