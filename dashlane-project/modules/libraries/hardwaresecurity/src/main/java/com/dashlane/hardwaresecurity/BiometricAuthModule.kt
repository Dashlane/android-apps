package com.dashlane.hardwaresecurity

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.user.Username
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import java.security.ProviderException
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthModule @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val cryptoObjectHelper: CryptoObjectHelper,
    private val biometricManager: BiometricManager,
) {

    private var biometricPrompt: BiometricPrompt? = null

    private var executor: CancellableHandlerExecutor? = null

    fun isHardwareSupported(): Boolean =
        biometricManager.isCanAuthenticateHardwareExists(BIOMETRIC_STRONG or BIOMETRIC_WEAK)

    fun isOnlyWeakSupported(): Boolean =
        biometricManager.isCanAuthenticateHardwareExists(BIOMETRIC_WEAK) &&
            !biometricManager.isCanAuthenticateHardwareExists(BIOMETRIC_STRONG)

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

    fun startHardwareAuthentication(
        activity: FragmentActivity,
        username: String,
        promptInfoBuilder: BiometricPrompt.PromptInfo.Builder,
        cancelOnNegativeAction: Boolean = true
    ): Flow<Result> {
        val biometricActivationStatus = getBiometricActivationStatus()
        return when (val biometricStatus = checkBiometricStatus(username, biometricActivationStatus)) {
            is Result.BiometricEnrolled -> {
                val promptInfo =
                    promptInfoBuilder.setAllowedAuthenticators(getPromptAuthenticator(biometricActivationStatus))
                        .build()
                startBiometricPrompt(activity, promptInfo, biometricStatus.cipher, cancelOnNegativeAction)
            }

            else -> flowOf(biometricStatus)
        }
    }

    fun stopHardwareAuthentication() {
        biometricPrompt?.cancelAuthentication()
        executor?.cancel()
    }

    fun isFeatureEnabled(username: String): Boolean {
        return if (!isHardwareSetUp()) {
            false
        } else {
            preferencesManager[username].getBoolean(ConstantsPrefs.USE_GOOGLE_FINGERPRINT)
        }
    }

    fun enableFeature(username: Username) {
        if (!isHardwareSetUp()) return
        preferencesManager[username].apply {
            putBoolean(ConstantsPrefs.USE_GOOGLE_FINGERPRINT, true)
        }
    }

    fun disableFeature(username: Username) {
        if (!isHardwareSetUp()) return
        preferencesManager[username].apply {
            putBoolean(ConstantsPrefs.USE_GOOGLE_FINGERPRINT, false)
        }
    }

    fun getMessageForErrorCode(code: Int, message: CharSequence): String {
        val messageToUser: String
        val errorMessage = getErrorMessage(code)
        messageToUser = message.toString() + errorMessage
        return messageToUser
    }

    fun getPromptAuthenticator(biometricActivationStatus: BiometricActivationStatus): Int {
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

    fun checkBiometricStatus(username: String, biometricActivationStatus: BiometricActivationStatus): Result {
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

    @VisibleForTesting
    fun startBiometricPrompt(
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
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            if (cancelOnNegativeAction) {
                                trySendBlocking(Result.Canceled(false))
                            } else {
                                trySendBlocking(Result.UserCancelled)
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

        data object AuthenticationFailure : Result()

        data class Error(val error: BiometricError) : Result()

        data class Canceled(val userPressedBack: Boolean) : Result()

        data class StrongBiometricSuccess(val cryptoObject: BiometricPrompt.CryptoObject) : Result()

        data object WeakBiometricSuccess : Result()

        data class BiometricEnrolled(val cipher: Cipher?) : Result()

        data object BiometricNotEnrolled : Result()

        data object SecurityHasChanged : Result()

        data object UserCancelled : Result()
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

@VisibleForTesting
fun BiometricManager.isCanAuthenticateHardwareExists(authenticators: Int) =
    runCanAuthenticateCatching {
        canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    }

@VisibleForTesting
fun BiometricManager.isCanAuthenticateSuccess(authenticators: Int) =
    runCanAuthenticateCatching {
        canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

@VisibleForTesting
inline fun BiometricManager.runCanAuthenticateCatching(
    block: BiometricManager.() -> Boolean
): Boolean =
    try {
        block()
    } catch (e: IllegalArgumentException) {
        
        
        
        false
    } catch (e: ProviderException) {
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        false
    } catch (e: NoSuchFieldError) {
        
        
        
        
        
        
        
        
        
        
        
        
        false
    }