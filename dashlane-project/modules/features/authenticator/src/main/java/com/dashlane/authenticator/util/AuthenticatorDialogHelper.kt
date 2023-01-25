package com.dashlane.authenticator.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.R
import com.dashlane.hermes.inject.HermesComponent
import com.dashlane.ui.util.DialogHelper
import com.dashlane.ui.util.withCenteredButtons
import com.dashlane.useractivity.hermes.TrackingLogUtils
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.getAllLinkedPackageName
import com.dashlane.vault.summary.SummaryObject

fun DialogHelper.showAuthenticatorRemoveConfirmDialog(
    context: Context,
    credentialName: String,
    topDomain: String?,
    linkedServices: SummaryObject.LinkedServices? = null,
    professional: Boolean,
    issuer: String?,
    confirmCallback: () -> Unit
): AlertDialog = builder(context, R.style.ThemeOverlay_Dashlane_DashlaneWarningDialog)
    .apply {
        setTitle(R.string.authenticator_item_edit_remove_popup_title)
        val name = credentialName.takeIf { it.isNotSemanticallyNull() }
            ?: context.getString(R.string.authenticator_default_account_name)
        val body = context.getString(R.string.authenticator_item_edit_remove_popup_body, name)
        setMessage(body)
        setPositiveButton(R.string.authenticator_item_edit_remove_popup_positive_button) { _, _ ->
            val packageName = linkedServices.getAllLinkedPackageName().firstOrNull()
            val domain = TrackingLogUtils.createDomainForLog(topDomain, packageName)
            AuthenticatorLogger(HermesComponent(context).logRepository)
                .setup(professional, domain)
                .logRemove2fa(issuer)
            
            confirmCallback.invoke()
        }
        setNegativeButton(R.string.authenticator_item_edit_remove_popup_negative_button) { _, _ -> }
    }.show().apply { withCenteredButtons() }