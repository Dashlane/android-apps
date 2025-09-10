package com.dashlane.cryptography

import okio.Buffer
import okio.ByteString
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CryptographyMarkerTest {
    @Test
    fun `empty marker`() {
        assertFailsWith<CryptographyMarkerException> { "".toCryptographyMarker() }
    }

    @Test
    fun `empty marker or null`() {
        assertNull("".toCryptographyMarkerOrNull())
    }

    @Test
    fun `incomplete marker`() {
        assertFailsWith<CryptographyMarkerException> { "$".toCryptographyMarker() }
    }

    @Test
    fun `incomplete marker or null`() {
        assertNull("$".toCryptographyMarkerOrNull())
    }

    @Test
    fun `invalid marker`() {
        assertFailsWith<CryptographyMarkerException> {
            "R2MR4w6JoLrCxYZgrhv5Gu17JTLR3prYZEyuLUW854FVOrcZ5K7DuzX4hhHF6Bsl".toCryptographyMarker()
        }
    }

    @Test
    fun `invalid marker or null`() {
        assertNull(
            "R2MR4w6JoLrCxYZgrhv5Gu17JTLR3prYZEyuLUW854FVOrcZ5K7DuzX4hhHF6Bsl".toCryptographyMarkerOrNull(),
        )
    }

    @Test
    fun kwc3() {
        assertEquals(CryptographyMarker.Kwc3, "KWC3".toCryptographyMarker())
    }

    @Test
    fun kwc5() {
        assertEquals(CryptographyMarker.Kwc5, "KWC5".toCryptographyMarker())
    }

    @Test
    fun flexible_argon2d() {
        assertEquals(
            CryptographyMarker.Flexible(
                CryptographyMarker.Flexible.KeyDerivation.Argon2d(
                    saltLength = 16,
                    timeCost = 3,
                    memoryCost = 65536,
                    parallelism = 2,
                ),
                cipher = CryptographyMarker.Flexible.Cipher.Aes256(
                    mode = CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC,
                    ivLength = 16,
                ),
            ),
            "\$1\$argon2d\$16\$3\$65536\$2\$aes256\$cbchmac\$16\$".toCryptographyMarker(),
        )
    }

    @Test
    fun flexible_argon2d_2() {
        assertEquals(
            CryptographyMarker.Flexible(
                CryptographyMarker.Flexible.KeyDerivation.Argon2d(
                    saltLength = 16,
                    timeCost = 3,
                    memoryCost = 32768,
                    parallelism = 2,
                ),
                cipher = CryptographyMarker.Flexible.Cipher.Aes256(
                    mode = CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC,
                    ivLength = 16,
                ),
            ),
            "\$1\$argon2d\$16\$3\$32768\$2\$aes256\$cbchmac\$16\$".toCryptographyMarker(),
        )
    }

    @Test
    fun flexible_argon2i() {
        assertEquals(
            CryptographyMarker.Flexible(
                CryptographyMarker.Flexible.KeyDerivation.Argon2i(
                    saltLength = 16,
                    timeCost = 3,
                    memoryCost = 65536,
                    parallelism = 2,
                ),
                cipher = CryptographyMarker.Flexible.Cipher.Aes256(
                    mode = CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC,
                    ivLength = 16,
                ),
            ),
            "\$1\$argon2i\$16\$3\$65536\$2\$aes256\$cbchmac\$16\$".toCryptographyMarker(),
        )
    }

    @Test
    fun flexible_pbkdf2() {
        assertEquals(
            CryptographyMarker.Flexible(
                keyDerivation = CryptographyMarker.Flexible.KeyDerivation.Pbkdf2(
                    saltLength = 16,
                    iterationCount = 200000,
                    hashMethod = CryptographyMarker.Flexible.KeyDerivation.Pbkdf2.HashMethod.SHA256,
                ),
                cipher = CryptographyMarker.Flexible.Cipher.Aes256(
                    mode = CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC,
                    ivLength = 16,
                ),
            ),
            "\$1\$pbkdf2\$16\$200000\$sha256\$aes256\$cbchmac\$16\$".toCryptographyMarker(),
        )
    }

    @Test
    fun flexible_noderivation() {
        assertEquals(
            CryptographyMarker.Flexible(
                keyDerivation = CryptographyMarker.Flexible.KeyDerivation.None,
                cipher = CryptographyMarker.Flexible.Cipher.Aes256(
                    mode = CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC,
                    ivLength = 16,
                ),
            ),
            "\$1\$noderivation\$aes256\$cbchmac\$16\$".toCryptographyMarker(),
        )
    }

    @Test
    fun flexible_noderivation64() {
        assertEquals(
            CryptographyMarker.Flexible(
                keyDerivation = CryptographyMarker.Flexible.KeyDerivation.None,
                cipher = CryptographyMarker.Flexible.Cipher.Aes256(
                    mode = CryptographyMarker.Flexible.Cipher.Aes256.Mode.CBC_HMAC_64,
                    ivLength = 16,
                ),
            ),
            "\$1\$noderivation\$aes256\$cbchmac64\$16\$".toCryptographyMarker(),
        )
    }

    @Test
    fun `get marker empty encrypted byte array`() {
        val encryptedData = byteArrayOf().asEncryptedByteArray()

        assertFailsWith<CryptographyMarkerException> {
            encryptedData.getCryptographyMarker()
        }
    }

    @Test
    fun `get marker empty encrypted base 64`() {
        val encryptedDataBase64 = "".asEncryptedBase64()

        assertFailsWith<CryptographyMarkerException> {
            encryptedDataBase64.getCryptographyMarker()
        }
    }

    @Test
    fun `get marker incomplete encrypted byte array`() {
        val encryptedData = "$".encodeToByteArray().asEncryptedByteArray()

        assertFailsWith<CryptographyMarkerException> {
            encryptedData.getCryptographyMarker()
        }
    }

    @Test
    fun `get marker incomplete encrypted base 64`() {
        val encryptedDataBase64 = "JA==".asEncryptedBase64()

        assertFailsWith<CryptographyMarkerException> {
            encryptedDataBase64.getCryptographyMarker()
        }
    }

    @Test
    fun `get marker invalid encrypted byte array`() {
        val encryptedData =
            "R2MR4w6JoLrCxYZgrhv5Gu17JTLR3prYZEyuLUW854FVOrcZ5K7DuzX4hhHF6Bsl".encodeToByteArray()
                .asEncryptedByteArray()

        assertFailsWith<CryptographyMarkerException> {
            encryptedData.getCryptographyMarker()
        }
    }

    @Test
    fun `get marker invalid encrypted base64`() {
        val encryptedDataBase64 =
            "UjJNUjR3NkpvTHJDeFlaZ3JodjVHdTE3SlRMUjNwcllaRXl1TFVXODU0RlZPcmNaNUs3RHV6WDRoaEhGNkJzbA==".asEncryptedBase64()

        assertFailsWith<CryptographyMarkerException> {
            encryptedDataBase64.getCryptographyMarker()
        }
    }

    @Test
    fun `get marker invalid base64`() {
        val encryptedDataBase64 = "q5oRFK@s232ANICg2".asEncryptedBase64()

        assertFailsWith<CryptographyBase64Exception> {
            encryptedDataBase64.getCryptographyMarker()
        }
    }

    @Test
    fun `select incomplete`() {
        val buffer = Buffer().writeUtf8("$")

        assertFailsWith<CryptographyMarkerException> {
            buffer.selectCryptographyMarker()
        }
    }

    @Test
    fun `select invalid`() {
        val buffer =
            Buffer().writeUtf8("R2MR4w6JoLrCxYZgrhv5Gu17JTLR3prYZEyuLUW854FVOrcZ5K7DuzX4hhHF6Bsl")

        assertFailsWith<CryptographyMarkerException> {
            buffer.selectCryptographyMarker()
        }
    }

    @Test
    fun `select kwc5`() {
        val random = Random(7129L)
        val data = random.nextBytes(32) + "KWC5".encodeToByteArray() + random.nextBytes(64)
        val buffer = Buffer().apply { write(data) }

        val marker = buffer.selectCryptographyMarker()

        assertEquals(CryptographyMarker.Kwc5, marker)
        assertEquals(
            data.encodeBase64ToString(),
            buffer.readByteString().base64(),
        ) 
    }

    @Test
    fun `select flexible default argon2d`() {
        val random = Random(7639L)
        val payloadData = random.nextBytes(128)
        val data =
            "\$1\$argon2d\$16\$3\$32768\$2\$aes256\$cbchmac\$16\$".encodeToByteArray() + payloadData
        val buffer = Buffer().apply { write(data) }

        val marker = buffer.selectCryptographyMarker()

        assertEquals(CryptographyMarker.Flexible.Defaults.argon2d, marker)
        assertEquals(
            payloadData.encodeBase64ToString(),
            buffer.readByteString().base64(),
        ) 
    }

    @Test
    fun `select flexible default argon2i`() {
        val random = Random(55110L)
        val payloadData = random.nextBytes(128)
        val data =
            "\$1\$argon2i\$16\$3\$32768\$2\$aes256\$cbchmac\$16\$".encodeToByteArray() + payloadData
        val buffer = Buffer().apply { write(data) }

        val marker = buffer.selectCryptographyMarker()

        assertEquals(CryptographyMarker.Flexible.Defaults.argon2i, marker)
        assertEquals(
            payloadData.encodeBase64ToString(),
            buffer.readByteString().base64(),
        ) 
    }

    @Test
    fun `select flexible default pbkdf2`() {
        val random = Random(55110L)
        val payloadData = random.nextBytes(128)
        val data =
            "\$1\$pbkdf2\$16\$200000\$sha256\$aes256\$cbchmac\$16\$".encodeToByteArray() + payloadData
        val buffer = Buffer().apply { write(data) }

        val marker = buffer.selectCryptographyMarker()

        assertEquals(CryptographyMarker.Flexible.Defaults.pbkdf2, marker)
        assertEquals(
            payloadData.encodeBase64ToString(),
            buffer.readByteString().base64(),
        ) 
    }

    @Test
    fun `select flexible default no derivation`() {
        val random = Random(55110L)
        val payloadData = random.nextBytes(128)
        val data = "\$1\$noderivation\$aes256\$cbchmac\$16\$".encodeToByteArray() + payloadData
        val buffer = Buffer().apply { write(data) }

        val marker = buffer.selectCryptographyMarker()

        assertEquals(CryptographyMarker.Flexible.Defaults.noDerivation, marker)
        assertEquals(
            payloadData.encodeBase64ToString(),
            buffer.readByteString().base64(),
        ) 
    }

    @Test
    fun `select flexible default no derivation 64`() {
        val random = Random(55110L)
        val payloadData = random.nextBytes(128)
        val data = "\$1\$noderivation\$aes256\$cbchmac64\$16\$".encodeToByteArray() + payloadData
        val buffer = Buffer().apply { write(data) }

        val marker = buffer.selectCryptographyMarker()

        assertEquals(CryptographyMarker.Flexible.Defaults.noDerivation64, marker)
        assertEquals(
            payloadData.encodeBase64ToString(),
            buffer.readByteString().base64(),
        ) 
    }
}

private fun ByteArray.encodeBase64ToString(): String =
    ByteString.of(*this).base64()