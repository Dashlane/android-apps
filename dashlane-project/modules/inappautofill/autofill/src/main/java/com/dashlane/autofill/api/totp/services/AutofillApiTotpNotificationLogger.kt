package com.dashlane.autofill.api.totp.services

import com.dashlane.autofill.api.totp.model.TotpNotification

interface AutofillApiTotpNotificationLogger {
    fun totpNotificationDisplay(totpNotification: TotpNotification)
    fun totpNotificationCopied(totpNotification: TotpNotification)
    fun totpNotificationDismiss(totpNotification: TotpNotification?)
}