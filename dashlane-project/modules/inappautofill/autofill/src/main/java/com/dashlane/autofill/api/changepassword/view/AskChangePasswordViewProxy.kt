package com.dashlane.autofill.api.changepassword.view

import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.DialogFragment
import com.dashlane.autofill.api.changepassword.view.AskChangePasswordDialogFragment.Actions
import com.dashlane.autofill.api.ui.AutoFillResponseActivity
import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.vault.model.loginForUi

class AskChangePasswordViewProxy(
    private val autoFillResponseActivity: AutoFillResponseActivity,
    private val summary: AutoFillHintSummary,
    private val forKeyboard: Boolean,
    private val changePasswordActivityResultLauncher: ActivityResultLauncher<Pair<AutoFillHintSummary, Boolean>>,
    private val onDismiss: (UnlockedAuthentifiant) -> Unit
) : Actions {

    private var dialog: DialogFragment? = null
    private lateinit var unlockedAuthentifiant: UnlockedAuthentifiant

    fun showDialog(unlockedAuthentifiant: UnlockedAuthentifiant) {
        this.unlockedAuthentifiant = unlockedAuthentifiant
        val supportFragmentManager = autoFillResponseActivity.supportFragmentManager
        dialog = supportFragmentManager.findFragmentByTag(ASK_CHANGE_PASSWORD_TAG) as? DialogFragment
        if (dialog == null) {
            dialog = AskChangePasswordDialogFragment(unlockedAuthentifiant.authentifiantSummary.loginForUi!!, this)
            dialog?.show(supportFragmentManager, ASK_CHANGE_PASSWORD_TAG)
        }
    }

    override fun changePassword() {
        dialog?.dismiss()
        changePasswordActivityResultLauncher.launch(summary to forKeyboard)
    }

    override fun dismiss() {
        dialog?.dismiss()
        onDismiss.invoke(unlockedAuthentifiant)
    }

    companion object {
        private const val ASK_CHANGE_PASSWORD_TAG = "change_password_ask_dialog"
    }
}
