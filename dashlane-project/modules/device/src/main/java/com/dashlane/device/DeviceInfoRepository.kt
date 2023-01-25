package com.dashlane.device



interface DeviceInfoRepository {
    val deviceId: String
    val anonymousDeviceId: String
    val deviceCountry: String
    val deviceCountryRefreshTimestamp: Long
    val inEuropeanUnion: Boolean
    val deviceName: String
}
