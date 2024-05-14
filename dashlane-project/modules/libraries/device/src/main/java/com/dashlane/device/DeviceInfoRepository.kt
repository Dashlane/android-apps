package com.dashlane.device

interface DeviceInfoRepository {
    val deviceId: String?
    val anonymousDeviceId: String?
    val deviceCountry: String
    val deviceName: String
}
