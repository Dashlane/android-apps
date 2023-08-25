package com.dashlane.autofill.api.totp.repository

import com.dashlane.autofill.api.totp.model.TotpNotification

interface TotpNotificationRepository {
    fun addTotpNotification(totpNotification: TotpNotification)

    fun getTotpNotification(id: String): TotpNotification?

    fun removeTotpNotification(id: String)
}
