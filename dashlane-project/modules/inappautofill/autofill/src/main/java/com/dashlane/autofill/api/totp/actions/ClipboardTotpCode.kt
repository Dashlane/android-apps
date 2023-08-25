package com.dashlane.autofill.api.totp.actions

import android.widget.Toast
import com.dashlane.authenticator.Totp
import com.dashlane.authenticator.otp
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.totp.repository.TotpNotificationRepository
import com.dashlane.autofill.api.totp.services.AutofillApiTotpNotificationLogger
import com.dashlane.autofill.api.totp.services.AutofillApiTotpService
import com.dashlane.autofill.api.totp.services.TotpNotificationClipboardService
import com.dashlane.autofill.api.totp.services.TotpNotificationDisplayService
import com.dashlane.util.Toaster
import com.dashlane.xml.domain.SyncObject

class ClipboardTotpCode(
    private val autofillTotp: AutofillApiTotpService,
    private val totpRepository: TotpNotificationRepository,
    private val totpNotificationDisplayService: TotpNotificationDisplayService,
    private val clipboardService: TotpNotificationClipboardService,
    private val totpNotificationLogger: AutofillApiTotpNotificationLogger,
    private val toaster: Toaster
) {
    fun execute(totpNotificationId: String) {
        val totpNotification = totpRepository.getTotpNotification(totpNotificationId) ?: return
        val credential = totpNotification.credential
        val otp = credential.otp()?.let { it as? Totp } ?: return
        val totpResult = autofillTotp.getTotp(otp) ?: return

        totpNotification.totpCodeCopied()
        totpRepository.addTotpNotification(totpNotification)

        clipboardService.copy(totpNotificationId, totpResult.code)
        totpNotificationDisplayService.updateInformingCodeCopied(
            totpNotificationId,
            totpNotification.credential.title,
            totpResult.code,
            totpResult.timeRemainingMilliseconds
        )
        totpNotificationLogger.totpNotificationCopied(totpNotification)
    }

    fun executeWithoutNotification(credential: SyncObject.Authentifiant) {
        val otp = credential.otp()?.let { it as? Totp } ?: return
        val totpResult = autofillTotp.getTotp(otp) ?: return
        clipboardService.copy(null, totpResult.code)
        toaster.show(R.string.autofill_totp_automatically_copy, Toast.LENGTH_SHORT)
    }
}
