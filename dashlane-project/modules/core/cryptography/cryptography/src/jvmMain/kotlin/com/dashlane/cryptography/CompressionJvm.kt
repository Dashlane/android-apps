package com.dashlane.cryptography

import okio.Buffer
import okio.DeflaterSink
import okio.IOException
import okio.InflaterSource
import okio.Sink
import okio.Source
import okio.Timeout
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

internal actual fun Source.inflate(): Source = InflaterSource(this, Inflater()).adaptExceptions()

internal actual fun Sink.deflate(level: Int): Sink = DeflaterSink(this, Deflater(level))

private fun InflaterSource.adaptExceptions(): Source =
    CryptographyCompressionExceptionAdapterSource(this)

private class CryptographyCompressionExceptionAdapterSource(private val source: InflaterSource) : Source {
    override fun read(sink: Buffer, byteCount: Long): Long =
        try {
            source.read(sink, byteCount)
        } catch (e: IOException) {
            throw when (e.cause) {
                is DataFormatException -> CryptographyCompressionException("Unable to decompress contents.", e)
                else -> e
            }
        }

    override fun timeout(): Timeout =
        source.timeout()

    override fun close() =
        source.close()
}