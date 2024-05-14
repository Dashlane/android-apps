package com.dashlane.nitro

import com.dashlane.cryptography.decodeBase64ToByteArrayOrNull
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper

internal data class AttestationDocument(
    val certificate: ByteArray,
    val pcrs: ObjectNode,
    val cabundle: List<ByteArray>,
    val userData: UserData
) {
    override fun toString() = "AttestationDocument(▪)"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttestationDocument

        if (!certificate.contentEquals(other.certificate)) return false
        if (cabundle != other.cabundle) return false
        if (userData != other.userData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = certificate.contentHashCode()
        result = 31 * result + cabundle.hashCode()
        result = 31 * result + userData.hashCode()
        return result
    }

    data class UserData(
        val header: ByteArray,
        val publicKey: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UserData

            if (!header.contentEquals(other.header)) return false
            if (!publicKey.contentEquals(other.publicKey)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = header.contentHashCode()
            result = 31 * result + publicKey.contentHashCode()
            return result
        }
    }

    companion object {
        fun ByteArray.decodeAttestationDocumentOrNull(): AttestationDocument? {
            val obj = CBORMapper().readTree(this) as? ObjectNode ?: return null
            val userData = (obj["user_data"] as? BinaryNode)?.binaryValue() ?: return null
            val userDataObj = JsonMapper().readTree(userData) as? ObjectNode ?: return null

            return AttestationDocument(
                certificate = (obj["certificate"] as? BinaryNode)?.binaryValue() ?: return null,
                pcrs = (obj["pcrs"] as? ObjectNode) ?: return null,
                cabundle = (obj["cabundle"] as? ArrayNode)?.filterIsInstance<BinaryNode>()
                    ?.mapNotNull { it.binaryValue() }
                    .orEmpty(),
                userData = UserData(
                    header = (userDataObj["header"] as? TextNode)?.textValue()
                        ?.decodeBase64ToByteArrayOrNull()
                        ?: return null,
                    publicKey = (userDataObj["publicKey"] as? TextNode)?.textValue()
                        ?.decodeBase64ToByteArrayOrNull()
                        ?: return null,
                )
            )
        }
    }
}