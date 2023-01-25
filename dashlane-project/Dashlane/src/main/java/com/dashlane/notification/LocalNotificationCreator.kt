package com.dashlane.notification

import com.dashlane.notification.creator.AutoFillNotificationCreator
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class LocalNotificationCreator @Inject constructor(
    private val autoFillNotificationCreator: AutoFillNotificationCreator
) {
    

    fun registerAccountCreation() {
        autoFillNotificationCreator.createForNewUser()
    }

    

    fun register() {
        autoFillNotificationCreator.createForExistingUser()
    }
}