package com.dashlane.login.pages.totp.u2f

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import kotlinx.coroutines.delay
import javax.inject.Inject

class UsbServiceDetectorImpl @Inject constructor(private val manager: UsbManager?) : UsbServiceDetector {

    private val blacklistedDevices = mutableListOf<UsbDevice>()

    override suspend fun detectUsbKey(): UsbDevice {
        return findDevice()
    }

    private suspend fun findDevice(): UsbDevice {
        var foundDevice: UsbDevice? = null
        while (foundDevice == null) {
            foundDevice = findU2fKey()
            
            delay(500)
        }
        return foundDevice
    }

    private fun findU2fKey(): UsbDevice? {
        var blackListedDeviceFound = false
        var foundDevice: UsbDevice? = null
        if (manager == null) {
            return null
        }
        for (device in manager.deviceList.values) {
            val deviceClass = device.deviceClass
            if (deviceClass == UsbConstants.USB_CLASS_HID || deviceClass == UsbConstants.USB_CLASS_PER_INTERFACE) {
                if (blacklistedDevices.contains(device)) blackListedDeviceFound = true else foundDevice = device
            }
        }
        
        if (!blackListedDeviceFound) blacklistedDevices.clear()
        return foundDevice
    }

    override fun ignore(device: UsbDevice) {
        blacklistedDevices.add(device)
    }
}