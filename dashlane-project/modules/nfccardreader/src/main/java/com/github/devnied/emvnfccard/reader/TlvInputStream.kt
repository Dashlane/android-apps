package com.github.devnied.emvnfccard.reader

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayDeque
import java.util.Deque

class TlvInputStream(inputStream: InputStream) : InputStream() {
    private val inputStream = if (inputStream is DataInputStream) inputStream else DataInputStream(inputStream)
    private var bufferSize = 0
    private var state = TlvInputState()
    private var markedState: TlvInputState? = null

    init {
        try {
            if (inputStream is BufferedInputStream || inputStream is ByteArrayInputStream) {
                bufferSize = inputStream.available()
            }
        } catch (ioe: IOException) {
            
        }
    }

    @Throws(IOException::class)
    fun readTag(): Int {
        check(!(!state.isAtStartOfTag && !state.isProcessingValue)) { "Not at start of tag" }
        var tag: Int
        var bytesRead = 0
        return try {
            var b = inputStream.readUnsignedByte()
            bytesRead++
            while (b == 0x00 || b == 0xFF) {
                b = inputStream.readUnsignedByte()
                bytesRead++
            }
            when (b and 0x1F) {
                0x1F -> {
                    tag = b
                    b = inputStream.readUnsignedByte()
                    bytesRead++
                    while (b and 0x80 == 0x80) {
                        tag = tag shl 8
                        tag = tag or (b and 0x7F)
                        b = inputStream.readUnsignedByte()
                        bytesRead++
                    }
                    tag = tag shl 8
                    tag = tag or (b and 0x7F)
                }
                else -> tag = b
            }
            state.setTagProcessed(tag, bytesRead)
            tag
        } catch (e: IOException) {
            throw e
        }
    }

    @Throws(IOException::class)
    fun readLength() = try {
        check(state.isAtStartOfLength) { "Not at start of length" }
        var bytesRead = 0
        var length: Int
        var b = inputStream.readUnsignedByte()
        bytesRead++
            length = b
            val count = b and 0x7F
            length = 0
            (0 until count).forEach { _ ->
                b = inputStream.readUnsignedByte()
                bytesRead++
                length = length shl 8
                length = length or b
            }
        }
        state.setLengthProcessed(length, bytesRead)
        length
    } catch (e: IOException) {
        throw e
    }

    @Throws(IOException::class)
    fun readValue() = try {
        check(state.isProcessingValue) { "Not yet processing value!" }
        val length = state.length
        val value = ByteArray(length)
        inputStream.readFully(value)
        state.updateValueBytesProcessed(length)
        value
    } catch (e: IOException) {
        throw e
    }

    @Throws(IOException::class)
    override fun available() = inputStream.available()

    @Throws(IOException::class)
    override fun read(): Int {
        val result = inputStream.read()
        if (result < 0) {
            return -1
        }
        state.updateValueBytesProcessed(1)
        return result
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        if (n <= 0) {
            return 0
        }
        val result = inputStream.skip(n)
        state.updateValueBytesProcessed(result.toInt())
        return result
    }

    @Synchronized
    override fun mark(readLimit: Int) {
        inputStream.mark(readLimit)
        markedState = TlvInputState(state)
    }

    override fun markSupported() = inputStream.markSupported()

    @Synchronized
    @Throws(IOException::class)
    override fun reset() {
        if (!markSupported()) {
            throw IOException("mark/reset not supported")
        }
        inputStream.reset()
        state = markedState ?: TlvInputState()
        markedState = null
    }

    @Throws(IOException::class)
    override fun close() = inputStream.close()

    override fun toString() = state.toString()

    private inner class TlvInputState private constructor(
        private val state: Deque<TlStruct>,
        var isAtStartOfTag: Boolean,
        var isAtStartOfLength: Boolean,
        var isProcessingValue: Boolean
    ) {

        constructor() : this(ArrayDeque<TlStruct>(), true, false, false)
        constructor(original: TlvInputState) : this(
            original.deepCopyOfState,
            original.isAtStartOfTag,
            original.isAtStartOfLength,
            original.isProcessingValue
        )

        val tag: Int
            get() {
                check(!state.isEmpty()) { "Tag not yet read." }
                return state.peek()!!.tag
            }

        val length: Int
            get() {
                check(!state.isEmpty()) { "Length not yet known." }
                return state.peek()!!.length
            }

        fun setTagProcessed(
            tag: Int,
            byteCount: Int
            val obj = TlStruct(tag)
            if (!state.isEmpty()) {
                val parent = state.peek()!!
                parent.updateValueBytesProcessed(byteCount)
            }
            state.push(obj)
            isAtStartOfTag = false
            isAtStartOfLength = true
            isProcessingValue = false
        }

        fun setLengthProcessed(length: Int, byteCount: Int) {
            require(length >= 0) {
                "Cannot set negative length (length = $length, 0x" + Integer.toHexString(
                    length
                ) + " for tag " + Integer.toHexString(tag) + ")."
            }
            val obj = state.pop()
            if (!state.isEmpty()) {
                val parent = state.peek()!!
                parent.updateValueBytesProcessed(byteCount)
            }
            obj.length = length
            state.push(obj)
            isAtStartOfTag = false
            isAtStartOfLength = false
            isProcessingValue = true
        }

        fun updateValueBytesProcessed(byteCount: Int) {
            if (state.isEmpty()) {
                return
            }
            val currentObject = state.peek()!!
            val bytesLeft = currentObject.length - currentObject.valueBytesProcessed
            require(byteCount <= bytesLeft) { "Cannot process $byteCount bytes! Only $bytesLeft bytes left in this TLV object $currentObject" }
            currentObject.updateValueBytesProcessed(byteCount)
            val currentLength = currentObject.length
            if (currentObject.valueBytesProcessed == currentLength) {
                state.pop()
                updateValueBytesProcessed(currentLength)
                isAtStartOfTag = true
                isAtStartOfLength = false
                isProcessingValue = false
            } else {
                isAtStartOfTag = false
                isAtStartOfLength = false
                isProcessingValue = true
            }
        }

        override fun toString() = state.toString()

        private val deepCopyOfState: Deque<TlStruct>
            get() {
                val newStack: Deque<TlStruct> = ArrayDeque(state.size)
                for (tlStruct in state) {
                    newStack.addLast(TlStruct(tlStruct))
                }
                return newStack
            }

        private inner class TlStruct(
            val tag: Int,
            var length: Int = Int.MAX_VALUE,
            var valueBytesProcessed: Int = 0
        ) {

            constructor(original: TlStruct) : this(
                original.tag,
                original.length,
                original.valueBytesProcessed
            )

            fun updateValueBytesProcessed(n: Int) {
                this.valueBytesProcessed += n
            }

            override fun toString(): String {
                return "[TlStruct " + Integer.toHexString(tag) + ", " + length + ", " + this.valueBytesProcessed + "]"
            }
        }
    }
}