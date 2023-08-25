package com.dashlane.autofill.api.totp.services

interface TotpNotificationUpdateService {
    fun registerNextUpdate(totpNotificationId: String, timeRemainingMilliseconds: Long)
    fun cancelNextUpdate(totpNotificationId: String)
}
