package com.dashlane.nitro

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper



internal data class CoseSign1(
    val protectedHeader: ByteArray,
    val payload: ByteArray,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoseSign1

        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        return payload.contentHashCode()
    }

    companion object {
        fun ByteArray.decodeCoseSign1OrNull(): CoseSign1? {
            val array = CBORMapper().readTree(this) as? ArrayNode ?: return null

            if (array.size() != 4) return null

            return CoseSign1(
                protectedHeader = (array[0] as? BinaryNode)?.binaryValue() ?: return null,
                payload = (array[2] as? BinaryNode)?.binaryValue() ?: return null,
                signature = (array[3] as? BinaryNode)?.binaryValue() ?: return null
            )
        }
    }
}