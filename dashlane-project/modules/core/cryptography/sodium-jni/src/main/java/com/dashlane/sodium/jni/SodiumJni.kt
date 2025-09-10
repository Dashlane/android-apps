@file:Suppress(
    "kotlin:S1128",
    "kotlin:S100",
)

package com.dashlane.sodium.jni

import com.dashlane.sodium.jni.SodiumJni.crypto_kx_keypair
import com.dashlane.sodium.jni.SodiumJni.crypto_secretbox_easy
import com.dashlane.sodium.jni.SodiumJni.randombytes_buf

@Suppress(
    "FunctionNaming",
    "ktlint:standard:function-naming",
)
object SodiumJni {
    init {
        System.loadLibrary("sodium")
    }

    external fun crypto_kx_keypair(
        publicKey: ByteArray,
        secretKey: ByteArray,
    ): Boolean

    external fun crypto_kx_client_session_keys(
        rx: ByteArray,
        tx: ByteArray,
        publicKey: ByteArray,
        secretKey: ByteArray,
        peerPublicKey: ByteArray,
    ): Boolean

    external fun crypto_kx_server_session_keys(
        rx: ByteArray,
        tx: ByteArray,
        publicKey: ByteArray,
        secretKey: ByteArray,
        clientPublicKey: ByteArray,
    ): Boolean

    external fun crypto_secretstream_xchacha20poly1305_init_push(
        state: ByteArray,
        header: ByteArray,
        key: ByteArray,
    ): Boolean

    external fun crypto_secretstream_xchacha20poly1305_push(
        state: ByteArray,
        encrypted: ByteArray,
        message: ByteArray,
    ): Boolean

    external fun crypto_secretstream_xchacha20poly1305_init_pull(
        state: ByteArray,
        header: ByteArray,
        key: ByteArray,
    ): Boolean

    external fun crypto_secretstream_xchacha20poly1305_pull(
        state: ByteArray,
        message: ByteArray,
        encrypted: ByteArray,
    ): Boolean

    external fun crypto_secretbox_easy(
        message: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
    ): ByteArray?

    external fun crypto_secretbox_open_easy(
        cipherText: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
    ): ByteArray?

    external fun randombytes_buf(
        size: Int,
    ): ByteArray

    external fun crypto_generichash(
        outlen: Int,
        input: ByteArray,
        inlen: Long,
        key: ByteArray,
        keylen: Int,
    ): ByteArray

    external fun crypto_generichash(
        outlen: Int,
        input: ByteArray,
        inlen: Long,
    ): ByteArray

    external fun sodium_memcmp(
        key1: ByteArray,
        key2: ByteArray,
    ): Boolean
}