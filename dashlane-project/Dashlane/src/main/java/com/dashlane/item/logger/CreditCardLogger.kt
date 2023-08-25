package com.dashlane.item.logger

import com.dashlane.inapplogin.UsageLogCode35Action
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository

class CreditCardLogger(
    private val teamspaceAccessor: TeamspaceAccessor,
    dataCounter: DataCounter,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : BaseLogger(teamspaceAccessor, dataCounter, sessionManager, bySessionUsageLogRepository) {

    var origin: String? = null

    fun logCopySecurityCode() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.COPY_SECURITY_CODE
            )
        )
    }

    fun logCopyCardNumber() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.COPY_NUMBER
            )
        )
    }

    fun logNfcDialogShown() {
        sendUsageLog75("scanPrompt", "display")
        origin = null
    }

    fun logNfcDialogDismissed() {
        sendUsageLog75("scanPrompt", "dismiss")
    }

    fun logNfcSuccessDialogShown() {
        sendUsageLog75("successScanPrompt", "display")
        origin = "nfcScan"
    }

    fun logNfcSuccessDialogClicked() {
        sendUsageLog75("successScanPrompt", "click")
    }

    private fun sendUsageLog75(subtype: String, action: String) {
        log(
            UsageLogCode75(
                type = "nfcCreditCardPopup",
                action = action,
                subtype = subtype
            )
        )
    }

    fun logRevealCardNumber() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.SHOW_NUMBER
            )
        )
    }

    fun logRevealCVV() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.SHOW_SECURITY_CODE
            )
        )
    }

    fun logRevealNotes() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.SHOW_NOTES
            )
        )
    }
}