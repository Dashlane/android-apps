package com.dashlane.login.pages.sso

import com.dashlane.authentication.login.AuthenticationSsoRepository
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.pages.ChangeAccountHelper
import com.dashlane.login.pages.LoginLockBaseDataProvider
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.session.SessionManager
import javax.inject.Inject

class SsoLockDataProvider @Inject constructor(
    private val ssoRepository: AuthenticationSsoRepository,
    private val changeAccountHelper: ChangeAccountHelper,
    private val sessionManager: SessionManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    successIntentFactory: LoginSuccessIntentFactory,
    lockManager: LockManager
) : LoginLockBaseDataProvider<SsoLockContract.Presenter>(
    lockManager,
    successIntentFactory
),
SsoLockContract.DataProvider {
    override val username = sessionManager.session?.userId.orEmpty()

    override val loginHistory: List<String> by lazy { globalPreferencesManager.getUserListHistory() }

    override suspend fun getSsoInfo(): SsoInfo {
        val session = sessionManager.session ?: throw SsoLockContract.NoSessionLoadedException()

        return ssoRepository.getSsoInfo(session.userId, session.accessKey)
    }

    override suspend fun unlock(userSsoInfo: UserSsoInfo) {
        val session = sessionManager.session ?: throw SsoLockContract.NoSessionLoadedException()

        if (userSsoInfo.login != session.userId) throw SsoLockContract.NoSessionLoadedException()

        val result = ssoRepository.validate(
            login = session.userId,
            ssoToken = userSsoInfo.ssoToken,
            serviceProviderKey = userSsoInfo.key,
            accessKey = session.accessKey
        )

        if (result !is AuthenticationSsoRepository.ValidateResult.Local) {
            throw IllegalStateException()
        }

        lockManager.unlock(LockPass.ofPassword(result.ssoKey))
    }

    override suspend fun changeAccount(email: String?) = changeAccountHelper.execute(email)
}