package com.dashlane.login.pages.totp.u2f

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.nfc.tech.IsoDep
import com.dashlane.core.u2f.U2fKey
import com.dashlane.core.u2f.transport.NfcTransport
import com.dashlane.core.u2f.transport.UsbTransport
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.selects.select
import javax.inject.Inject

class U2fKeyDetectorImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val usbManager: UsbManager?,
    private val nfcServiceDetector: NfcServiceDetector,
    private val usbServiceDetector: UsbServiceDetector
) : U2fKeyDetector {

    private var detectUsbDeferred: Deferred<UsbDevice>? = null
    private var detectNfcDeferred: Deferred<IsoDep>? = null

    override suspend fun detectKey(coroutineScope: CoroutineScope): U2fKey {
        cancel()
        detectNfcDeferred = coroutineScope.async { nfcServiceDetector.detectNfcTag() }
        detectUsbDeferred = coroutineScope.async { usbServiceDetector.detectUsbKey() }
        return select {
            detectNfcDeferred!!.onAwait {
                cancel()
                U2fKey(NfcTransport(it))
            }
            detectUsbDeferred!!.onAwait {
                cancel()

                
                U2fKey(UsbTransport(context, usbManager!!, it))
            }
        }
    }

    override fun ignore(u2fKey: U2fKey) {
        
        val transport = u2fKey.transport
        if (transport is UsbTransport) {
            usbServiceDetector.ignore(transport.usbDevice)
        }
    }

    override fun cancel() {
        detectNfcDeferred?.cancel()
        detectUsbDeferred?.cancel()
    }
}