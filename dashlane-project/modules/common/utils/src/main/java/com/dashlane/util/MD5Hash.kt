package com.dashlane.util

import okio.ByteString.Companion.encode

object MD5Hash {
    @JvmStatic
    fun hash(dataToHash: String?): String? {
        if (!dataToHash.isNullOrEmpty()) {
            try {
                return dataToHash.encode(Charsets.ISO_8859_1).md5().hex()
            } catch (ignored: Exception) {
                
            }
        }
        return null
    }
}