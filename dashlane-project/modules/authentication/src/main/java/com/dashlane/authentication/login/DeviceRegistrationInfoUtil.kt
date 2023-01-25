package com.dashlane.authentication.login

import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.server.api.endpoints.Platform
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationDevice

internal fun DeviceRegistrationInfo.toAuthRegistrationDevice() = AuthRegistrationDevice(
    osCountry = osCountry,
    temporary = false,
    appVersion = appVersion,
    deviceName = deviceName,
    platform = Platform.SERVER_ANDROID,
    osLanguage = osLanguage
)