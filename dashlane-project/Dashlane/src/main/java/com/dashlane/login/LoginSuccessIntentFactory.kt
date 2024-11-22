package com.dashlane.login

import android.app.Activity
import android.content.Intent
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.login.devicelimit.UnlinkDevicesActivity
import com.dashlane.login.monobucket.MonobucketActivity
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import javax.inject.Inject

class LoginSuccessIntentFactory @Inject constructor(
    private val activity: Activity,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager
) {
    private val devicesCount get() = preferencesManager[sessionManager.session?.username].devicesCount

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
        return LoginIntents.createHomeActivityIntent(activity)
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
        migrateToMasterPasswordUser
    )

    fun createMigrationToMasterPasswordUserIntent(
        authTicket: String,
        successIntent: Intent
    ): Intent {
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
        return LoginIntents.createMigrationToSsoMemberIntent(
            activity,
            login = login,
            serviceProviderUrl = serviceProviderUrl,
            isNitroProvider = isNitroProvider,
            totpAuthTicket = totpAuthTicket
        )
    }

    fun createEnforce2faLimitActivityIntent(): Intent {
        return LoginIntents.createEnforce2faLimitActivityIntent(activity = activity)
    }
}