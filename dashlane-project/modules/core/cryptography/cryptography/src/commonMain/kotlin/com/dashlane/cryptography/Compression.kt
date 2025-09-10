package com.dashlane.cryptography

import okio.Sink
import okio.Source

internal expect fun Source.inflate(): Source

internal expect fun Sink.deflate(level: Int = 7): Sink