package com.dashlane.secrettransfer.domain

import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.encodeBase64ToString

data class SecretTransferPublicKey(
    val publicKey: String
) {
    private val delimiter = "\n"
    private val split: List<String> = listOf()
        get() = field.ifEmpty { publicKey.split(delimiter) }

    val info = split[1].decodeBase64ToByteArray().copyOfRange(0, 12)
    val raw = split[1].decodeBase64ToByteArray().run { copyOfRange(12, size) }.encodeBase64ToString()

    fun toPeerPublicKey(responsePublicKey: String) = listOf(
        split[0],
        info.plus(responsePublicKey.decodeBase64ToByteArray()).encodeBase64ToString(),
        split[2],
    ).joinToString(delimiter)
}
