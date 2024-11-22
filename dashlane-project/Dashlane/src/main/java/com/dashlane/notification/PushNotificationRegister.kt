package com.dashlane.notification

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.devices.ClearPushNotificationIDService
import com.dashlane.server.api.endpoints.devices.SetPushNotificationIDService
import com.dashlane.server.api.exceptions.DashlaneApiException
import javax.inject.Inject

class PushNotificationRegister @Inject constructor(
    private val setPushNotificationIDService: SetPushNotificationIDService,
    private val clearPushNotificationIDService: ClearPushNotificationIDService,
) {
    suspend fun register(registeredId: String, authorization: Authorization.User): Boolean {
        try {
            setPushNotificationIDService.execute(
                userAuthorization = authorization,
                request = SetPushNotificationIDService.Request(
                    type = SetPushNotificationIDService.Request.Type.GOOGLE,
                    pushID = registeredId,
                    sendToAppboy = true
                )
            )
        } catch (_: DashlaneApiException) {
            return false
        }
        return true
    }

    suspend fun unregister(registeredId: String, authorization: Authorization.User): Boolean {
        try {
            clearPushNotificationIDService.execute(
                userAuthorization = authorization,
                request = ClearPushNotificationIDService.Request(pushID = registeredId)
            )
        } catch (_: DashlaneApiException) {
            return false
        }
        return true
    }
}