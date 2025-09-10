package com.dashlane.hardwaresecurity

import androidx.annotation.VisibleForTesting
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt.CryptoObject
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.user.Username
import java.security.ProviderException
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthModule @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val cryptoObjectHelper: CryptoObjectHelper,
    private val biometricManager: BiometricManager,
) {
    var currentCryptoObject: CryptoObject? = null
        private set
        get() = field.also {
            
            currentCryptoObject = null
        }

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

    fun getPromptAuthenticator(biometricActivationStatus: BiometricActivationStatus): Int? {
        return when (biometricActivationStatus) {
            BiometricActivationStatus.ENABLED_WEAK -> BIOMETRIC_WEAK
            BiometricActivationStatus.ENABLED_STRONG -> BIOMETRIC_STRONG
            else -> null
        }
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

    fun createCryptoObjectAndPromptAuthenticators(email: String): Pair<CryptoObject?, Int?> {
        val activationStatus = getBiometricActivationStatus()
        val biometricStatus = checkBiometricStatus(email, activationStatus)
        val promptAuthenticators = getPromptAuthenticator(activationStatus)
        if (biometricStatus !is Result.BiometricEnrolled) {
            currentCryptoObject = null
            return null to promptAuthenticators
        }
        val cryptoObject = biometricStatus.cipher?.let { CryptoObject(it) }
        currentCryptoObject = cryptoObject
        return cryptoObject to promptAuthenticators
    }

    companion object {
        const val TAG = "BiometricAuthModule"
    }

    sealed class Result {
        data class BiometricEnrolled(val cipher: Cipher?) : Result()

        data object BiometricNotEnrolled : Result()

        data object SecurityHasChanged : Result()
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
    block: BiometricManager.() -> Boolean,
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