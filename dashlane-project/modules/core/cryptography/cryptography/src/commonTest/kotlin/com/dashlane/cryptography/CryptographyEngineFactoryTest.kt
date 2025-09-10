package com.dashlane.cryptography

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class CryptographyEngineFactoryTest {
    private val random = Random(4512271)
    private val decryptionEngine = mockk<DecryptionEngine>()
    private val encryptionEngine = mockk<EncryptionEngine>()
    private val cryptography = mockk<Cryptography> {
        every { createDecryptionEngine(any()) } returns decryptionEngine
        every { createKwc3EncryptionEngine(any()) } returns encryptionEngine
        every { createKwc5EncryptionEngine(any()) } returns encryptionEngine
        every { createFlexibleArgon2dEncryptionEngine(any(), any(), any()) } returns encryptionEngine
        every { createFlexibleArgon2iEncryptionEngine(any(), any(), any()) } returns encryptionEngine
        every { createFlexiblePbkdf2EncryptionEngine(any(), any(), any()) } returns encryptionEngine
        every { createFlexibleNoDerivationEncryptionEngine(any()) } returns encryptionEngine
        every { createFlexibleNoDerivation64EncryptionEngine(any()) } returns encryptionEngine
    }

    @Test
    fun createDecryptionEngineCryptographyKeyOfPassword() {
        val key = CryptographyKey.ofPassword("SB4zsDd0WRi6RPfHCosd0av9BRtWz1rp")
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                throw UnsupportedOperationException()

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt {
                throw UnsupportedOperationException()
            }
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val decryptionEngine = engineFactory.createDecryptionEngine()

        assertEquals(this.decryptionEngine, decryptionEngine)
        verify { cryptography.createDecryptionEngine(key) }
    }

    @Test
    fun createDecryptionEngineCryptographyKeyOfBytes32() {
        val key = CryptographyKey.ofBytes32(random.nextBytes(32))
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                throw UnsupportedOperationException()

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt {
                throw UnsupportedOperationException()
            }
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val decryptionEngine = engineFactory.createDecryptionEngine()

        assertEquals(this.decryptionEngine, decryptionEngine)
        verify { cryptography.createDecryptionEngine(key) }
    }

    @Test
    fun createDecryptionEngineCryptographyKeyOfBytes64() {
        val key = CryptographyKey.ofBytes64(random.nextBytes(64))
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                throw UnsupportedOperationException()

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt {
                throw UnsupportedOperationException()
            }
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val decryptionEngine = engineFactory.createDecryptionEngine()

        assertEquals(this.decryptionEngine, decryptionEngine)
        verify { cryptography.createDecryptionEngine(key) }
    }

    @Test
    fun createKwc3EncryptionEngine() {
        val key = CryptographyKey.ofPassword("BN0Ppz81NzYFzZTt0OvPMHAQYs6OP7jJ")
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                CryptographyMarker.Kwc3

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt {
                throw UnsupportedOperationException()
            }
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val encryptionEngine = engineFactory.createEncryptionEngine()

        assertEquals(this.encryptionEngine, encryptionEngine)
        verify { cryptography.createKwc3EncryptionEngine(key) }
    }

    @Test
    fun createKwc5EncryptionEngine() {
        val key = CryptographyKey.ofBytes32(random.nextBytes(32))
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                CryptographyMarker.Kwc5

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt {
                throw UnsupportedOperationException()
            }
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val encryptionEngine = engineFactory.createEncryptionEngine()

        assertEquals(this.encryptionEngine, encryptionEngine)
        verify { cryptography.createKwc5EncryptionEngine(key) }
    }

    @Test
    fun createFlexibleArgon2dEncryptionEngine() {
        val key = CryptographyKey.ofPassword("Fwoa9lKkkfFw4rhEaMfPhBNIyKrP4jfc")
        val fixedSalt = CryptographyFixedSalt(random.nextBytes(16))
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                CryptographyMarker.Flexible.Defaults.argon2d

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt =
                fixedSalt
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val encryptionEngine = engineFactory.createEncryptionEngine()

        assertEquals(this.encryptionEngine, encryptionEngine)
        verify { cryptography.createFlexibleArgon2dEncryptionEngine(key, fixedSalt) }
    }

    @Test
    fun createFlexibleArgon2iEncryptionEngine() {
        val key = CryptographyKey.ofPassword("g0G77XQEIzFtAKYoG8b2S833FUBdQB4E")
        val fixedSalt = CryptographyFixedSalt(random.nextBytes(16))
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                CryptographyMarker.Flexible.Defaults.argon2i

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt =
                fixedSalt
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val encryptionEngine = engineFactory.createEncryptionEngine()

        assertEquals(this.encryptionEngine, encryptionEngine)
        verify { cryptography.createFlexibleArgon2iEncryptionEngine(key, fixedSalt) }
    }

    @Test
    fun createFlexiblePbkdf2EncryptionEngine() {
        val key = CryptographyKey.ofPassword("urI6xHc8nuN5NiSF9FC59MKNOs8xUtPC")
        val fixedSalt = CryptographyFixedSalt(random.nextBytes(16))
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                CryptographyMarker.Flexible.Defaults.argon2i

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt =
                fixedSalt
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val encryptionEngine = engineFactory.createEncryptionEngine()

        assertEquals(this.encryptionEngine, encryptionEngine)
        verify { cryptography.createFlexibleArgon2iEncryptionEngine(key, fixedSalt) }
    }

    @Test
    fun createFlexibleNoDerivationEncryptionEngine() {
        val key = CryptographyKey.ofBytes32(random.nextBytes(32))
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                CryptographyMarker.Flexible.Defaults.noDerivation

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt {
                throw UnsupportedOperationException()
            }
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val encryptionEngine = engineFactory.createEncryptionEngine()

        assertEquals(this.encryptionEngine, encryptionEngine)
        verify { cryptography.createFlexibleNoDerivationEncryptionEngine(key) }
    }

    @Test
    fun createFlexibleNoDerivation64EncryptionEngine() {
        val key = CryptographyKey.ofBytes64(random.nextBytes(64))
        val settings = object : CryptographySettings {
            override fun getOrCreateMarker(keyType: CryptographyKey.Type) =
                CryptographyMarker.Flexible.Defaults.noDerivation64

            override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt {
                throw UnsupportedOperationException()
            }
        }

        val engineFactory = CryptographyEngineFactory(cryptography, key, settings)
        val encryptionEngine = engineFactory.createEncryptionEngine()

        assertEquals(this.encryptionEngine, encryptionEngine)
        verify { cryptography.createFlexibleNoDerivation64EncryptionEngine(key) }
    }
}