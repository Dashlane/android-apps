package com.dashlane.ui.screens.activities.onboarding.hardwareauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.core.KeyChainHelper
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Mode
import com.dashlane.hermes.generated.definitions.Reason
import com.dashlane.hermes.generated.events.user.AskAuthentication
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.notificationcenter.NotificationCenterRepositoryImpl
import com.dashlane.notificationcenter.view.ActionItemType
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.SecurityHelper
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.Toaster
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.hardwaresecurity.BiometricActivationStatus
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HardwareAuthActivationActivity : DashlaneActivity() {
    @Inject
    lateinit var biometricAuthModule: BiometricAuthModule

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var lockRepository: LockRepository

    @Inject
    lateinit var mKeyChainHelper: KeyChainHelper

    @Inject
    lateinit var securityHelper: SecurityHelper

    @Inject
    lateinit var sessionCredentialsSaver: SessionCredentialsSaver

    @Inject
    lateinit var toaster: Toaster

    @Inject
    lateinit var logRepository: LogRepository

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    @Inject
    lateinit var lockManager: LockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lockManager.startAutoLockGracePeriod()
        sessionManager.session?.let { mKeyChainHelper.initializeKeyStoreIfNeeded(it.userId) }
    }

    override fun onStart() {
        super.onStart()
        when (biometricAuthModule.getBiometricActivationStatus()) {
            BiometricActivationStatus.NOT_ENABLED -> securityHelper.showPopupBiometricsRequired(this, ::finish)
            BiometricActivationStatus.INSUFFICIENT_STRENGTH -> securityHelper.showPopupStrongerBiometricsRequired(this, ::finish)
            BiometricActivationStatus.ENABLED_STRONG,
            BiometricActivationStatus.ENABLED_WEAK -> Unit 
        }
    }

    override fun onResume() {
        super.onResume()
        if (!biometricAuthModule.isHardwareSetUp()) return
        val username = sessionManager.session?.userId ?: return
        lifecycleScope.launch {
            
            
            delay(resources!!.getInteger(android.R.integer.config_mediumAnimTime).toLong())
            if (biometricAuthModule.createEncryptionKeyForBiometrics(username = username)) {
                showBiometricPrompt(username)
            } else {
                
                showRegisterDialog("0x00002")
            }
        }
    }

    private fun processAuthenticationResult(result: BiometricAuthModule.Result) {
        sessionManager.session?.takeIf {
            result !is BiometricAuthModule.Result.StrongBiometricSuccess &&
                result !is BiometricAuthModule.Result.WeakBiometricSuccess &&
                result !is BiometricAuthModule.Result.AuthenticationFailure
        }
            ?.let { biometricAuthModule.deleteEncryptionKeyForBiometrics(it.userId) }

        when (result) {
            is BiometricAuthModule.Result.StrongBiometricSuccess,
            is BiometricAuthModule.Result.WeakBiometricSuccess -> {
                
                runCatching { sessionManager.session?.let(sessionCredentialsSaver::saveCredentials) }

                
                lockManager.setLockType(LockTypeManager.LOCK_TYPE_BIOMETRIC)

                
                NotificationCenterRepositoryImpl.setDismissed(
                    userPreferencesManager,
                    ActionItemType.BIOMETRIC.trackingKey,
                    false
                )

                setResultOkAndFinish(isSuccessful = true)
            }

            is BiometricAuthModule.Result.Canceled -> finish()
            is BiometricAuthModule.Result.Error -> {
                toaster.show(
                    biometricAuthModule.getMessageForErrorCode(this, result.error.code, result.error.message),
                    Toast.LENGTH_SHORT
                )
                setResultOkAndFinish(isSuccessful = false)
            }

            is BiometricAuthModule.Result.BiometricNotEnrolled -> showRegisterDialog("0x00001")
            is BiometricAuthModule.Result.SecurityHasChanged,
            is BiometricAuthModule.Result.AuthenticationFailure -> {
                toaster.show(R.string.error_during_hardware_authentication, Toast.LENGTH_LONG)
            }

            else -> Unit
        }
    }

    override fun onPause() {
        super.onPause()
        biometricAuthModule.stopHardwareAuthentication()
    }

    private fun showRegisterDialog(errorMessageId: String) {
        val messageResId = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            R.string.onboarding_dialog_hardware_registeration_module_google_fp_title_fingeronly
        } else {
            R.string.onboarding_dialog_hardware_registeration_module_google_fp_title
        }
        val message = getString(messageResId) + "\n" + errorMessageId
        DialogHelper()
            .builder(this)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(R.string.onboarding_dialog_hardware_registeration_module_google_fp_button) { _, _ ->
                val lockManager = lockRepository.getLockManager(sessionManager.session!!)
                lockManager.startAutoLockGracePeriod()
                try {
                    lockManager.startAutoLockGracePeriod()
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    startActivity(intent)
                } catch (e: Exception) {
                }
            }
            .setOnCancelListener { finish() }
            .show()
    }

    private suspend fun showBiometricPrompt(username: String) {
        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.onboarding_dialog_auth_required_hardware_module_google_fp_title))
            .setNegativeButtonText(getString(R.string.onboarding_dialog_auth_required_hardware_module_google_fp_button))
            .setConfirmationRequired(false)

        logRepository.queueEvent(AskAuthentication(Mode.BIOMETRIC, Reason.EDIT_SETTINGS))

        biometricAuthModule.startHardwareAuthentication(
            activity = this@HardwareAuthActivationActivity,
            username = username,
            promptInfoBuilder = promptInfoBuilder
        )
            .collect { result -> processAuthenticationResult(result) }
    }

    private fun setResultOkAndFinish(isSuccessful: Boolean) {
        intent.getParcelableExtraCompat<Intent>(SUCCESS_INTENT).takeIf { isSuccessful }?.let { startActivity(it) }

        val data = Intent().putExtra(EXTRA_IS_SUCCESSFUL, isSuccessful)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {
        private const val SUCCESS_INTENT = "success_intent"
        const val EXTRA_IS_SUCCESSFUL = "is_successful"

        fun newIntent(context: Context, successIntent: Intent? = null) =
            Intent(context, HardwareAuthActivationActivity::class.java).also {
                it.putExtra(
                    SUCCESS_INTENT,
                    successIntent
                )
            }
    }

    class ResultContract : ActivityResultContract<Intent?, Boolean>() {
        override fun createIntent(context: Context, input: Intent?) =
            Intent(context, HardwareAuthActivationActivity::class.java).also {
                it.putExtra(
                    SUCCESS_INTENT,
                    input
                )
            }

        override fun parseResult(resultCode: Int, intent: Intent?) =
            resultCode == RESULT_OK && intent != null && intent.getBooleanExtra(EXTRA_IS_SUCCESSFUL, false)
    }
}
