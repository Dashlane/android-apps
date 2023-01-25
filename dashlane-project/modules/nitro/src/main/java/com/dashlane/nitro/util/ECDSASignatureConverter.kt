package com.dashlane.nitro.util



object ECDSASignatureConverter {

    private val DER_SEQUENCE_TAG = byteArrayOf(0x30)
    private const val DER_INTEGER_TAG: Byte = 0x02

    @Throws(CoseException::class)
    fun convertConcatToDerFormat(concat: ByteArray): ByteArray {
        val len = concat.size / 2
        val r = concat.copyOfRange(0, len)
        val s = concat.copyOfRange(len, concat.size)

        return encodeSignature(r, s)
    }

    @Throws(CoseException::class)
    private fun encodeSignature(r: ByteArray, s: ByteArray): ByteArray {
        return arrayListOf(
            r.toUnsignedInteger(),
            s.toUnsignedInteger()
        ).toDerSequence()
    }

    @Throws(CoseException::class)
    private fun ArrayList<ByteArray>.toDerSequence(): ByteArray {
        val y = this.toBytes()
        return arrayListOf(DER_SEQUENCE_TAG, computeLength(y.size), y).toBytes()
    }

    @Throws(CoseException::class)
    private fun ByteArray.toUnsignedInteger(): ByteArray {
        var pad = 0
        var offset = 0
        loop@ for (i in this) {
            if (i.toInt() == 0) {
                offset++
            } else {
                break@loop
            }
        }
        if (offset == size) {
            return byteArrayOf(DER_INTEGER_TAG, 0x01, 0x00)
        }
        if (this[offset].toInt() and 0x80 != 0) {
            pad++
        }

        
        val length = size - offset
        val der = ByteArray(2 + length + pad)
        der[0] = DER_INTEGER_TAG
        der[1] = (length + pad).toByte()
        System.arraycopy(this, offset, der, 2 + pad, length)
        return der
    }

    private fun ArrayList<ByteArray>.toBytes(): ByteArray {
        var l = 0
        l = stream().map { r: ByteArray -> r.size }.reduce(
            l
        ) { a: Int, b: Int -> Integer.sum(a, b) }
        val b = ByteArray(l)
        l = 0
        for (r in this) {
            System.arraycopy(r, 0, b, l, r.size)
            l += r.size
        }
        return b
    }

    @Throws(CoseException::class)
    private fun computeLength(x: Int): ByteArray {
        if (x <= 127) {
            return byteArrayOf(x.toByte())
        } else if (x < 256) {
            return byteArrayOf(0x81.toByte(), x.toByte())
        }
        throw CoseException("Error in ASN1.GetLength")
    }

    class CoseException(message: String?) : Exception(message)
}