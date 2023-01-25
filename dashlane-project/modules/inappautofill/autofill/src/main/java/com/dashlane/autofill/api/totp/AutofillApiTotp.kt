package com.dashlane.autofill.api.totp

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.dashlane.autofill.api.totp.actions.ClipboardTotpCode
import com.dashlane.autofill.api.totp.actions.RemoveTotpNotification
import com.dashlane.autofill.api.totp.actions.StartTotpNotification
import com.dashlane.autofill.api.totp.actions.UpdateTotpNotification
import com.dashlane.xml.domain.SyncObject



interface AutofillApiTotp {
    fun startTotpNotification(credential: SyncObject.Authentifiant): String?
    fun updateTotpNotification(totpNotificationId: String)
    fun clipboardTotpCode(totpNotificationId: String)
    fun clipboardTotpCode(credential: SyncObject.Authentifiant)
    fun removeTotpNotification(totpNotificationId: String)

    private class AutofillApiTotpWrapper(
        private val component: AutofillApiTotpComponent
    ) : AutofillApiTotp {
        override fun startTotpNotification(credential: SyncObject.Authentifiant): String? {
            return StartTotpNotification(
                component.autofillApiTotpService,
                component.totpRepository,
                component.totpNotificationUpdateService
            ).execute(credential)
        }

        override fun updateTotpNotification(totpNotificationId: String) {
            UpdateTotpNotification(
                component.autofillApiTotpService,
                component.totpRepository,
                component.totpNotificationDisplayService,
                component.totpNotificationUpdateService,
                component.loggerService
            ).execute(totpNotificationId)
        }

        override fun clipboardTotpCode(totpNotificationId: String) {
            ClipboardTotpCode(
                component.autofillApiTotpService,
                component.totpRepository,
                component.totpNotificationDisplayService,
                component.clipboardService,
                component.loggerService,
                component.toaster
            ).execute(totpNotificationId)
        }

        override fun clipboardTotpCode(credential: SyncObject.Authentifiant) {
            ClipboardTotpCode(
                component.autofillApiTotpService,
                component.totpRepository,
                component.totpNotificationDisplayService,
                component.clipboardService,
                component.loggerService,
                component.toaster
            ).executeWithoutNotification(credential)
        }

        override fun removeTotpNotification(totpNotificationId: String) {
            RemoveTotpNotification(
                component.totpRepository,
                component.totpNotificationDisplayService,
                component.totpNotificationUpdateService,
                component.loggerService
            ).execute(totpNotificationId)
        }
    }

    companion object {
        operator fun invoke(context: Context) =
            AutofillApiTotpWrapper((context.applicationContext as AutofillApiTotpApplication).component) as AutofillApiTotp

        @VisibleForTesting
        operator fun invoke(component: AutofillApiTotpComponent) =
            AutofillApiTotpWrapper(component) as AutofillApiTotp
    }
}
