package com.dashlane.createaccount

import com.dashlane.authentication.create.AccountCreationEmailRepository
import com.dashlane.cryptography.ObfuscatedByteArray
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@ActivityRetainedScoped
class CreateAccountRepository @Inject constructor() : Mutex by Mutex() {
    private var createAccountData = CreateAccountData()

    suspend fun getCreateAccountData() = withLock { createAccountData }

    suspend fun updateResult(result: AccountCreationEmailRepository.Result.Success?): Unit =
        withLock { createAccountData = createAccountData.copy(result = result) }

    suspend fun updatePassword(password: ObfuscatedByteArray): Unit =
        withLock { createAccountData = createAccountData.copy(password = password) }

    suspend fun updateBiometric(biometricEnabled: Boolean): Unit =
        withLock { createAccountData = createAccountData.copy(biometricEnabled = biometricEnabled) }

    suspend fun updateBiometricRecovery(biometricRecoveryEnabled: Boolean): Unit =
        withLock { createAccountData = createAccountData.copy(biometricRecoveryEnabled = biometricRecoveryEnabled) }

    suspend fun clearData(): Unit =
        withLock { createAccountData = CreateAccountData() }
}

data class CreateAccountData(
    val result: AccountCreationEmailRepository.Result.Success? = null,
    val password: ObfuscatedByteArray? = null,
    val biometricEnabled: Boolean = false,
    val biometricRecoveryEnabled: Boolean = false,
)