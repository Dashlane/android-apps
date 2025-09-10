@file:Suppress("kotlin:S1128")

package com.dashlane.cryptography

import okio.HashingSink
import okio.HashingSource
import okio.Sink
import okio.Source
import okio.hashingSink
import okio.hashingSource
import javax.crypto.Mac

actual typealias Mac = Mac

internal actual fun Source.hashingSource(mac: Mac): HashingSource =
    hashingSource(mac)

internal actual fun Sink.hashingSink(mac: Mac): HashingSink =
    hashingSink(mac)