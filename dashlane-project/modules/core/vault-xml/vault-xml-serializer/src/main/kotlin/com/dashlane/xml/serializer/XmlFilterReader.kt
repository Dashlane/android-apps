@file:Suppress("kotlin:S6514")

package com.dashlane.xml.serializer

import java.io.Reader

internal class XmlFilterReader(private val reader: Reader) : Reader() {
    private var overflowedSurrogate: Char = NULL

    @Suppress(
        "ComplexMethod",
        "LoopWithTooManyJumpStatements",
        "MagicNumber",
        "NestedBlockDepth",
        "ReturnCount",
        "kotlin:S3776",
    ) 
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        var readCount: Int
        if (overflowedSurrogate == NULL) {
            readCount = reader.read(cbuf, off, len)
            if (readCount == -1) return -1
        } else {
            readCount = reader.read(cbuf, off + 1, len - 1)
            if (readCount == -1) return -1
            readCount++
            cbuf[off] = overflowedSurrogate
            overflowedSurrogate = NULL
        }

        var i = 0
        while (i < readCount) {
            val c = cbuf[off + i]
            val isValidXmlCharacter =
                if (c.code and 0xfff0 == 0) {
                    c == '\u0009' || c == '\u000A' || c == '\u000D'
                } else {
                    c in '\u0020'..'\uD7FF' || c in '\uE000'..'\uFFFD'
                }
            if (isValidXmlCharacter) {
                i++
                continue
            }

            if (c.isSurrogate()) {
                if (i + 1 < readCount) {
                    val cc = cbuf[off + i + 1]
                    val surrogatePair = c.code shl 16 or cc.code
                    val isValidSurrogatePair = surrogatePair in MIN_SURROGATE_PAIR..MAX_SURROGATE_PAIR
                    if (isValidSurrogatePair) {
                        i += 2
                    } else {
                        cbuf.shl(i + 1, off + readCount)
                        readCount -= 1
                    }
                    continue
                } else {
                    overflowedSurrogate = c
                    readCount--
                    break
                }
            } else {
                cbuf.shl(i + 1, off + readCount)
                readCount--
            }
        }

        return readCount
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun CharArray.shl(start: Int, end: Int) {
        copyInto(
            this,
            destinationOffset = start - 1,
            startIndex = start,
            endIndex = end,
        )
    }

    override fun close() {
        reader.close()
    }

    companion object {
        private const val MIN_SURROGATE_PAIR = '\uD800'.code shl 16 or '\udc00'.code
        private const val MAX_SURROGATE_PAIR = '\udbff'.code shl 16 or '\udfff'.code

        private const val NULL = '\u0000'
    }
}