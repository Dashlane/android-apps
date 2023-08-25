package com.dashlane.authenticator.dashboard

sealed class AuthenticatorDashboardEditState {
    object EditLogins : AuthenticatorDashboardEditState()

    object ViewLogins : AuthenticatorDashboardEditState()
}