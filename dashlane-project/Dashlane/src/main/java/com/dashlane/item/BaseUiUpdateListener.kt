package com.dashlane.item

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.dashlane.R
import com.dashlane.item.subview.ItemSubView
import com.dashlane.navigation.Navigator
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.util.DialogHelper

abstract class BaseUiUpdateListener(private val activity: AppCompatActivity, private val navigator: Navigator) :
    ItemEditViewContract.View.UiUpdateListener {

    private val nfcDialog: DialogFragment?
        get() = activity.supportFragmentManager.findFragmentByTag(NFC_DIALOG_TAG) as? DialogFragment
    private val nfcErrorDialog: DialogFragment?
        get() = activity.supportFragmentManager.findFragmentByTag(NFC_DIALOG_ERROR_TAG) as? DialogFragment

    override fun notifyNotEnoughDataToSave(@StringRes message: Int) {
        showSaveError(message)
    }

    override fun showNfcPromptDialog(dismissAction: () -> Unit) {
        if (activity.supportFragmentManager.findFragmentByTag(NFC_DIALOG_SUCCESS_TAG) != null) {
            
            return
        }
        nfcErrorDialog?.dismissAllowingStateLoss()
        NotificationDialogFragment.Builder().setTitle(activity.getString(R.string.nfc_creditcard_prompt_title))
            .setCustomViewWithBigIcon(R.drawable.nfc_creditcard_scan_logo)
            .setMessage(activity.getString(R.string.nfc_creditcard_prompt_message))
            .setPositiveButtonText(activity.getString(R.string.nfc_creditcard_prompt_positive_button))
            .setCancelable(true)
            .setClickPositiveOnCancel(true)
            .setClicker(object : NotificationDialogFragment.TwoButtonClicker {
                override fun onPositiveButton() {
                    dismissAction.invoke()
                }

                override fun onNegativeButton() = Unit
            })
            .build()
            .show(activity.supportFragmentManager, NFC_DIALOG_TAG)
    }

    override fun showNfcErrorDialog() {
        if (activity.supportFragmentManager.findFragmentByTag(NFC_DIALOG_SUCCESS_TAG) != null) {
            
            return
        }
        nfcDialog?.dismissAllowingStateLoss()
        NotificationDialogFragment.Builder().setTitle(activity.getString(R.string.nfc_creditcard_prompt_error_title))
            .setMessage(activity.getString(R.string.nfc_creditcard_prompt_error_message))
            .setPositiveButtonText(activity.getString(R.string.nfc_creditcard_prompt_positive_button))
            .setCancelable(true)
            .setClickPositiveOnCancel(true)
            .setClicker(object : NotificationDialogFragment.TwoButtonClicker {
                override fun onPositiveButton() = Unit

                override fun onNegativeButton() = Unit
            })
            .build()
            .show(activity.supportFragmentManager, NFC_DIALOG_ERROR_TAG)
    }

    override fun showNfcSuccessDialog(subviewToFocus: ItemSubView<*>?, dismissAction: () -> Unit) {
        nfcDialog?.dismissAllowingStateLoss()
        nfcErrorDialog?.dismissAllowingStateLoss()
        NotificationDialogFragment.Builder().setTitle(
            activity.getString(R.string.nfc_creditcard_success_title)
        )
            .setMessage(
                activity.getString(R.string.nfc_creditcard_success_message)
            )
            .setPositiveButtonText(
                activity.getString(R.string.nfc_creditcard_success_positive_button)
            )
            .setClicker(object : NotificationDialogFragment.TwoButtonClicker {
                override fun onPositiveButton() {
                    subviewToFocus?.let {
                        requestFocus(it)
                    }
                    dismissAction.invoke()
                }

                override fun onNegativeButton() {
                    subviewToFocus?.let {
                        requestFocus(it)
                    }
                    dismissAction.invoke()
                }
            })
            .setCancelable(true)
            .setClickPositiveOnCancel(true)
            .build()
            .show(activity.supportFragmentManager, NFC_DIALOG_SUCCESS_TAG)
    }

    override fun showRestorePromptDialog() {
        DialogHelper().builder(activity, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
            .setTitle(activity.getString(R.string.infobox_restore_dialog_title))
            .setMessage(activity.getString(R.string.infobox_restore_dialog_message))
            .setPositiveButton(activity.getString(R.string.infobox_restore_dialog_positive_button)) { _, _ -> notifyRestorePassword() }
            .setNegativeButton(activity.getString(R.string.infobox_restore_dialog_negative_button)) { _, _ -> }
            .setCancelable(true)
            .show()
    }

    override fun openLinkedServices(
        itemId: String,
        fromViewOnly: Boolean,
        addNew: Boolean,
        temporaryWebsites: List<String>,
        temporaryApps: List<String>?,
        urlDomain: String?
    ) {
        navigator.goToLinkedWebsites(itemId, fromViewOnly, addNew, temporaryWebsites, temporaryApps, urlDomain)
    }

    

    abstract fun requestFocus(subview: ItemSubView<*>)

    private fun showSaveError(@StringRes message: Int) {
        NotificationDialogFragment.Builder().setTitle(activity.getString(R.string.error))
            .setMessage(activity.getString(message))
            .setPositiveButtonText(activity.getString(R.string.ok))
            .setCancelable(true)
            .setClickPositiveOnCancel(false)
            .build()
            .show(activity.supportFragmentManager, "tag_error_input_dialog")
    }

    companion object {
        const val NFC_DIALOG_TAG = "fragment_tag_nfc_dialog"
        const val NFC_DIALOG_ERROR_TAG = "fragment_tag_nfc_error_dialog"
        const val NFC_DIALOG_SUCCESS_TAG = "fragment_tag_nfc_success_dialog"
        const val BOTTOM_SHEET_DIALOG_TAG = "fragment_tag_bottom_sheet_dialog"
    }
}