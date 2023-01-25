package com.dashlane.core.u2f.transport

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import com.dashlane.core.u2f.U2fChallenge
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.registerExportedReceiverCompat
import com.dashlane.util.sha256
import com.dashlane.util.tryAsSuccess
import com.dashlane.util.tryOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.asKotlinRandom



class UsbTransport(
    private val context: Context,
    private val manager: UsbManager,
    val usbDevice: UsbDevice
) : Transport {
    private val permissionAction = "${context.packageName}.USB_PERMISSION"
    private val transportHelper = UsbTransportHelper()

    private lateinit var connection: UsbDeviceConnection
    private lateinit var dongleInterface: UsbInterface
    private lateinit var input: UsbEndpoint
    private lateinit var output: UsbEndpoint

    override suspend fun init(): Boolean {
        if (!askPermissions()) return false
        connection = manager.openDevice(usbDevice)
        
        if (u2fInterfaceReady()) return tryAsSuccess { initChannel(connection, input, output) }
        return false
    }

    private fun u2fInterfaceReady(): Boolean {
        for (i in 0 until usbDevice.interfaceCount) {
            dongleInterface = usbDevice.getInterface(i)
            for (j in 0 until dongleInterface.endpointCount) {
                val tmpEndpoint = dongleInterface.getEndpoint(j)
                if (tmpEndpoint.direction == UsbConstants.USB_DIR_IN) {
                    input = tmpEndpoint
                } else {
                    output = tmpEndpoint
                }
            }
            connection.apply {
                claimInterface(dongleInterface, true)
                setInterface(dongleInterface)
            }
            val descriptor = ByteArray(256)
            
            tryAsSuccess {
                connection.controlTransfer(
                    UsbConstants.USB_DIR_IN or 0x01,
                    0x06,
                    (0x22 shl 8),
                    i,
                    descriptor,
                    descriptor.size,
                    2000
                )
            }
            if (transportHelper.getUsagePageOrUsage(descriptor, descriptor.size, true) == USAGE_U2F &&
                transportHelper.getUsagePageOrUsage(descriptor, descriptor.size, false) == USAGE_FIDO
            ) {
                
                return true
            }
        }
        return false
    }

    override fun sign(challenge: U2fChallenge) = tryOrNull { sign(challenge, connection, input, output) }

    override fun close() {
        for (i in 0 until usbDevice.interfaceCount) {
            connection.releaseInterface(usbDevice.getInterface(i))
        }
        connection.close()
    }

    private suspend fun askPermissions() = suspendCoroutine<Boolean> {
        context.registerExportedReceiverCompat(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (permissionAction == intent.action) {
                    val device = intent.getParcelableExtraCompat<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    context.unregisterReceiver(this)
                    it.resume(device != null && granted)
                } else {
                    it.resume(false)
                }
            }
        }, IntentFilter(permissionAction))

        
        manager.requestPermission(
            usbDevice,
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(permissionAction),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            )
        )
    }

    

    @Throws(IOException::class)
    private fun initChannel(connection: UsbDeviceConnection, input: UsbEndpoint, output: UsbEndpoint) {
        transportHelper.channel = UsbTransportHelper.CHANNEL_BROADCAST
        val nonce = SecureRandom().asKotlinRandom().nextBytes(8)
        val response = transportHelper.exchange(connection, input, output, UsbTransportHelper.CMD_INIT, nonce)
        response?.let {
            val readNonce = ByteArray(8)
            System.arraycopy(it, 0, readNonce, 0, 8)
            if (!nonce.contentEquals(readNonce)) {
                throw IOException("Invalid channel initialization")
            }
            transportHelper.channel = (it[8].toInt() and 0xff shl 24) or (it[9].toInt() and 0xff shl 16) or
                    (it[10].toInt() and 0xff shl 8) or (it[11].toInt() and 0xff)
        } ?: throw IOException("Init channel failed")
    }

    @Throws(ApduException::class)
    private fun sign(
        challenge: U2fChallenge,
        connection: UsbDeviceConnection,
        input: UsbEndpoint,
        output: UsbEndpoint
    ): ByteArray? {
        val bos = ByteArrayOutputStream().apply {
            val keyHandle = challenge.keyHandleBytes
            val msgLength = 32 + 32 + 1 + keyHandle.size
            val clientDataString = challenge.clientDataString ?: return null
            val clientParam = clientDataString.sha256()
            val appParam = challenge.origin.sha256()
            write(0x00) 
            write(0x02) 
            write(0x03) 
            write(0x00) 
            write(0x00) 
            write(msgLength shr 8)
            write(msgLength and 0xff)
            write(clientParam)
            write(appParam)
            write(keyHandle.size)
            write(keyHandle)
            write(0x00)
            write(0x00)
        }

        var resp: ByteArray? = null
        runCatching {
            var busy = true
            while (busy) {
                resp = null
                Thread.sleep(300L)
                resp =
                    transportHelper.exchange(connection, input, output, UsbTransportHelper.CMD_MSG, bos.toByteArray())
                busy = isResponseBusy(resp)
            }
        }
        if (!isResponseOK(resp)) {
            throw ApduException(transportHelper.getResponseCode(resp))
        }
        return resp!!.sliceArray(IntRange(0, resp!!.size - 2))
    }

    private fun isResponseOK(response: ByteArray?) =
        transportHelper.getResponseCode(response) == UsbTransportHelper.RESP_OK

    private fun isResponseBusy(response: ByteArray?) =
        transportHelper.getResponseCode(response) == UsbTransportHelper.RESP_USER_PRESENCE_REQUIRED

    companion object {
        

        private const val USAGE_U2F = 0x01

        

        private const val USAGE_FIDO = 0xf1d0
    }
}