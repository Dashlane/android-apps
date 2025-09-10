package com.dashlane.createaccount

import kotlinx.serialization.Serializable

sealed interface CreateAccountDestination {
    @Serializable
    data class Email(val email: String?, val skipIfPrefilled: Boolean) : CreateAccountDestination

    @Serializable
    data class Password(val email: String, val isB2B: Boolean) : CreateAccountDestination

    @Serializable
    data object BiometricSetup : CreateAccountDestination

    @Serializable
    data object TermsAndConditions : CreateAccountDestination
}