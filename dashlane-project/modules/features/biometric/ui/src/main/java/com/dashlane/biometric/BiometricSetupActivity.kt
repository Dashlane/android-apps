package com.dashlane.biometric

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hardwaresecurity.SecurityHelper.IntentRegistry
import com.dashlane.util.Toaster
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BiometricSetupActivity : AppCompatActivity() {
    @Inject
    lateinit var toaster: Toaster

    companion object {
        const val IS_BIOMETRIC_RECOVERY = "is_biometric_recovery"
        const val EXTRA_IS_SUCCESSFUL = "is_successful"

        fun newIntent(context: Context, isBiometricRecovery: Boolean) =
            Intent(context, BiometricSetupActivity::class.java)
                .also { it.putExtra(IS_BIOMETRIC_RECOVERY, isBiometricRecovery) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DashlaneTheme {
                BiometricSetupScreen(
                    viewModel = hiltViewModel(),
                    onCancel = { finish() },
                    showToast = { toaster.show(it, Toast.LENGTH_LONG) },
                    goToSettings = {
                        IntentRegistry.enableBiometricsIntentSequence
                            .find { packageManager.queryIntentActivities(it, 0).isNotEmpty() }
                            ?.let { startActivity(it) }
                            ?: run { finish() }
                    },
                    onSuccess = {
                        setResult(RESULT_OK)
                        finish()
                    },
                )
            }
        }
    }
}