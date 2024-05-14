package com.dashlane.login.controller

import com.dashlane.notification.model.TokenNotificationHandler
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UseDataClass")
@Singleton
class LoginTokensModule @Inject constructor() {
    val tokenHashmap = HashMap<String, TokenNotificationHandler>()
    val tokenShouldNotifyHashmap = HashMap<String, Boolean>()
}