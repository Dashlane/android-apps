package com.dashlane.autofill.api.totp

import android.content.Context
import com.dashlane.autofill.api.totp.repository.TotpNotificationRepository
import com.dashlane.autofill.api.totp.services.AutofillApiTotpNotificationLogger
import com.dashlane.autofill.api.totp.services.AutofillApiTotpService
import com.dashlane.autofill.api.totp.services.TotpNotificationDisplayService
import com.dashlane.autofill.api.totp.services.TotpNotificationClipboardService
import com.dashlane.autofill.api.totp.services.TotpNotificationUpdateService
import com.dashlane.util.Toaster

interface AutofillApiTotpComponent {
    val autofillApiTotpService: AutofillApiTotpService
    val totpRepository: TotpNotificationRepository
    val totpNotificationDisplayService: TotpNotificationDisplayService
    val totpNotificationUpdateService: TotpNotificationUpdateService
    val clipboardService: TotpNotificationClipboardService
    val loggerService: AutofillApiTotpNotificationLogger
    val toaster: Toaster

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiTotpApplication).component
    }
}
