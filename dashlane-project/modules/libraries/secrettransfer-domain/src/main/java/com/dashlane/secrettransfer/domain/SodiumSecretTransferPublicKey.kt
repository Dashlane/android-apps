package com.dashlane.secrettransfer.domain

import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.encodeBase64ToString

data class SodiumSecretTransferPublicKey(
    val publicKey: String
) {

    val info = publicKey.decodeBase64ToByteArray().copyOfRange(0, 12)
    val raw = publicKey.decodeBase64ToByteArray().run { copyOfRange(12, size) }.encodeBase64ToString()

    fun toPeerPublicKey(responsePublicKey: String) = info.plus(responsePublicKey.decodeBase64ToByteArray()).encodeBase64ToString()
}
