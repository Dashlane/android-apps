package com.dashlane.cryptography

import okio.Closeable

inline fun <T> ByteArray.use(block: (ByteArray) -> T): T =
    try {
        block(this)
    } finally {
        close()
    }

fun ByteArray.close() =
    fill(0)

inline fun <T> CharArray.use(block: (CharArray) -> T): T =
    try {
        block(this)
    } finally {
        close()
    }

fun CharArray.close() =
    fill('\u0000')