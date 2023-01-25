package com.dashlane.login.pages.totp.u2f

import android.hardware.usb.UsbDevice



interface UsbServiceDetector {

    

    suspend fun detectUsbKey(): UsbDevice

    

    fun ignore(device: UsbDevice)
}