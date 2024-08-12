package com.dashlane.login.root

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.SsoInfo
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ActivityRetainedScoped
class LoginRepository @Inject constructor() : Mutex by Mutex() {

    private var registeredUserDevice: RegisteredUserDevice? = null
    private var authTicket: String? = null
    private var ssoInfo: SsoInfo? = null

    suspend fun getRegisteredUserDevice(): RegisteredUserDevice? = withLock { registeredUserDevice }

    suspend fun updateRegisteredUserDevice(registeredUserDevice: RegisteredUserDevice?) =
        withLock { this.registeredUserDevice = registeredUserDevice }

    suspend fun getAuthTicket(): String? = withLock { authTicket }

    suspend fun updateAuthTicket(authTicket: String?) =
        withLock { this.authTicket = authTicket }

    suspend fun getSsoInfo(): SsoInfo? = withLock { ssoInfo }

    suspend fun updateSsoInfo(ssoInfo: SsoInfo?) =
        withLock { this.ssoInfo = ssoInfo }
}
