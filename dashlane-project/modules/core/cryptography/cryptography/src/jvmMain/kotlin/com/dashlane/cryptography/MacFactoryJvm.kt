package com.dashlane.cryptography

import javax.crypto.spec.SecretKeySpec

@Suppress("FunctionName")
fun MacFactory(): MacFactory =
    MacFactoryImpl()

private class MacFactoryImpl : MacFactory {
    override fun createHmacSha256(key: ByteArray): Mac =
        Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec(key, "HmacSHA256"))
        }
}