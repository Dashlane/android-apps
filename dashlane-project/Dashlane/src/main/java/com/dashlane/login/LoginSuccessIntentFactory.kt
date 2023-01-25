package com.dashlane.login

import android.app.Activity
import android.content.Intent
import com.dashlane.login.devicelimit.UnlinkDevicesActivity
import com.dashlane.login.monobucket.MonobucketActivity
import com.dashlane.masterpassword.ChangeMasterPasswordOrigin
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import javax.inject.Inject



class LoginSuccessIntentFactory @Inject constructor(
    private val activity: Activity,
    private val sessionManager: SessionManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val userPreferencesManager: UserPreferencesManager,
    private val ssoLoggerConfigProvider: LoginSsoLoggerConfigProvider
) {
    private val devicesCount get() = userPreferencesManager.devicesCount
    private val ssoLoggerConfig get() = ssoLoggerConfigProvider.ssoLoggerConfig

    fun createLoginBiometricSetupIntent() =
        LoginIntents.createSettingsActivityIntent(activity)

    fun createLoginSyncProgressIntent() =
        LoginIntents.createProgressActivityIntent(activity)

    fun createMonobucketIntent(monobucketOwner: Device) =
        LoginIntents.createMonobucketConfirmationIntent(activity).apply {
            putExtra(MonobucketActivity.EXTRA_BUCKET_OWNER, monobucketOwner)
        }

    fun createDeviceLimitIntent(devices: List<Device>) =
        LoginIntents.createDeviceLimitConfirmation(activity).apply {
            putExtra(UnlinkDevicesActivity.EXTRA_DEVICES, devices.toTypedArray())
        }

    fun createApplicationHomeIntent(): Intent {
        sessionManager.session?.let(sessionCredentialsSaver::saveCredentialsIfNecessary)
        return LoginIntents.createHomeActivityIntent(activity)
    }

    fun createAccountRecoveryIntent(): Intent {
        sessionManager.session?.let(sessionCredentialsSaver::saveCredentialsIfNecessary)
        return LoginIntents.createChangeMasterPasswordIntent(
            activity,
            ChangeMasterPasswordOrigin.Recovery,
            devicesCount
        )
    }

    fun createLoginSsoIntent(
        login: String,
        serviceProviderUrl: String,
        isSsoProvider: Boolean,
        migrateToMasterPasswordUser: Boolean = false
    ) = LoginIntents.createSsoLoginActivityIntent(
        activity,
        login,
        serviceProviderUrl,
        isSsoProvider,
        migrateToMasterPasswordUser,
        ssoLoggerConfig
    )

    fun createMigrationToMasterPasswordUserIntent(
        authTicket: String,
        successIntent: Intent
    ): Intent {
        sessionManager.session?.let(sessionCredentialsSaver::saveCredentialsIfNecessary)
        return LoginIntents.createChangeMasterPasswordIntent(
            activity,
            ChangeMasterPasswordOrigin.Migration(authTicket, successIntent),
            devicesCount
        )
    }

    fun createMigrationToSsoMemberIntent(
        login: String,
        serviceProviderUrl: String,
        isNitroProvider: Boolean,
        totpAuthTicket: String?
    ): Intent {
        sessionManager.session?.let(sessionCredentialsSaver::saveCredentialsIfNecessary)
        return LoginIntents.createMigrationToSsoMemberIntent(
            activity,
            ssoLoggerConfig,
            login = login,
            serviceProviderUrl = serviceProviderUrl,
            isNitroProvider = isNitroProvider,
            totpAuthTicket = totpAuthTicket
        )
    }

    fun createEnforce2faLimitActivityIntent(): Intent {
        sessionManager.session?.let(sessionCredentialsSaver::saveCredentialsIfNecessary)
        return LoginIntents.createEnforce2faLimitActivityIntent(activity = activity)
    }
}