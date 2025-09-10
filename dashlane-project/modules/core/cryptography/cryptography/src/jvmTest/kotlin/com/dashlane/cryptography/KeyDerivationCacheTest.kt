package com.dashlane.cryptography

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KeyDerivationCacheTest {
    @Test
    fun `test argon2d cached result`() {
        val key = "X17CXP8k+leZrZdv0FrXTUzfTOqiGVyDsf7bLS1PUFK+jbf0".decodeBase64ToByteArray()
        val salt = "ad1yV7G6YEvldcuHtH13mNqWf/OAv/46RPTVPD9ZCPE87WvG".decodeBase64ToByteArray()
        val timeCost = 3
        val memoryCost = 1 shl 15
        val parallelism = 2
        val derivedKeyBase64 = "M4+TEr3f7Tr0+KPuJrORC2tTiWllPrGqZSLbKSEQRwU="
        val keyDerivation = mockk<KeyDerivationEngine> {
            every {
                deriveKeyArgon2d(
                    key,
                    salt,
                    timeCost,
                    memoryCost,
                    parallelism,
                )
            } returns derivedKeyBase64.decodeBase64ToByteArray()
        }

        val cache = keyDerivation.cache()
        val derivedKey1 = cache.deriveKeyArgon2d(key, salt, timeCost, memoryCost, parallelism)
        val derivedKey2 = cache.deriveKeyArgon2d(key, salt, timeCost, memoryCost, parallelism)

        assertEquals(derivedKeyBase64, derivedKey1.encodeBase64ToString())
        assertEquals(derivedKeyBase64, derivedKey2.encodeBase64ToString())
        verify(exactly = 1) { keyDerivation.deriveKeyArgon2d(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `test argon2d non cached result`() {
        val key1 = "ZEAWTu+A9HNgOJOm3l1vGj7gDRJbh0RzQjsJe5eAKQbAdDuL".decodeBase64ToByteArray()
        val salt1 = "UwKvb26pmMyfo1fjwXRhUp83kFvFBz6iHRNWv4vp0Lv70x6K".decodeBase64ToByteArray()
        val timeCost1 = 3
        val memoryCost1 = 1 shl 15
        val parallelism1 = 2
        val derivedKey1Base64 = "AeqW+Y5coBaoox/Xd1PVGe3rbbzjYErYcj+1ulIkhYY="
        val key2 = "z+nCgi3wHUFh9YFDX6c2xWjlKYV/rXYahYI11MTBV5Y11+TK".decodeBase64ToByteArray()
        val salt2 = "6siw6DkBzjy
        val timeCost2 = 3
        val memoryCost2 = 1 shl 15
        val parallelism2 = 2
        val derivedKey2Base64 = "gXRJXaZIDF8J+kScL1DBgsk7JyBI9peIA80fBAzXC4c="
        val keyDerivation = mockk<KeyDerivationEngine> {
            every {
                deriveKeyArgon2d(
                    key1,
                    salt1,
                    timeCost1,
                    memoryCost1,
                    parallelism1,
                )
            } returns derivedKey1Base64.decodeBase64ToByteArray()
            every {
                deriveKeyArgon2d(
                    key2,
                    salt2,
                    timeCost2,
                    memoryCost2,
                    parallelism2,
                )
            } returns derivedKey2Base64.decodeBase64ToByteArray()
        }

        val cache = keyDerivation.cache()
        val derivedKey1 = cache.deriveKeyArgon2d(key1, salt1, timeCost1, memoryCost1, parallelism1)
        val derivedKey2 = cache.deriveKeyArgon2d(key2, salt2, timeCost2, memoryCost2, parallelism2)

        assertEquals(derivedKey1Base64, derivedKey1.encodeBase64ToString())
        assertEquals(derivedKey2Base64, derivedKey2.encodeBase64ToString())
        verify(exactly = 2) { keyDerivation.deriveKeyArgon2d(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `test argon2d clear cache`() {
        val key = "c0FNmlPrKdg8d533npBE5lAE9bUH7/ybV73C0fFE5q0VG1MV".decodeBase64ToByteArray()
        val salt = "XkI9HF/lN61uNtQtOXJIuYr3XQWbQfEDzZRS2Q5rEl1Bva+V".decodeBase64ToByteArray()
        val timeCost = 3
        val memoryCost = 1 shl 15
        val parallelism = 2
        val derivedKeyBase64 = "jOUQixnzwjP7NHvt8wLHsIqBun+52RaR89ZXTL/flOA="
        val keyDerivation = mockk<KeyDerivationEngine> {
            every {
                deriveKeyArgon2d(
                    key,
                    salt,
                    timeCost,
                    memoryCost,
                    parallelism,
                )
            } returns derivedKeyBase64.decodeBase64ToByteArray()
        }

        val cache = keyDerivation.cache()
        val derivedKey1 = cache.deriveKeyArgon2d(key, salt, timeCost, memoryCost, parallelism)

        assertEquals(derivedKeyBase64, derivedKey1.encodeBase64ToString())
        verify(exactly = 1) { keyDerivation.deriveKeyArgon2d(any(), any(), any(), any(), any(), any()) }

        cache.close()

        
        every {
            keyDerivation.deriveKeyArgon2d(
                key,
                salt,
                timeCost,
                memoryCost,
                parallelism,
            )
        } returns derivedKeyBase64.decodeBase64ToByteArray()

        val derivedKey2 = cache.deriveKeyArgon2d(key, salt, timeCost, memoryCost, parallelism)

        assertEquals(derivedKeyBase64, derivedKey2.encodeBase64ToString())
        verify(exactly = 2) { keyDerivation.deriveKeyArgon2d(any(), any(), any(), any(), any(), any()) }
    }
}