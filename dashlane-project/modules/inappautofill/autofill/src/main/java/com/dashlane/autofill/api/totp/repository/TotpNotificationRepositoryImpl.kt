package com.dashlane.autofill.api.totp.repository

import com.dashlane.autofill.api.totp.model.TotpNotification
import javax.inject.Inject

class TotpNotificationRepositoryImpl @Inject constructor() : TotpNotificationRepository {
    private val totpNotifications = mutableMapOf<String, TotpNotification>()

    override fun addTotpNotification(totpNotification: TotpNotification) {
        totpNotifications[totpNotification.id] = totpNotification
    }

    override fun getTotpNotification(id: String): TotpNotification? {
        return totpNotifications[id]
    }

    override fun removeTotpNotification(id: String) {
        totpNotifications.remove(id)
    }
}
