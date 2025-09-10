package com.dashlane.item

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.item.subview.ItemSubView
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment

abstract class BaseUiUpdateListener(private val activity: AppCompatActivity) :
    ItemEditViewContract.View.UiUpdateListener {
    override fun notifyNotEnoughDataToSave(
        @StringRes message: Int,
    ) {
        showSaveError(message)
    }

    abstract fun requestFocus(subview: ItemSubView<*>)

    private fun showSaveError(
        @StringRes message: Int,
    ) {
        NotificationDialogFragment.Builder().setTitle(activity.getString(R.string.error))
            .setMessage(activity.getString(message))
            .setPositiveButtonText(activity.getString(R.string.ok))
            .setCancelable(true)
            .setClickPositiveOnCancel(false)
            .build()
            .show(activity.supportFragmentManager, "tag_error_input_dialog")
    }

    companion object {
        const val BOTTOM_SHEET_DIALOG_TAG = "fragment_tag_bottom_sheet_dialog"
    }
}