package com.dashlane.login.lock

import com.dashlane.common.logger.developerinfo.DeveloperInfoLogger
import com.dashlane.session.SessionManager
import com.dashlane.crypto.keys.userKeyBytes
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.hardwaresecurity.CryptoObjectHelper
import javax.inject.Inject

class LockValidator @Inject constructor(
    private val sessionManager: SessionManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val cryptoObjectHelper: CryptoObjectHelper,
    private val developerInfoLogger: DeveloperInfoLogger
) {
    private val expectedPin: String?
        get() = sessionManager.session?.let { userSecureStorageManager.readPin(it.localKey, it.username) }

    fun check(pass: LockPass): Boolean {
        return when (pass) {
            is LockPass.PinPass -> checkPin(pass)
            is LockPass.BiometricPass -> checkBiometric(pass)
            is LockPass.PasswordPass -> checkPassword(pass)
            is LockPass.WeakBiometricPass -> true
        }
    }

    private fun checkPin(pass: LockPass.PinPass): Boolean = isInputValid(pass.pin) && pass.pin == expectedPin

    private fun checkPassword(pass: LockPass.PasswordPass): Boolean =
        sessionManager.session?.appKey?.userKeyBytes == pass.appKey.userKeyBytes

    private fun checkBiometric(pass: LockPass.BiometricPass): Boolean {
        return pass.cryptoObject.cipher
            ?.let {
                cryptoObjectHelper.challengeAuthentication(it) { exceptionType ->
                    
                    
                    
                    developerInfoLogger.log(
                        action = "authentication_challenge_error",
                        message = "Cryptographic Key is not properly stored",
                        exceptionType = exceptionType
                    )
                }
            }
            ?.let { it is CryptoObjectHelper.CryptoChallengeResult.Success }
            ?: false
    }

    private fun isInputValid(userInput: String): Boolean {
        return userInput.length == PIN_CODE_LENGTH
    }

    companion object {
        const val PIN_CODE_LENGTH: Int = 4
    }
}