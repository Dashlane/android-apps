package com.dashlane.core.u2f.transport

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbRequest
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer



class UsbTransportHelper {
    var channel: Int = CHANNEL_BROADCAST

    

    @Suppress("DEPRECATION")
    @Throws(IOException::class)
    fun exchange(
        connection: UsbDeviceConnection,
        input: UsbEndpoint,
        output: UsbEndpoint,
        cmd: Byte,
        data: ByteArray
    ): ByteArray? {
        
        val bytes = wrapDataApdu(cmd, data)
        var offset = 0
        val requestWrite = UsbRequest()
        if (!requestWrite.initialize(connection, output)) throw IOException("Can't initialize write for command: $cmd")
        var transferBuffer = ByteArray(HID_BUFFER_SIZE)
        while (offset != bytes.size) {
            val blockSize = if (bytes.size - offset > HID_BUFFER_SIZE) HID_BUFFER_SIZE else bytes.size - offset
            System.arraycopy(bytes, offset, transferBuffer, 0, blockSize)
            if (!requestWrite.queue(ByteBuffer.wrap(transferBuffer), HID_BUFFER_SIZE)) {
                requestWrite.close()
                throw IOException("Can't write for command: $cmd")
            }
            connection.requestWait()
            offset += blockSize
        }

        
        val requestRead = UsbRequest()
        if (!requestRead.initialize(connection, input)) throw IOException("Can't initialize read for command: $cmd")
        transferBuffer = ByteArray(HID_BUFFER_SIZE)
        val responseBuffer = ByteBuffer.allocate(HID_BUFFER_SIZE)
        val response = ByteArrayOutputStream()
        var responseData: ByteArray? = null
        while (responseData == null) {
            responseData = unwrapResponseApdu(cmd, response.toByteArray())
            if (responseData != null) {
                requestWrite.close()
                requestRead.close()
                return responseData
            }
            
            responseBuffer.clear()
            if (!requestRead.queue(responseBuffer, HID_BUFFER_SIZE)) {
                requestWrite.close()
                requestRead.close()
                throw IOException("Can't read for command: $cmd")
            }
            connection.requestWait()
            responseBuffer.rewind()
            responseBuffer.get(transferBuffer, 0, HID_BUFFER_SIZE)
            response.write(transferBuffer, 0, HID_BUFFER_SIZE)
        }
        
        return null
    }

    

    fun wrapDataApdu(tag: Byte, data: ByteArray): ByteArray {
        var sequenceIdx = 0
        var offset = 0
        val output = ByteArrayOutputStream().apply {
            write(channel shr 24)
            write(channel shr 16)
            write(channel shr 8)
            write(channel)
            write(tag.toInt())
            write(data.size shr 8)
            write(data.size)
        }
        var blockSize = if (data.size > HID_BUFFER_SIZE - 7) HID_BUFFER_SIZE - 7 else data.size
        output.write(data, offset, blockSize)
        offset += blockSize
        while (offset != data.size) {
            output.write(channel shr 24)
            output.write(channel shr 16)
            output.write(channel shr 8)
            output.write(channel)
            output.write(sequenceIdx)
            sequenceIdx++
            blockSize = if (data.size - offset > HID_BUFFER_SIZE - 5) HID_BUFFER_SIZE - 5 else data.size - offset
            output.write(data, offset, blockSize)
            offset += blockSize
        }
        if (output.size() % HID_BUFFER_SIZE != 0) {
            val padding = ByteArray(HID_BUFFER_SIZE - (output.size() % HID_BUFFER_SIZE))
            output.write(padding, 0, padding.size)
        }
        return output.toByteArray()
    }

    

