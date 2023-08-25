package com.dashlane.createaccount

import android.widget.EditText
import com.dashlane.authentication.AuthenticationException
import com.dashlane.cryptography.ObfuscatedByteArray

interface AccountCreator {

    val isGdprDebugModeEnabled: Boolean

    val isGdprForced: Boolean

    @Throws(
        AuthenticationException::class,
        CannotInitializeSessionException::class
    )
    suspend fun createAccount(
        username: String,
        password: ObfuscatedByteArray,
        termsState: TermsState?,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean
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
