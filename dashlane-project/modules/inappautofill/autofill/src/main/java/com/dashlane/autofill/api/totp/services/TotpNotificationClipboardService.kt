package com.dashlane.autofill.api.totp.services



interface TotpNotificationClipboardService {
    fun copy(totpNotificationId: String?, code: String)
}