    @Throws(IOException::class)
    fun unwrapResponseApdu(tag: Byte, data: ByteArray?): ByteArray? {
        var offset = 0
        var sequenceIdx = 0
        if (data == null || data.size < 7) {
            return null
        }
        var readChannel = (data[offset].toInt() and 0xff shl 24) or (data[offset + 1].toInt() and 0xff shl 16) or
                (data[offset + 2].toInt() and 0xff shl 8) or (data[offset + 3].toInt() and 0xff)
        if (readChannel != channel) {
            if (CHANNEL_BROADCAST == channel) {
                channel = readChannel
            } else {
                throw IOException("Invalid channel")
            }
        }
        offset += 4
        if (data[offset++] != tag) {
            throw IOException("Invalid command")
        }
        var responseLength = data[offset++].toInt() and 0xff shl 8
        responseLength = responseLength or (data[offset++].toInt() and 0xff)
        if (data.size < 7 + responseLength) {
            return null
        }
        var blockSize = if (responseLength > HID_BUFFER_SIZE - 7) HID_BUFFER_SIZE - 7 else responseLength
        val response = ByteArrayOutputStream()
        response.write(data, offset, blockSize)
        offset += blockSize
        while (response.size() != responseLength) {
            if (offset == data.size) {
                return null
            }
            readChannel = (data[offset].toInt() and 0xff shl 24) or (data[offset + 1].toInt() and 0xff shl 16) or
                    (data[offset + 2].toInt() and 0xff shl 8) or (data[offset + 3].toInt() and 0xff)
            offset += 4
            if (readChannel != channel) {
                throw IOException("Invalid channel")
            }
            if (data[offset++].toInt() != sequenceIdx) {
                throw IOException("Invalid sequence")
            }
            blockSize =
                if (responseLength - response.size() > HID_BUFFER_SIZE - 5) HID_BUFFER_SIZE - 5 else responseLength - response.size()
            if (blockSize > data.size - offset) {
                return null
            }
            response.write(data, offset, blockSize)
            offset += blockSize
            sequenceIdx++
        }
        return response.toByteArray()
    }

    

    fun getUsagePageOrUsage(data: ByteArray, size: Int, getUsage: Boolean): Int {
        var result = -1
        var i = 0
        var sizeCode: Int
        var dataLen: Int
        var keySize: Int
        loop@ while (i < size) {
            val key = data[i].toInt() and 0xff
            val keyCmd = key and 0xfc
            if (key and 0xf0 == 0xf0) {
                dataLen = if (i + 1 < size) {
                    data[i + 1].toInt()
                } else {
                    0
                }
                keySize = 3
            } else {
                sizeCode = key and 0x03
                dataLen = when (sizeCode) {
                    0, 1, 2 -> sizeCode
                    3 -> 4
                    else -> 0
                }
                keySize = 1
            }
            when {
                keyCmd == 0x04 && !getUsage -> {
                    result = getBytes(data, size, dataLen, i)
                    break@loop
                }
                keyCmd == 0x08 && getUsage -> {
                    result = getBytes(data, size, dataLen, i)
                    break@loop
                }
            }
            i += dataLen + keySize
        }
        return result
    }

    fun getResponseCode(response: ByteArray?): Int {
        return response?.let {
            if (it.size < 2) {
                -2
            } else {
                (it[it.size - 2].toInt() and 0xff shl 8) or (it[it.size - 1].toInt() and 0xff)
            }
        } ?: -1
    }

    private fun getBytes(data: ByteArray, length: Int, size: Int, cur: Int): Int {
        if (cur + size >= length) {
            return 0
        }
        return when (size) {
            1 -> data[cur + 1].toInt() and 0xff
            2 -> data[cur + 2].toInt() and 0xff shl 8 or (data[cur + 1].toInt() and 0xff)
            3 -> (data[cur + 4].toInt() and 0xff shl 24) or (data[cur + 3].toInt() and 0xff shl 16) or
                    (data[cur + 2].toInt() and 0xff shl 8) or (data[cur + 1].toInt() and 0xff)
            else -> 0
        }
    }

    companion object {
        const val CHANNEL_BROADCAST = 0xffffffff.toInt()
        const val CMD_INIT = 0x86.toByte()
        const val CMD_MSG = 0x83.toByte()
        const val RESP_OK = 0x9000
        const val RESP_USER_PRESENCE_REQUIRED = 0x6985
        private const val HID_BUFFER_SIZE = 64
    }
}