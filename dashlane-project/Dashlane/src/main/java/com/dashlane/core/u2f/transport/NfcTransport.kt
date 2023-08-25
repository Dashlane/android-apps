package com.dashlane.core.u2f.transport

import android.nfc.tech.IsoDep
import com.dashlane.core.u2f.U2fChallenge
import com.dashlane.util.sha256
import com.dashlane.util.tryAsSuccess
import com.dashlane.util.tryOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException

class NfcTransport(private val tag: IsoDep) : Transport {
    private val selectCommand =
        byteArrayOf(0x00, 0xa4.toByte(), 0x04, 0x00, 0x08, 0xa0.toByte(), 0x00, 0x00, 0x06, 0x47, 0x2f, 0x00, 0x01)
    private val selectCommandYubico =
        byteArrayOf(0x00, 0xa4.toByte(), 0x04, 0x00, 0x07, 0xa0.toByte(), 0x00, 0x00, 0x05, 0x27, 0x10, 0x02)
    private val getResponseCommand = byteArrayOf(0x00, 0xc0.toByte(), 0x00, 0x00, 0xff.toByte())


    override suspend fun init(): Boolean {
        tag.timeout = 5000
        if (!tryAsSuccess { tag.connect() }) return false
        return try {
            send(selectCommand)
            true
        } catch (e: ApduException) {
            if (e.code == 0x6a82) {
                tryAsSuccess { send(selectCommandYubico) }
            } else {
                false
            }
        }
    }

    override fun sign(challenge: U2fChallenge): ByteArray? {
        
        
        
        

        val appParam = challenge.origin.sha256()
        val keyHandle = challenge.keyHandleBytes

        val clientDataString = challenge.clientDataString
        val clientParam = clientDataString!!.sha256()

        val apdu = ByteArray(5 + 32 + 32 + 1 + keyHandle.size + 1)
        apdu[1] = 0x02 
        apdu[2] = 0x03 
        apdu[4] = (64 + 1 + keyHandle.size).toByte() 
        apdu[apdu.size - 1] = 0xff.toByte()
        System.arraycopy(clientParam, 0, apdu, 5, 32)
        System.arraycopy(appParam, 0, apdu, 5 + 32, 32)
        apdu[5 + 64] = keyHandle.size.toByte()
        System.arraycopy(keyHandle, 0, apdu, 5 + 64 + 1, keyHandle.size)

        return tryOrNull { send(apdu) }
    }

    override fun close() {
        tryAsSuccess { tag.close() }
    }

    
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    @Throws(IOException::class, ApduException::class)
    private fun send(apdu: ByteArray): ByteArray {
        var cmd = apdu
        var status = 0x6100
        val data = ByteArrayOutputStream()

        while (status and 0xff00 == BYTES_REMAINING_CODE) { 
            val resp = tag.transceive(cmd)
            status = 0xff and resp[resp.size - 2].toInt() shl 8 or (0xff and resp[resp.size - 1].toInt())
            data.write(resp)
            cmd = getResponseCommand
        }

        if (status != NO_ERROR_CODE) {
            throw ApduException(status)
        }

        return data.toByteArray()
    }

    companion object {
        

        private const val NO_ERROR_CODE = 0x9000
        private const val BYTES_REMAINING_CODE = 0x6100
    }
}