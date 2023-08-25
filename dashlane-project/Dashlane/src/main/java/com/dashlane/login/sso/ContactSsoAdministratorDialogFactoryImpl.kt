package com.dashlane.login.sso

import android.app.Activity
import com.dashlane.R
import com.dashlane.ui.util.DialogHelper
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ContactSsoAdministratorDialogFactoryImpl @Inject constructor(
    private val activity: Activity
) : ContactSsoAdministratorDialogFactory {
    override fun show(onDismiss: (() -> Unit)?) = DialogHelper().builder(activity)
        .setMessage(R.string.sso_contact_administrator_message)
        .setPositiveButton(android.R.string.ok, null)
        .setOnDismissListener { onDismiss?.invoke() }
        .create()
        .also {
            it.show()
        }
}