package com.dashlane.autofill.api.totp.actions

import com.dashlane.autofill.api.totp.repository.TotpNotificationRepository
import com.dashlane.autofill.api.totp.services.AutofillApiTotpNotificationLogger
import com.dashlane.autofill.api.totp.services.TotpNotificationDisplayService
import com.dashlane.autofill.api.totp.services.TotpNotificationUpdateService

class RemoveTotpNotification(
    private val totpRepository: TotpNotificationRepository,
    private val totpNotificationDisplayService: TotpNotificationDisplayService,
    private val totpNotificationUpdateService: TotpNotificationUpdateService,
    private val totpNotificationLogger: AutofillApiTotpNotificationLogger

) {

    fun execute(totpNotificationId: String) {
        val totpNotification = totpRepository.getTotpNotification(totpNotificationId)

        totpNotificationDisplayService.dismissAll()
        totpNotificationLogger.totpNotificationDismiss(totpNotification)

        if (totpNotification == null) {
            return
        }

        totpRepository.removeTotpNotification(totpNotification.id)
        totpNotificationUpdateService.cancelNextUpdate(totpNotification.id)
    }
}
