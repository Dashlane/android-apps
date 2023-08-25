
package com.dashlane.util.hardwaresecurity

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.biometricrecovery.MasterPasswordResetIntroActivity
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.DashlaneIntent
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.screens.activities.onboarding.hardwareauth.HardwareAuthActivationActivity
import com.dashlane.ui.screens.activities.onboarding.hardwareauth.OnboardingHardwareAuthActivity
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.singleTop
import dagger.Lazy
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import java.security.ProviderException
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.inject.Inject

class BiometricAuthModule @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val cryptoObjectHelper: CryptoObjectHelper,
    private val biometricRecovery: Lazy<BiometricRecovery>,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val accountStorage: UserAccountStorage,
    private val biometricManager: BiometricManager
) {

    var referrer: String? = null
        get() = if (field == null) UsageLogConstant.LockSubAction.fromApp else field
    val logger = AuthModuleLogger(ConstantsPrefs.USE_GOOGLE_FINGERPRINT, sessionManager, bySessionUsageLogRepository)
    private var biometricPrompt: BiometricPrompt? = null

    private var executor: CancellableHandlerExecutor? = null

    private val sso get() = sessionManager.session?.let { accountStorage[it.username]?.sso } == true

    fun isHardwareSupported(): Boolean = biometricManager.isCanAuthenticateHardwareExists(BIOMETRIC_STRONG or BIOMETRIC_WEAK)

    fun getBiometricActivationStatus(): BiometricActivationStatus {
        val enabledStrong = biometricManager.isCanAuthenticateSuccess(BIOMETRIC_STRONG)
        val enabledWeak = biometricManager.isCanAuthenticateSuccess(BIOMETRIC_WEAK)
        val hasStrong = biometricManager.isCanAuthenticateHardwareExists(BIOMETRIC_STRONG)

        return when {
            enabledStrong -> BiometricActivationStatus.ENABLED_STRONG
            enabledWeak && hasStrong -> BiometricActivationStatus.INSUFFICIENT_STRENGTH
            enabledWeak -> BiometricActivationStatus.ENABLED_WEAK
            else -> BiometricActivationStatus.NOT_ENABLED
        }
    }

    fun isHardwareSetUp(): Boolean {
        val biometricActivationStatus = getBiometricActivationStatus()
        return biometricActivationStatus == BiometricActivationStatus.ENABLED_WEAK ||
            biometricActivationStatus == BiometricActivationStatus.ENABLED_STRONG
    }

    fun createEncryptionKeyForBiometrics(username: String): Boolean {
        if (getBiometricActivationStatus() != BiometricActivationStatus.ENABLED_STRONG) return true
        val keyStoreKey = CryptoObjectHelper.BiometricsSeal(username)
        return try {
            cryptoObjectHelper.createEncryptionKey(keyStoreKey = keyStoreKey, isUserAuthenticationRequired = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteEncryptionKeyForBiometrics(username: String) {
        cryptoObjectHelper.deleteEncryptionKey(keyStoreKey = CryptoObjectHelper.BiometricsSeal(username))
    }

    fun logRegisterBiometrics() {
        logger.logUsageStartRegisterProcess()
    }

    fun startHardwareAuthentication(activity: FragmentActivity): Flow<Result> {
        val username = sessionManager.session?.userId ?: return emptyFlow()

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.window_biometric_unlock_hardware_module_google_fp_title))
            .setNegativeButtonText(activity.getString(if (sso) R.string.sso_lock_use_sso else R.string.fragment_lock_pin_button_use_master_password))
            .setConfirmationRequired(false)
            .setSubtitle(username)

        return startHardwareAuthentication(
            activity = activity,
            username = username,
            promptInfoBuilder = promptInfoBuilder,
            cancelOnNegativeAction = false
        )
    }

    fun startHardwareAuthentication(
        activity: FragmentActivity,
        username: String,
        promptInfoBuilder: BiometricPrompt.PromptInfo.Builder,
        cancelOnNegativeAction: Boolean = true
    ): Flow<Result> {
        val biometricActivationStatus = getBiometricActivationStatus()
        return when (val biometricStatus = checkBiometricStatus(username, biometricActivationStatus)) {
            is Result.BiometricEnrolled -> {
                val promptInfo = promptInfoBuilder.setAllowedAuthenticators(getPromptAuthenticator(biometricActivationStatus)).build()
                startBiometricPrompt(activity, promptInfo, biometricStatus.cipher, cancelOnNegativeAction)
            }
            else -> flowOf(biometricStatus)
        }
    }

    fun stopHardwareAuthentication() {
        biometricPrompt?.cancelAuthentication()
        executor?.cancel()
    }

    fun isFeatureEnabled(username: String? = null): Boolean {
        return if (!isHardwareSetUp()) {
            false
        } else {
            val prefs = if (username != null) {
                userPreferencesManager.preferencesFor(username)
            } else {
                userPreferencesManager
            }

            prefs.getBoolean(ConstantsPrefs.USE_GOOGLE_FINGERPRINT)
        }
    }

    fun enableFeature() {
        if (!isHardwareSetUp()) return
        userPreferencesManager.putBoolean(ConstantsPrefs.USE_GOOGLE_FINGERPRINT, true)
        logger.logUsageStartFeature()
    }

    fun disableFeature() {
        if (!isHardwareSetUp()) return
        userPreferencesManager.putBoolean(ConstantsPrefs.USE_GOOGLE_FINGERPRINT, false)
        logger.logUsageStopFeature()
    }

    fun startOnboarding(context: Context) {
        if (biometricRecovery.get().isFeatureAvailable) {
            val successIntent = MasterPasswordResetIntroActivity.newIntent(context)
            context.startActivity(HardwareAuthActivationActivity.newIntent(context, successIntent).singleTop())
        } else {
            context.startActivity(DashlaneIntent.newInstance(context, OnboardingHardwareAuthActivity::class.java))
            logger.logUsageStartOnboarding()
        }
    }

    fun getMessageForErrorCode(context: Context, code: Int, message: CharSequence?): String {
        val messageToUser: String
        val errorMessage = getErrorMessage(code)
        messageToUser = if (message == null) {
            context.getString(R.string.error_during_hardware_authentication) + errorMessage
        } else {
            message.toString() + errorMessage
        }
        return messageToUser
    }

    private fun getPromptAuthenticator(biometricActivationStatus: BiometricActivationStatus): Int {
        return when (biometricActivationStatus) {
            BiometricActivationStatus.NOT_ENABLED,
            BiometricActivationStatus.INSUFFICIENT_STRENGTH -> -1
            BiometricActivationStatus.ENABLED_WEAK -> BIOMETRIC_WEAK
            BiometricActivationStatus.ENABLED_STRONG -> BIOMETRIC_STRONG
        }
    }

    private fun getErrorMessage(code: Int): String {
        return " 0x$code"
    }

    private fun checkBiometricStatus(username: String, biometricActivationStatus: BiometricActivationStatus): Result {
        return when (biometricActivationStatus) {
            BiometricActivationStatus.ENABLED_WEAK -> Result.BiometricEnrolled(null)
            BiometricActivationStatus.ENABLED_STRONG -> {
                val cipherResult = cryptoObjectHelper.getEncryptCipher(CryptoObjectHelper.BiometricsSeal(username))
                return if (cipherResult !is CryptoObjectHelper.CipherInitResult.Success) {
                    when (cipherResult) {
                        is CryptoObjectHelper.CipherInitResult.InvalidatedKeyError -> {
                            
                            
                            Result.SecurityHasChanged
                        }
                        else -> Result.BiometricNotEnrolled
                    }
                } else {
                    Result.BiometricEnrolled(cipherResult.cipher)
                }
            }
            else -> Result.BiometricNotEnrolled
        }
    }

    private fun startBiometricPrompt(
        activity: FragmentActivity,
        promptInfo: BiometricPrompt.PromptInfo,
        cipher: Cipher?,
        cancelOnNegativeAction: Boolean = true
    ) = callbackFlow {
        executor = CancellableHandlerExecutor(activity.mainLooper)

        biometricPrompt = BiometricPrompt(
            activity,
            executor!!,
            object : BiometricPrompt.AuthenticationCallback() {
                @SuppressWarnings("SwitchIntDef")
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    
                    sessionManager.session?.let { logger.logUsageAuthFailure(referrer) }
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            if (cancelOnNegativeAction) {
                                trySendBlocking(Result.Canceled(false))
                            } else {
                                trySendBlocking(Result.UseMasterPasswordClicked)
                            }
                        }
                        BiometricPrompt.ERROR_USER_CANCELED -> trySendBlocking(Result.Canceled(true))
                        BiometricPrompt.ERROR_TIMEOUT -> trySendBlocking(Result.Canceled(false))
                        BiometricPrompt.ERROR_LOCKOUT, BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            biometricPrompt?.cancelAuthentication()
                            trySendBlocking(Result.Error(BiometricError.HardwareLockout(errorCode, errString.toString())))
                        }
                        BiometricPrompt.ERROR_CANCELED -> {
                            
                        }
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                            
                            trySendBlocking(Result.BiometricNotEnrolled)
                        }
                        else -> {
                            trySendBlocking(Result.Error(BiometricError.HardwareError(errorCode, errString.toString())))
                        }
                    }
                    close()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    trySendBlocking(Result.AuthenticationFailure)
                    
                    
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    logger.logUsageAuthSuccess(referrer)
                    
                    val cryptoObject = result.cryptoObject
                    if (cipher != null && cryptoObject == null) {
                        trySendBlocking(Result.AuthenticationFailure)
                    } else if (cryptoObject != null) {
                        trySendBlocking(Result.StrongBiometricSuccess(cryptoObject))
                    } else {
                        trySendBlocking(Result.WeakBiometricSuccess)
                    }
                    close()
                }
            }
        )
        
        cipher?.let {
            val cryptoObject = BiometricPrompt.CryptoObject(cipher)
            biometricPrompt?.authenticate(promptInfo, cryptoObject)
        } ?: run {
            biometricPrompt?.authenticate(promptInfo)
        }

        awaitClose {
            biometricPrompt?.cancelAuthentication()
        }
    }

    companion object {
        const val TAG = "BiometricAuthModule"
    }

    sealed class Result {

        object AuthenticationFailure : Result()

        data class Error(val error: BiometricError) : Result()

        data class Canceled(val userPressedBack: Boolean) : Result()

        data class StrongBiometricSuccess(val cryptoObject: BiometricPrompt.CryptoObject) : Result()

        object WeakBiometricSuccess : Result()

        data class BiometricEnrolled(val cipher: Cipher?) : Result()

        object BiometricNotEnrolled : Result()

        object SecurityHasChanged : Result()

        object UseMasterPasswordClicked : Result()
    }

    sealed class BiometricError {
        abstract val code: Int
        abstract val message: String

        data class HardwareError(
            override val code: Int,
            override val message: String
        ) : BiometricError()

        data class HardwareLockout(
            override val code: Int,
            override val message: String
        ) : BiometricError()

        data class CryptoObjectError(
            override val code: Int,
            override val message: String
        ) : BiometricError()
    }

    class CancellableHandlerExecutor(looper: Looper) : Executor {
        private val handler = Handler(looper)
        private var canceled = false
        private var commands = mutableListOf<Runnable>()

        override fun execute(command: Runnable) {
            if (!canceled) {
                commands.add(command)
                handler.post(command)
            }
        }

        fun cancel() {
            canceled = true
            commands.forEach {
                handler.removeCallbacks(it)
            }
        }
    }
}

private fun BiometricManager.isCanAuthenticateHardwareExists(authenticators: Int) =
    runCanAuthenticateCatchingWithDefault(false) {
        canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    }

private fun BiometricManager.isCanAuthenticateSuccess(authenticators: Int) =
    runCanAuthenticateCatchingWithDefault(false) {
        canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

private inline fun <T> BiometricManager.runCanAuthenticateCatchingWithDefault(
    value: T,
    block: BiometricManager.() -> T
): T =
    try {
        block()
    } catch (e: IllegalArgumentException) {
        
        
        
        value
    } catch (e: ProviderException) {
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        value
    } catch (e: NoSuchFieldError) {
        
        
        
        
        
        
        
        
        
        
        
        
        value
    }
