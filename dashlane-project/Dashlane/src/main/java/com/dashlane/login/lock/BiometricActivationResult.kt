package com.dashlane.login.lock

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.biometric.BiometricSetupActivity
import com.dashlane.util.clearTop

class BiometricActivationResult : ActivityResultContract<Unit, Boolean>() {
    override fun createIntent(
        context: Context,
        input: Unit,
    ): Intent =
        BiometricSetupActivity.newIntent(context = context, isBiometricRecovery = false).clearTop()

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Boolean {
        return resultCode == Activity.RESULT_OK &&
            intent?.getBooleanExtra(BiometricSetupActivity.EXTRA_IS_SUCCESSFUL, false) == true
    }
}