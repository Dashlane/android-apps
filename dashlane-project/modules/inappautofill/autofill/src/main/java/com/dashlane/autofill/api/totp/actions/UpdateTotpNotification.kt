package com.dashlane.autofill.api.totp.actions

import com.dashlane.authenticator.Totp
import com.dashlane.authenticator.otp
import com.dashlane.autofill.api.totp.model.TotpNotification
import com.dashlane.autofill.api.totp.model.TotpResult
import com.dashlane.autofill.api.totp.repository.TotpNotificationRepository
import com.dashlane.autofill.api.totp.services.AutofillApiTotpNotificationLogger
import com.dashlane.autofill.api.totp.services.AutofillApiTotpService
import com.dashlane.autofill.api.totp.services.TotpNotificationDisplayService
import com.dashlane.autofill.api.totp.services.TotpNotificationUpdateService

class UpdateTotpNotification(
    private val autofillTotp: AutofillApiTotpService,
    private val totpRepository: TotpNotificationRepository,
    private val totpNotificationDisplayService: TotpNotificationDisplayService,
    private val totpNotificationUpdateService: TotpNotificationUpdateService,
    private val totpNotificationLogger: AutofillApiTotpNotificationLogger
) {
    companion object {
        const val MAXIMUM_CODE_UPDATES_WITHOUT_COPIES = 3
        const val MAXIMUM_CODE_UPDATES_WITH_COPIES = 5
    }

    fun execute(totpNotificationId: String) {
        val totpNotification = totpRepository.getTotpNotification(totpNotificationId)
            ?: return

        if (!canDisplayNotification(totpNotification)) {
            removeTotpNotification(totpNotificationId)
            return
        }

        val credential = totpNotification.credential
        val otp = credential.otp()?.let { it as? Totp } ?: return
        val totpResult = autofillTotp.getTotp(otp)

        if (totpResult == null) {
            removeTotpNotification(totpNotificationId)
            return
        }

        when {
            isFirstShow(totpNotification) ->
                updateWithFirstPin(totpNotificationId, totpNotification, totpResult)
            isUpdatingNotCopied(totpNotification) ->
                updateWithRenewedPin(totpNotificationId, totpNotification, totpResult)
            isUpdatingAlreadyCopied(totpNotification) ->
                updateWithSafeguardPin(totpNotificationId, totpNotification, totpResult)
            else ->
                removeTotpNotification(totpNotificationId)
        }
    }

    private fun canDisplayNotification(totpNotification: TotpNotification): Boolean {
        if (totpNotification.totalCodeUpdates >= MAXIMUM_CODE_UPDATES_WITH_COPIES) {
            return false
        }
        if (totpNotification.hasCopies && !totpNotification.isCodeUpdatedCopied) {
            return false
        }
        if (!totpNotification.hasCopies && totpNotification.totalCodeUpdates >= MAXIMUM_CODE_UPDATES_WITHOUT_COPIES) {
            return false
        }

        return true
    }

    private fun removeTotpNotification(totpNotificationId: String) {
        totpRepository.removeTotpNotification(totpNotificationId)
        totpNotificationDisplayService.dismissAll()
    }

    private fun isFirstShow(totpNotification: TotpNotification): Boolean {
        return totpNotification.totalCodeUpdates == 0
    }

    private fun updateWithFirstPin(
        totpNotificationId: String,
        totpNotification: TotpNotification,
        totpResult: TotpResult
    ) {
        updateTotpNotification(totpNotification, totpResult)
        totpNotificationDisplayService.display(
            totpNotificationId,
            totpNotification.credential.title,
            totpResult.code,
            totpResult.timeRemainingMilliseconds
        )
        totpNotificationLogger.totpNotificationDisplay(totpNotification)
    }

    private fun isUpdatingNotCopied(totpNotification: TotpNotification): Boolean {
        return totpNotification.totalCodeUpdates > 0 && !totpNotification.hasCopies
    }

    private fun updateWithRenewedPin(
        totpNotificationId: String,
        totpNotification: TotpNotification,
        totpResult: TotpResult
    ) {
        updateTotpNotification(totpNotification, totpResult)
        totpNotificationDisplayService.updateWithNewCode(
            totpNotificationId,
            totpNotification.credential.title,
            totpResult.code,
            totpResult.timeRemainingMilliseconds
        )
        totpNotificationLogger.totpNotificationDisplay(totpNotification)
    }

    private fun isUpdatingAlreadyCopied(totpNotification: TotpNotification): Boolean {
        return totpNotification.totalCodeUpdates > 0 && totpNotification.hasCopies
    }

    private fun updateWithSafeguardPin(
        totpNotificationId: String,
        totpNotification: TotpNotification,
        totpResult: TotpResult
    ) {
        updateTotpNotification(totpNotification, totpResult)
        totpNotificationDisplayService.updateWithSafeguardCode(
            totpNotificationId,
            totpNotification.credential.title,
            totpResult.code,
            totpResult.timeRemainingMilliseconds
        )
        totpNotificationLogger.totpNotificationDisplay(totpNotification)
    }

    private fun updateTotpNotification(totpNotification: TotpNotification, totpResult: TotpResult) {
        totpNotification.totpCodeUpdated()
        totpRepository.addTotpNotification(totpNotification)
        totpNotificationUpdateService.registerNextUpdate(
            totpNotification.id,
            totpResult.timeRemainingMilliseconds
        )
    }
}
