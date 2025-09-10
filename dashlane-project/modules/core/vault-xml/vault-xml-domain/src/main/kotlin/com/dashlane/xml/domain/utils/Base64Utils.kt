package com.dashlane.xml.domain.utils

import okio.ByteString
import okio.ByteString.Companion.decodeBase64

internal fun String.decodeBase64(): ByteArray? =
    decodeBase64()?.toByteArray()

@Suppress("SpreadOperator")
internal fun ByteArray.encodeBase64(): String =
    ByteString.of(*this).base64()