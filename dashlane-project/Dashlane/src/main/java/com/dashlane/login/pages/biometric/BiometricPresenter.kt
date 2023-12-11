package com.dashlane.login.pages.biometric

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.LoginLockBasePresenter
import com.dashlane.login.root.LoginPresenter
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.Toaster
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.showToaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BiometricPresenter(
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val loginLogger: LoginLogger,
    rootPresenter: LoginPresenter,
    coroutineScope: CoroutineScope,
    lockManager: LockManager,
    toaster: Toaster
) : LoginLockBasePresenter<BiometricContract.DataProvider, BiometricContract.ViewProxy>(
    rootPresenter = rootPresenter,
    coroutineScope = coroutineScope,
    lockManager = lockManager,
    toaster = toaster
),
BiometricContract.Presenter {

    override val lockTypeName: Int = LockTypeManager.LOCK_TYPE_BIOMETRIC

    override fun onNextClicked() {
        
    }

    @SuppressWarnings("UNUSED")
    private val lifecycleObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    
                    
                    coroutineScope.launch(Dispatchers.Main) {
                        
                        
                        delay(resources!!.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                        startListeningForHardwareAuthentication()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> stopListeningForHardwareAuthentication()
                Lifecycle.Event.ON_DESTROY -> (activity as? DashlaneActivity)?.lifecycle?.removeObserver(this)
                else -> {
                    
                }
            }
        }
    }

    override fun onViewOrProviderChanged() {
        super.onViewOrProviderChanged()
        providerOrNull ?: return
        viewOrNull ?: return
        (activity as? DashlaneActivity)?.lifecycle?.addObserver(lifecycleObserver)

        if (provider.lockSetting.shouldThemeAsDialog) {
            activity?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        view.showEmail(provider.username)
    }

    override fun initView() {
        super.initView()
        if (provider.lockSetting.shouldThemeAsDialog) {
            activity?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun onUnlock() {
        val result = provider.onUnlockSuccess()

        listOfNotNull(
            provider.createNextActivityIntent(),
            provider.createMasterPasswordResetIntroActivityIntent()
        ).takeUnless { it.isEmpty() }?.let {
            activity?.startActivities(it.toTypedArray())
        }

        activity?.setResult(Activity.RESULT_OK, result)
        activity?.finish()
    }

    private fun startListeningForHardwareAuthentication() {
        val fragmentActivity = activity as? FragmentActivity ?: return
        val biometricPromptFlow = if (provider.lockSetting.isLockCancelable) {
            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(
                    provider.lockSetting.topicLock ?: fragmentActivity.getString(R.string.window_biometric_unlock_hardware_module_google_fp_title)
                )
                .setSubtitle(provider.lockSetting.subTopicLock ?: provider.username)
                .setNegativeButtonText(fragmentActivity.getString(R.string.cancel))
                .setConfirmationRequired(false)
            provider.biometricAuthModule.startHardwareAuthentication(
                activity = fragmentActivity,
                username = provider.username,
                promptInfoBuilder = promptInfoBuilder
            )
        } else {
            provider.biometricAuthModule.startHardwareAuthentication(activity = fragmentActivity)
        }

        fragmentActivity.lifecycleScope.launch {
            biometricPromptFlow.collect { result -> processAuthenticationResult(result) }
        }
    }

    private fun stopListeningForHardwareAuthentication() {
        provider.biometricAuthModule.stopHardwareAuthentication()
    }

    private fun processAuthenticationResult(result: BiometricAuthModule.Result) {
        when (result) {
            is BiometricAuthModule.Result.StrongBiometricSuccess -> {
                if (provider.challengeAuthentication(result.cryptoObject)) {
                    loginLogger.logSuccess(loginMode = LoginMode.Biometric)
                    onUnlock()
                } else {
                    forceLogout(null)
                }
            }
            is BiometricAuthModule.Result.WeakBiometricSuccess -> {
                if (provider.unlockWeakBiometric()) {
                    loginLogger.logSuccess(loginMode = LoginMode.Biometric)
                    onUnlock()
                } else {
                    forceLogout(null)
                }
            }
            is BiometricAuthModule.Result.UserCancelled -> {
                (activity as? DashlaneActivity)?.lifecycle?.removeObserver(lifecycleObserver)
                rootPresenter.onBiometricNegativeClicked()
            }
            is BiometricAuthModule.Result.Canceled -> onHardwareAuthenticationCanceled(result.userPressedBack)
            is BiometricAuthModule.Result.AuthenticationFailure -> {
                loginLogger.logWrongBiometric()
                lockManager.addFailUnlockAttempt()
                if (!lockManager.hasFailedUnlockTooManyTimes()) {
                    startListeningForHardwareAuthentication()
                } else {
                    forceLogout(null)
                }
            }
            is BiometricAuthModule.Result.Error -> processAuthenticationError(result.error)
            is BiometricAuthModule.Result.BiometricEnrolled -> Unit
            BiometricAuthModule.Result.BiometricNotEnrolled,
            BiometricAuthModule.Result.SecurityHasChanged -> {
                loginLogger.logErrorUnknown(loginMode = LoginMode.Biometric)
                
                
                val context = activity?.applicationContext

                if (context != null) {
                    lockManager.setLockType(LockTypeManager.LOCK_TYPE_MASTER_PASSWORD)
                    sessionManager.session?.username.let(sessionCredentialsSaver::deleteSavedCredentials)
                    userPreferencesManager.putBoolean(ConstantsPrefs.INVALIDATED_BIOMETRIC, true)
                }
                forceLogout(null)
            }
        }
    }

    private fun processAuthenticationError(error: BiometricAuthModule.BiometricError) {
        when (error) {
            is BiometricAuthModule.BiometricError.CryptoObjectError,
            is BiometricAuthModule.BiometricError.HardwareLockout -> forceLogout(error.message)
            is BiometricAuthModule.BiometricError.HardwareError -> {
                loginLogger.logErrorUnknown(loginMode = LoginMode.Biometric)
                view.context.showToaster(
                    provider.biometricAuthModule.getMessageForErrorCode(view.context, error.code, error.message),
                    Toast.LENGTH_SHORT
                )
                onHardwareAuthenticationCanceled(false)
            }
        }
    }

    private fun onHardwareAuthenticationCanceled(userPressedBack: Boolean) {
        if (!provider.lockSetting.isLockCancelable) {
            if (userPressedBack) {
                activity?.setResult(Activity.RESULT_CANCELED)
                activity?.finish()
                return
            }
        }
        rootPresenter.onPrimaryFactorCancelOrLogout()
    }

    private fun forceLogout(errorMessage: CharSequence?) {
        if (provider.isAccountTypeMasterPassword()) {
            logoutTooManyAttempts(errorMessage)
        } else {
            
            provider.biometricAuthModule.stopHardwareAuthentication()
            rootPresenter.onBiometricNegativeClicked()
        }
    }
}
