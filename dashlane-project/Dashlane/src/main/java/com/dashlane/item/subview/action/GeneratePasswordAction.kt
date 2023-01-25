package com.dashlane.item.subview.action

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.item.subview.Action
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.util.DeviceUtils
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



class GeneratePasswordAction(
    private val domain: String,
    private val origin: String,
    private val passwordChosenAction: (VaultItem<SyncObject.GeneratedPassword>) -> Unit = {}
) : Action {

    override val icon: Int = -1

    override val tintColorRes: Int? = null

    override val text: Int = R.string.generate

    override fun onClickAction(activity: AppCompatActivity) {
        DeviceUtils.hideKeyboard(activity)
        if (activity.supportFragmentManager.findFragmentByTag(DIALOG_PASSWORD_GENERATOR) != null) return
        NotificationDialogFragment.Builder()
            .setPositiveButtonText(activity, R.string.use)
            .setNegativeButtonText(activity, R.string.cancel)
            .setClickNegativeOnCancel(true)
            .build(setupDialog(activity))
            .show(activity.supportFragmentManager, DIALOG_PASSWORD_GENERATOR)
    }

    

    fun setupDialog(activity: AppCompatActivity): PasswordGeneratorDialog {
        
        val dialog =
            activity.supportFragmentManager.findFragmentByTag(DIALOG_PASSWORD_GENERATOR) as? PasswordGeneratorDialog
                ?: PasswordGeneratorDialog()
        return dialog.apply {
            setPasswordCallback { generatedPassword ->
                passwordChosenAction.invoke(generatedPassword)
            }
            setOrigin(origin)
            setDomainAsking(domain)
        }
    }

    companion object {
        private const val DIALOG_PASSWORD_GENERATOR = "PASSWORD_GENERATOR_POPUP"
    }
}