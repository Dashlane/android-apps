package com.dashlane.notification

import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.devices.SetPushNotificationIDService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import javax.inject.Inject

class PushNotificationRegister @Inject constructor(
    private val service: SetPushNotificationIDService,
    private val sessionManager: SessionManager,
) {
    suspend fun register(registeredId: String): Boolean {
        try {
            val session = sessionManager.session ?: return false
            service.execute(
                userAuthorization = session.authorization,
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
}