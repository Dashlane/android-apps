package com.dashlane.authentication.create

import com.dashlane.authentication.AuthenticationException
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.user.UserAccountInfo

interface AccountCreator {
    @Throws(
        AuthenticationException::class,
        CannotInitializeSessionException::class,
    )
    suspend fun createAccount(
        username: String,
        password: ObfuscatedByteArray,
        accountType: UserAccountInfo.AccountType,
        termsState: TermsState?,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean,
        pinCode: String? = null,
        country: String? = null,
    )

    @Throws(
        AuthenticationException::class,
        CannotInitializeSessionException::class,
    )
    suspend fun createAccountSso(
        username: String,
        ssoToken: String,
        serviceProviderKey: String,
        termsState: TermsState?,
    )

    data class TermsState(
        val conditions: Boolean,
        val offers: Boolean,
    )

    class CannotInitializeSessionException(cause: Throwable?) : Exception("Session can't be created", cause)
}