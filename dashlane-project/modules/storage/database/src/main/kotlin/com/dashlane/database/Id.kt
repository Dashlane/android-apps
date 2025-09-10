package com.dashlane.database

import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

@JvmInline
value class Id constructor(val value: ByteString) {
    val fileName: String
        get() = value.base64Url()

    val id: String
        get() = value.utf8()

    companion object {
        fun of(id: String): Id = Id(id.encodeUtf8())
    }
}