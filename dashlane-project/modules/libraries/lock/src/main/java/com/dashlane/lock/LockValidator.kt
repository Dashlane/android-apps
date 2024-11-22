package com.dashlane.lock

import com.dashlane.crypto.keys.userKeyBytes
import com.dashlane.hardwaresecurity.CryptoObjectHelper
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.Session
import com.dashlane.storage.securestorage.UserSecureStorageManager
import javax.inject.Inject

class LockValidator @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val cryptoObjectHelper: CryptoObjectHelper,
) {
    fun check(session: Session, pass: LockPass): Boolean {
        return when (pass) {
            is LockPass.PinPass -> checkPin(session, pass)
            is LockPass.BiometricPass -> checkBiometric(pass)
            is LockPass.PasswordPass -> checkPassword(session, pass)
            is LockPass.WeakBiometricPass -> true
        }
    }

    private fun checkPin(session: Session, pass: LockPass.PinPass): Boolean {
        val expectedPin = userSecureStorageManager.readPin(session.localKey, session.username)
        return pass.pin.length == preferencesManager[session.username].pinCodeLength && pass.pin == expectedPin
    }

    private fun checkPassword(session: Session, pass: LockPass.PasswordPass): Boolean =
        session.appKey.userKeyBytes == pass.appKey.userKeyBytes

    private fun checkBiometric(pass: LockPass.BiometricPass): Boolean {
        return pass.cryptoObject.cipher
            ?.let { cryptoObjectHelper.challengeAuthentication(it) }
            ?.let { it is CryptoObjectHelper.CryptoChallengeResult.Success }
            ?: false
    }
}