package com.dashlane.notification

import com.dashlane.network.webservices.fcm.PushNotificationService
import javax.inject.Inject

class PushNotificationRegister @Inject constructor(private val service: PushNotificationService) {
    suspend fun register(username: String, uki: String, registeredId: String): Boolean {
        return try {
            service.setPushNotificationId(login = username, uki = uki, pushID = registeredId).isSuccess
        } catch (e: Exception) {
            false
        }
    }
}