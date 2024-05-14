package com.dashlane.autofill.unlockfill

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.autofill.changepassword.view.AskChangePasswordViewProxy
import com.dashlane.autofill.changepassword.view.AutofillChangePasswordActivity
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

class AutofillAuthChangePasswordActivity : AutofillAuthActivity() {
    private val changePasswordActivityResultLauncher =
        registerForActivityResult(
            object : ActivityResultContract<Pair<AutoFillHintSummary, Boolean>, Pair<Int, Intent?>>() {
                override fun createIntent(
                    context: Context,
                    input: Pair<AutoFillHintSummary, Boolean>
                ): Intent = AutofillChangePasswordActivity.buildIntent(context, input.first, input.second)

                override fun parseResult(resultCode: Int, intent: Intent?): Pair<Int, Intent?> = resultCode to intent
            }
        ) { (resultCode, intent) ->
            onChangePasswordActivityResult(resultCode, intent)
        }

    private val askChangePasswordViewProxy: AskChangePasswordViewProxy by lazy(LazyThreadSafetyMode.NONE) {
        AskChangePasswordViewProxy(
            this,
            summary!!,
            forKeyboardAutofill,
            changePasswordActivityResultLauncher
        ) {
            dealWithSecurityWarning(it)
        }
    }

    private fun onChangePasswordActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) finish() else finishAndTransferResult(data!!)
    }

    override fun changePasswordShowDialog(unlockedAuthentifiant: UnlockedAuthentifiant) {
        askChangePasswordViewProxy.showDialog(unlockedAuthentifiant)
    }
}