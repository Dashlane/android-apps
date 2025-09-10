package com.dashlane.navigation

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import com.dashlane.data.login.Device
import com.dashlane.data.login.RegisteredUserDevice
import com.dashlane.navigation.NavigationConstants.EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION
import com.dashlane.navigation.NavigationConstants.EXTRA_MONOBUCKET_UNREGISTRATION
import com.dashlane.user.UserAccountInfo

interface LoginIntentFactory {
    companion object {
        fun Intent.copyOriginExtras(intent: Intent) {
            copyParcelableExtra<Intent>(intent, NavigationConstants.STARTED_WITH_INTENT)
            copyParcelableArrayExtra(intent, EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION)

            copyBooleanExtra(intent, NavigationConstants.SESSION_RESTORED_FROM_BOOT)
            copyBooleanExtra(intent, EXTRA_BREACH_NOTIFICATION_FORCE_REFRESH)
            copyBooleanExtra(intent, NavigationConstants.FORCED_LOCK_SESSION_RESTORED)
            copyBooleanExtra(intent, EXTRA_MONOBUCKET_UNREGISTRATION)

            copyPushTokenNotificationExtras(intent)
        }

        private fun Intent.copyBooleanExtra(from: Intent, name: String): Boolean =
            from.getBooleanExtra(name, false).also { putExtra(name, it) }

        private fun <T : Parcelable> Intent.copyParcelableExtra(from: Intent, name: String): T? =
            from.getParcelableExtra<T>(name).also { putExtra(name, it) }

        private fun Intent.copyParcelableArrayExtra(from: Intent, name: String) =
            from.getParcelableArrayExtra(name).also { putExtra(name, it) }

        fun Intent.copyPushTokenNotificationExtras(intent: Intent) {
            if (copyBooleanExtra(intent, NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION)) {
                copyBooleanExtra(intent, NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_USER)
                copyBooleanExtra(
                    intent,
                    NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_ALREADY_LOGGED_IN,
                )
            }
        }
    }

    fun createCreateAccountIntent(activity: Activity, email: String, skipEmailIfPrefilled: Boolean): Intent

    fun createLoginActivityIntent(activity: Activity): Intent

    fun createLoginActivityIntentWithError(activity: Activity): Intent

    fun createARKIntent(
        activity: Activity,
        registeredUserDevice: RegisteredUserDevice,
        accountType: UserAccountInfo.AccountType,
        authTicket: String?,
    ): Intent

    fun createLoginBiometricSetupIntent(activity: Activity): Intent

    fun createLoginSyncProgressIntent(activity: Activity): Intent

    fun createMonobucketIntent(activity: Activity, monobucketOwner: Device): Intent

    fun createDeviceLimitIntent(activity: Activity, devices: List<Device>): Intent

    fun createApplicationHomeIntent(activity: Activity): Intent

    fun createOnboardingApplicationLockIntent(activity: Activity, nextIntent: Intent): Intent

    fun createLoginSsoIntent(
        activity: Activity,
        login: String,
        serviceProviderUrl: String,
        isSsoProvider: Boolean,
        migrateToMasterPasswordUser: Boolean = false,
    ): Intent

    fun createMigrationToMasterPasswordUserIntent(
        activity: Activity,
        authTicket: String,
        successIntent: Intent,
    ): Intent

    fun createMigrationToSsoMemberIntent(
        activity: Activity,
        login: String,
        serviceProviderUrl: String,
        isNitroProvider: Boolean,
        totpAuthTicket: String?,
    ): Intent

    fun createEnforce2faLimitActivityIntent(activity: Activity, clearTask: Boolean = true): Intent

    fun createOfferIntent(activity: Activity): Intent
}