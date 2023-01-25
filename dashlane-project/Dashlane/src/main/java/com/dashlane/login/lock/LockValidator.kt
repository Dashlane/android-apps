package com.dashlane.login.lock

import com.dashlane.session.SessionManager
import com.dashlane.session.userKeyBytes
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.util.hardwaresecurity.CryptoObjectHelper
import javax.inject.Inject

class LockValidator @Inject constructor(
    private val sessionManager: SessionManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val cryptoObjectHelper: CryptoObjectHelper
) {
    private val expectedPin: String?
        get() = sessionManager.session?.let(userSecureStorageManager::readPin)

    fun check(pass: LockPass): Boolean {
        return when (pass) {
            is LockPass.PinPass -> checkPin(pass)
            is LockPass.BiometricPass -> checkBiometric(pass)
            is LockPass.PasswordPass -> checkPassword(pass)
        }
    }

    private fun checkPin(pass: LockPass.PinPass): Boolean = isInputValid(pass.pin) && pass.pin == expectedPin

    private fun checkPassword(pass: LockPass.PasswordPass): Boolean =
        sessionManager.session?.appKey?.userKeyBytes == pass.appKey.userKeyBytes

    private fun checkBiometric(pass: LockPass.BiometricPass): Boolean {
        pass.cryptoObject?.cipher?.let {
            cryptoObjectHelper.challengeAuthentication(it)
        }
        
        return true
    }

    private fun isInputValid(userInput: String): Boolean {
        return userInput.length == PIN_CODE_LENGTH
    }

    companion object {
        const val PIN_CODE_LENGTH: Int = 4
    }
}