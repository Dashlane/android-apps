package com.dashlane.core

import android.annotation.SuppressLint
import okio.ByteString.Companion.encodeUtf8

object HashForLog {
    @JvmStatic
    @SuppressLint("WeakHashAlgorithm")
    fun getHashForLog(part1: String, part2: String): String =
        "$part1-$part2".encodeUtf8().md5().hex()
}
