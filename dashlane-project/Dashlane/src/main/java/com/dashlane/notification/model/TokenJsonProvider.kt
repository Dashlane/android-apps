package com.dashlane.notification.model

import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class TokenJsonProvider @Inject constructor() {
    private var _json: ObfuscatedByteArray? = null

    var json: String?
        get() = _json?.decodeUtf8ToString()
        set(value) {
            _json?.close()
            _json = value?.encodeUtf8ToObfuscated()
        }
}
