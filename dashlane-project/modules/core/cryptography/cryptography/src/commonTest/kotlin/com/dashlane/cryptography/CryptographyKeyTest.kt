package com.dashlane.cryptography

import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CryptographyKeyTest {
    private val random = Random(428897)

    @Test
    fun key_password() {
        val password = "idv2uefJD1gEhn1W3A3vFUuVpCX2j62"

        val key = CryptographyKey.ofPassword(password)

        assertEquals(CryptographyKey.Type.PASSWORD, key.type)
        assertEquals(password, key.toByteArray().decodeToString())
        assertEquals(password, key.toObfuscatedByteArray().decodeUtf8ToString())
    }

    @Test
    fun key_password_clone() {
        val password = "6zk34jqhOnEO8sjFGBAAh7v1DsdK0RfnVoHbFsQ6PWL2ATrB5epcgdHquT2SzeZD"

        val key = CryptographyKey.ofPassword(password)
        val clone = key.clone()
        assertEquals(key, clone)
        key.close()

        assertEquals(CryptographyKey.Type.PASSWORD, clone.type)
        assertEquals(password, clone.toByteArray().decodeToString())
        assertEquals(password, clone.toObfuscatedByteArray().decodeUtf8ToString())
        assertFailsWith<IllegalStateException> { key.toByteArray() }
        assertFailsWith<IllegalStateException> { key.toObfuscatedByteArray() }
    }

    @Test
    fun key_password_utf8() {
        val password = "idv2uefJD1gEhn1W3A3vFUuVpCX2j62"
        val bytes = password.encodeUtf8().toByteArray()

        val key = CryptographyKey.ofPasswordUtf8Bytes(bytes)

        assertEquals(password, bytes.decodeToString()) 
        assertEquals(CryptographyKey.Type.PASSWORD, key.type)
        assertEquals(password, key.toByteArray().decodeToString())

        
        assertEquals(key, CryptographyKey.ofBytes(password.encodeToByteArray(), CryptographyKey.Type.PASSWORD))
        assertEquals(key, CryptographyKey.ofBytes(password.encodeUtf8ToObfuscated(), CryptographyKey.Type.PASSWORD))
    }

    @Test
    fun key_password_utf8_clone() {
        val password = "cep02ai74SuO4wOg7Rb68lzYh"
        val bytes = password.encodeUtf8().toByteArray()

        val key = CryptographyKey.ofPasswordUtf8Bytes(bytes)
        val clone = key.clone()
        assertEquals(key, clone)
        key.close()

        assertEquals(password, bytes.decodeToString()) 
        assertEquals(CryptographyKey.Type.PASSWORD, clone.type)
        assertEquals(password, clone.toByteArray().decodeToString())
        assertFailsWith<IllegalStateException> { key.toByteArray() }
        assertFailsWith<IllegalStateException> { key.toObfuscatedByteArray() }
    }

    @Test
    fun key_32() {
        val bytes = random.nextBytes(32)
        val bytesCopy = bytes.copyOf()

        val key = CryptographyKey.ofBytes32(bytes)

        assertEquals(bytesCopy.toByteString(), bytes.toByteString()) 
        assertEquals(CryptographyKey.Type.RAW_32, key.type)
        assertEquals(bytes.toByteString(), key.toByteArray().toByteString())

        
        assertEquals(key, CryptographyKey.ofBytes(bytes, CryptographyKey.Type.RAW_32))
        assertEquals(key, CryptographyKey.ofBytes(bytes.toObfuscated(), CryptographyKey.Type.RAW_32))
    }

    @Test
    fun key_32_16() {
        val bytes = random.nextBytes(16)
        val bytesCopy = bytes.copyOf()

        val key = CryptographyKey.ofBytes32(bytes)

        assertEquals(bytesCopy.toByteString(), bytes.toByteString()) 
        assertEquals(CryptographyKey.Type.RAW_32, key.type)
        assertEquals(bytes.toByteString(), key.toByteArray().toByteString())

        
        assertEquals(key, CryptographyKey.ofBytes(bytes, CryptographyKey.Type.RAW_32))
        assertEquals(key, CryptographyKey.ofBytes(bytes.toObfuscated(), CryptographyKey.Type.RAW_32))
    }

    @Test
    fun key_32_64() {
        val bytes = random.nextBytes(16)
        val bytesCopy = bytes.copyOf()

        val key = CryptographyKey.ofBytes32(bytes)

        assertEquals(bytesCopy.toByteString(), bytes.toByteString()) 
        assertEquals(CryptographyKey.Type.RAW_32, key.type)
        assertEquals(bytes.toByteString(), key.toByteArray().toByteString())

        
        assertEquals(key, CryptographyKey.ofBytes(bytes, CryptographyKey.Type.RAW_32))
        assertEquals(key, CryptographyKey.ofBytes(bytes.toObfuscated(), CryptographyKey.Type.RAW_32))
    }

    @Test
    fun key_32_clone() {
        val bytes = random.nextBytes(32)
        val bytesCopy = bytes.copyOf()

        val key = CryptographyKey.ofBytes32(bytes)
        val clone = key.clone()
        assertEquals(key, clone)
        key.close()

        assertEquals(bytesCopy.toByteString(), bytes.toByteString()) 
        assertEquals(CryptographyKey.Type.RAW_32, clone.type)
        assertEquals(bytes.toByteString(), clone.toByteArray().toByteString())
    }

    @Test
    fun key_32_16_clone() {
        val bytes = random.nextBytes(16)
        val bytesCopy = bytes.copyOf()

        val key = CryptographyKey.ofBytes32(bytes)
        val clone = key.clone()
        assertEquals(key, clone)
        key.close()

        assertEquals(bytesCopy.toByteString(), bytes.toByteString()) 
        assertEquals(CryptographyKey.Type.RAW_32, clone.type)
        assertEquals(bytes.toByteString(), clone.toByteArray().toByteString())
    }

    @Test
    fun key_32_64_clone() {
        val bytes = random.nextBytes(64)
        val bytesCopy = bytes.copyOf()

        val key = CryptographyKey.ofBytes32(bytes)
        val clone = key.clone()
        assertEquals(key, clone)
        key.close()

        assertEquals(bytesCopy.toByteString(), bytes.toByteString()) 
        assertEquals(CryptographyKey.Type.RAW_32, clone.type)
        assertEquals(bytes.toByteString(), clone.toByteArray().toByteString())
    }

    @Test
    fun key_64() {
        val bytes = random.nextBytes(64)
        val bytesCopy = bytes.copyOf()

        val key = CryptographyKey.ofBytes64(bytes)

        assertEquals(bytesCopy.toByteString(), bytes.toByteString()) 
        assertEquals(CryptographyKey.Type.RAW_64, key.type)
        assertEquals(bytes.toByteString(), key.toByteArray().toByteString())

        
        assertEquals(key, CryptographyKey.ofBytes(bytes, CryptographyKey.Type.RAW_64))
        assertEquals(key, CryptographyKey.ofBytes(bytes.toObfuscated(), CryptographyKey.Type.RAW_64))
    }

    @Test
    fun key_64_clone() {
        val bytes = random.nextBytes(64)
        val bytesCopy = bytes.copyOf()

        val key = CryptographyKey.ofBytes64(bytes)
        val clone = key.clone()
        assertEquals(key, clone)
        key.close()

        assertEquals(bytesCopy.toByteString(), bytes.toByteString()) 
        assertEquals(CryptographyKey.Type.RAW_64, clone.type)
        assertEquals(bytes.toByteString(), clone.toByteArray().toByteString())
    }

    @Test
    fun invalid_key_32() {
        val bytes = random.nextBytes(12)
        assertFailsWith<IllegalArgumentException> {
            CryptographyKey.ofBytes32(bytes)
        }
        assertFailsWith<IllegalArgumentException> {
            CryptographyKey.ofBytes(bytes, CryptographyKey.Type.RAW_32)
        }
        assertFailsWith<IllegalArgumentException> {
            CryptographyKey.ofBytes(bytes.toObfuscated(), CryptographyKey.Type.RAW_32)
        }
    }

    @Test
    fun invalid_key_64() {
        val bytes = random.nextBytes(32)
        assertFailsWith<IllegalArgumentException> {
            CryptographyKey.ofBytes64(bytes)
        }
        assertFailsWith<IllegalArgumentException> {
            CryptographyKey.ofBytes(bytes, CryptographyKey.Type.RAW_64)
        }
        assertFailsWith<IllegalArgumentException> {
            CryptographyKey.ofBytes(bytes.toObfuscated(), CryptographyKey.Type.RAW_64)
        }
    }
}