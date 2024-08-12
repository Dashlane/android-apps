package com.dashlane.authenticator

import com.dashlane.authenticator.ipc.PasswordManagerService
import com.dashlane.preference.UserPreferencesManager
import javax.inject.Inject

interface AuthenticatorSunsetChecker {
    val shouldDisplaySunsetBanner: Boolean
    fun markDisplayed()
}

class AuthenticatorSunsetCheckerImpl @Inject constructor(
    private val passwordManagerServiceStub: PasswordManagerService.Stub,
    private val userPreferencesManager: UserPreferencesManager
) : AuthenticatorSunsetChecker {

    private val passwordManagerService: PasswordManagerServiceStubImpl
        get() = passwordManagerServiceStub as PasswordManagerServiceStubImpl

    override val shouldDisplaySunsetBanner: Boolean
        get() = passwordManagerService.isPaired && !userPreferencesManager.hasSunsetBannerDisplayed

    override fun markDisplayed() {
        userPreferencesManager.hasSunsetBannerDisplayed = true
    }
}
