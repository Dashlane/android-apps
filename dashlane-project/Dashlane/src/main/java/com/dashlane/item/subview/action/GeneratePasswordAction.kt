package com.dashlane.item.subview.action

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.ui.action.Action
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog
import com.dashlane.util.DeviceUtils

class GeneratePasswordAction(
    private val domain: String,
    private val origin: String,
    private val passwordChosenAction: (String?, String) -> Unit
) : Action {

    override val icon: Int = -1

    override val tintColorRes: Int? = null

    override val text: Int = R.string.generate

    override fun onClickAction(activity: AppCompatActivity) {
        DeviceUtils.hideKeyboard(activity)
        if (activity.supportFragmentManager.findFragmentByTag(DIALOG_PASSWORD_GENERATOR) != null) return

        
        activity.supportFragmentManager.setFragmentResultListener(
            PasswordGeneratorDialog.PASSWORD_GENERATOR_REQUEST_KEY,
            activity
        ) { _, bundle ->
            val id = bundle.getString(PasswordGeneratorDialog.PASSWORD_GENERATOR_RESULT_ID)
            val password = bundle.getString(PasswordGeneratorDialog.PASSWORD_GENERATOR_RESULT_PASSWORD, "")
            passwordChosenAction.invoke(id, password)
        }

        
        setupDialog(activity)
            .show(activity.supportFragmentManager, DIALOG_PASSWORD_GENERATOR)
    }

    private fun setupDialog(activity: AppCompatActivity): PasswordGeneratorDialog {
        
        return activity.supportFragmentManager.findFragmentByTag(DIALOG_PASSWORD_GENERATOR) as? PasswordGeneratorDialog
            ?: PasswordGeneratorDialog.newInstance(activity, origin, domain)
    }

    companion object {
        private const val DIALOG_PASSWORD_GENERATOR = "PASSWORD_GENERATOR_POPUP"
    }
}