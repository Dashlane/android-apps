package com.dashlane.ui.screens.fragments.userdata.sharing

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.dashlane.R
import com.dashlane.ui.dialogs.fragment.WaiterDialogFragment
import com.dashlane.ui.util.DialogHelper

fun FragmentManager.showLoadingDialog(context: Context) {
    WaiterDialogFragment.dismissWaiter(this)
    WaiterDialogFragment.showWaiter(
        false,
        context.getString(R.string.contacting_dashlane),
        context.getString(R.string.contacting_dashlane),
        this
    )
}

fun FragmentManager.onSuccess() {
    WaiterDialogFragment.dismissWaiter(this)
}

fun FragmentManager.showErrorDialog(context: Context) {
    WaiterDialogFragment.dismissWaiter(this)
    DialogHelper()
        .builder(context)
        .setMessage(context.getString(R.string.ui_sharing_error_dialog_title))
        .setCancelable(true)
        .show()
}