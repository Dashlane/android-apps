package com.dashlane.util

import okio.ByteString.Companion.encodeUtf8

fun String.sha256(): ByteArray = this.encodeUtf8().sha256().toByteArray()
