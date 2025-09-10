package com.dashlane.cryptography

import okio.Sink
import okio.Source

expect class Cipher

internal expect fun Source.cipherSource(cipher: Cipher): Source

internal expect fun Sink.cipherSink(cipher: Cipher): Sink