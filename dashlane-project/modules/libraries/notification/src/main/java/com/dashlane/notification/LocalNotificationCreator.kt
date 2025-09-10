package com.dashlane.notification

import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.notification.creator.ChromeAutofillNotificationCreator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNotificationCreator @Inject constructor(
    private val autoFillNotificationCreator: AutoFillNotificationCreator,
    private val chromeAutoFillNotificationCreator: ChromeAutofillNotificationCreator,
) {
    fun registerAccountCreation() {
        autoFillNotificationCreator.createForNewUser()
    }

    fun register() {
        autoFillNotificationCreator.createForExistingUser()
        chromeAutoFillNotificationCreator.createForExistingUser()
    }
}