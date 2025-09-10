package com.dashlane.wear.login

import com.dashlane.data.login.RegisteredUserDevice
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@ActivityRetainedScoped
class WearLoginRepository @Inject constructor() : Mutex by Mutex() {
    private var userLogin: String? = null
    private var secretTransferPayload: SecretTransferPayload? = null
    private var registeredUserDevice: RegisteredUserDevice? = null

    suspend fun getUserLogin(): String? = withLock { userLogin }

    suspend fun updateUserLogin(login: String) {
        withLock { this.userLogin = login }
    }

    suspend fun getSecretTransferPayload(): SecretTransferPayload? =
        withLock { secretTransferPayload }

    suspend fun updateSecretTransferPayload(secretTransferPayload: SecretTransferPayload?) {
        withLock {
            this.secretTransferPayload = secretTransferPayload
            this.userLogin = secretTransferPayload?.login
        }
    }

    suspend fun getRegisteredUserDevice(): RegisteredUserDevice? = withLock { registeredUserDevice }

    suspend fun updateRegisteredUserDevice(registeredUserDevice: RegisteredUserDevice) {
        withLock { this.registeredUserDevice = registeredUserDevice }
    }

    suspend fun resetRepository() {
        withLock {
            secretTransferPayload = null
            registeredUserDevice = null
        }
    }
}