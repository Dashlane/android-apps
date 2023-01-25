package com.dashlane.login.sso

import androidx.appcompat.app.AlertDialog

interface ContactSsoAdministratorDialogFactory {
    fun show(onDismiss: (() -> Unit)? = null): AlertDialog
}