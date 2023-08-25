package com.dashlane.autofill.api.totp.actions

import com.dashlane.authenticator.Totp
import com.dashlane.authenticator.otp
import com.dashlane.autofill.api.totp.model.TotpNotification
import com.dashlane.autofill.api.totp.repository.TotpNotificationRepository
import com.dashlane.autofill.api.totp.services.AutofillApiTotpService
import com.dashlane.autofill.api.totp.services.TotpNotificationUpdateService
import com.dashlane.xml.domain.SyncObject
import java.util.UUID

class StartTotpNotification(
    private val autofillTotp: AutofillApiTotpService,
    private val totpRepository: TotpNotificationRepository,
    private val totpNotificationUpdateService: TotpNotificationUpdateService
) {

    fun execute(credential: SyncObject.Authentifiant): String? {
        val totp = credential.otp()?.let { it as? Totp } ?: return null
        val totpNotification = TotpNotification(
            UUID.randomUUID().toString(),
            credential
        )

        if (autofillTotp.getTotp(totp) == null) {
            return null
        }

        totpRepository.addTotpNotification(totpNotification)
        totpNotificationUpdateService.registerNextUpdate(totpNotification.id, 0)

        return totpNotification.id
    }
}
