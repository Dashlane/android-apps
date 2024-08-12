package com.dashlane.authentication.create

import android.widget.EditText
import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.AuthenticationException
import com.dashlane.cryptography.ObfuscatedByteArray

interface AccountCreator {

    @Throws(
        AuthenticationException::class,
        CannotInitializeSessionException::class
    )
    suspend fun createAccount(
        username: String,
        password: ObfuscatedByteArray,
        accountType: UserAccountInfo.AccountType,
        termsState: TermsState?,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean,
        pinCode: String? = null,
        country: String? = null
    )

    @Throws(
        AuthenticationException::class,
        CannotInitializeSessionException::class
    )
    suspend fun createAccountSso(
        username: String,
        ssoToken: String,
        serviceProviderKey: String,
        termsState: TermsState?
    )

    fun preFillUsername(usernameField: EditText, suggestedEmail: String? = null)

    fun preFillPassword(passwordField: EditText)

    data class TermsState(
        val conditions: Boolean,
        val offers: Boolean
    )

    class CannotInitializeSessionException(cause: Throwable?) : Exception("Session can't be created", cause)
}
