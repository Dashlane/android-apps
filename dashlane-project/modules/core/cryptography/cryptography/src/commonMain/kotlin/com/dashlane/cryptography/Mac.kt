package com.dashlane.cryptography

import okio.HashingSink
import okio.HashingSource
import okio.Sink
import okio.Source

expect class Mac {
    fun update(byteArray: ByteArray)

    fun doFinal(): ByteArray
}

internal expect fun Source.hashingSource(mac: Mac): HashingSource

internal expect fun Sink.hashingSink(mac: Mac): HashingSink