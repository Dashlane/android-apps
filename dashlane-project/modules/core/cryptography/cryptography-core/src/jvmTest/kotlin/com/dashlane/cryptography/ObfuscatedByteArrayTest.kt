package cryptography

import com.dashlane.cryptography.clone
import com.dashlane.cryptography.isEmpty
import com.dashlane.cryptography.toObfuscated
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ObfuscatedByteArrayTest {
    @Test
    fun `size indices get and toByteArray`() {
        val random = Random(1548)
        val size = 16
        val bytes = random.nextBytes(size)

        val obfuscatedByteArray = bytes.toObfuscated()

        assertFalse(obfuscatedByteArray.isEmpty)
        assertEquals(size, obfuscatedByteArray.size)
        assertEquals(0 until size, obfuscatedByteArray.indices)
        for (index in 0 until size) {
            assertEquals(bytes[index], obfuscatedByteArray[index])
        }
        assertEquals(bytes.toByteString(), obfuscatedByteArray.toByteArray().toByteString())
    }

    @Test
    fun `size indices and toByteArray for empty`() {
        val size = 0
        val bytes = byteArrayOf()

        val obfuscatedByteArray = bytes.toObfuscated()

        assertTrue(obfuscatedByteArray.isEmpty)
        assertEquals(size, obfuscatedByteArray.size)
        assertEquals(IntRange.EMPTY, obfuscatedByteArray.indices)
        assertEquals(bytes.toByteString(), obfuscatedByteArray.toByteArray().toByteString())
    }

    @Test
    fun `size indices get and toByteArray for clone`() {
        val random = Random(1981066)
        val size = 24
        val bytes = random.nextBytes(size)

        val obfuscatedByteArray = bytes.toObfuscated()
        val copy = obfuscatedByteArray.clone()

        assertEquals(size, copy.size)
        assertEquals(0 until size, copy.indices)
        for (index in 0 until size) {
            assertEquals(bytes[index], copy[index])
        }
        assertEquals(bytes.toByteString(), copy.toByteArray().toByteString())
    }

    @Test
    fun `size indices get and toByteArray when closed`() {
        val random = Random(800344)
        val size = 16
        val bytes = random.nextBytes(size)

        val obfuscatedByteArray = bytes.toObfuscated()
        obfuscatedByteArray.close()

        assertFailsWith<IllegalStateException> { obfuscatedByteArray.size }
        assertFailsWith<IllegalStateException> { obfuscatedByteArray.indices }
        for (index in 0 until size) {
            assertFailsWith<IllegalStateException> { obfuscatedByteArray[index] }
        }
        assertFailsWith<IllegalStateException> { obfuscatedByteArray[Random(800344).nextInt(0, size)] }
        assertFailsWith<IllegalStateException> { obfuscatedByteArray.toByteArray() }
    }

    @Test
    fun `size indices get and toByteArray for clone when closed`() {
        val random = Random(800344)
        val size = 32
        val bytes = random.nextBytes(size)

        val obfuscatedByteArray = bytes.toObfuscated()
        val copy = obfuscatedByteArray.clone()
        obfuscatedByteArray.close()

        assertEquals(size, copy.size)
        assertEquals(0 until size, copy.indices)
        for (index in 0 until size) {
            assertEquals(bytes[index], copy[index])
        }
        assertEquals(bytes.toByteString(), copy.toByteArray().toByteString())
    }

    @Test
    fun `toByteArray with range`() {
        val random = Random(6996360)
        val size = 18
        val bytes = random.nextBytes(size)
        val offset = 3
        val byteCount = 14

        val obfuscatedByteArray = bytes.toObfuscated()
        val range = obfuscatedByteArray.toByteArray(offset, byteCount)

        assertEquals(bytes.toByteString(offset, byteCount), range.toByteString())
    }

    @Test
    fun `equals equal`() {
        val random = Random(907053)
        val bytes = random.nextBytes(16)

        val obfuscatedByteArray1 = bytes.toObfuscated()
        val obfuscatedByteArray2 = bytes.toObfuscated()

        assertEquals(obfuscatedByteArray1, obfuscatedByteArray2)
    }

    @Test
    fun `equals not equal`() {
        val random = Random(32098)
        val bytes1 = random.nextBytes(18)
        val bytes2 = random.nextBytes(18)

        val obfuscatedByteArray1 = bytes1.toObfuscated()
        val obfuscatedByteArray2 = bytes2.toObfuscated()

        assertNotEquals(obfuscatedByteArray1, obfuscatedByteArray2)
    }

    @Test
    fun `equals size not equal`() {
        val random = Random(516610)
        val bytes1 = random.nextBytes(18)
        val bytes2 = bytes1 + random.nextBytes(8)

        val obfuscatedByteArray1 = bytes1.toObfuscated()
        val obfuscatedByteArray2 = bytes2.toObfuscated()

        assertNotEquals(obfuscatedByteArray1, obfuscatedByteArray2)
    }

    @Test
    fun `hashCode equal`() {
        val random = Random(1981066)
        val bytes = random.nextBytes(122)

        val obfuscatedByteArray1 = bytes.toObfuscated()
        val obfuscatedByteArray2 = bytes.toObfuscated()

        assertEquals(obfuscatedByteArray1.hashCode(), obfuscatedByteArray2.hashCode())
    }

    @Test
    fun `hashCode not equal`() {
        val random = Random(766)
        val bytes1 = random.nextBytes(8)
        val bytes2 = random.nextBytes(8)

        val obfuscatedByteArray1 = bytes1.toObfuscated()
        val obfuscatedByteArray2 = bytes2.toObfuscated()

        assertNotEquals(obfuscatedByteArray1.hashCode(), obfuscatedByteArray2.hashCode())
    }

    @Test
    fun `hashCode size not equal`() {
        val random = Random(6609)
        val bytes1 = random.nextBytes(8)
        val bytes2 = bytes1 + random.nextBytes(8)

        val obfuscatedByteArray1 = bytes1.toObfuscated()
        val obfuscatedByteArray2 = bytes2.toObfuscated()

        assertNotEquals(obfuscatedByteArray1.hashCode(), obfuscatedByteArray2.hashCode())
    }
}