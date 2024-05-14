package com.dashlane.nitro.util

import okio.ByteString.Companion.toByteString

internal fun ByteArray.encodeHex() = toByteString().hex()