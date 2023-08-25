package com.dashlane.login

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyActivity
import com.dashlane.login.devicelimit.DeviceLimitActivity
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.monobucket.MonobucketActivity
import com.dashlane.login.pages.enforce2fa.Enforce2faLimitActivity
import com.dashlane.login.progress.LoginSyncProgressActivity
import com.dashlane.login.settings.LoginSettingsActivity
import com.dashlane.login.sso.LoginSsoActivity
import com.dashlane.login.sso.migration.MigrationToSsoMemberActivity
import com.dashlane.login.sso.migration.MigrationToSsoMemberIntroActivity
import com.dashlane.masterpassword.ChangeMasterPasswordActivity
import com.dashlane.masterpassword.ChangeMasterPasswordOrigin
import com.dashlane.masterpassword.warning.ChangeMPWarningDesktopActivity
import com.dashlane.navigation.NavigationConstants
import com.dashlane.notification.EXTRA_BREACH_NOTIFICATION_FORCE_REFRESH
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.util.clearTask
import com.dashlane.util.clearTop

object LoginIntents {

    fun createLoginActivityIntent(activity: Activity): Intent =
        Intent(activity, LoginActivity::class.java).apply {
            val intent = activity.intent

            putExtra(LockSetting.EXTRA_REDIRECT_TO_HOME, true)
            putExtra(NavigationConstants.FORCED_LOCK_SESSION_RESTORED, false)
            putExtra(NavigationConstants.SESSION_RESTORED_FROM_BOOT, false)

            copyPushTokenNotificationExtras(intent)
        }

    fun createMonobucketConfirmationIntent(activity: Activity) =
        Intent(activity, MonobucketActivity::class.java).apply {
            val intent = activity.intent

            copyOriginExtras(intent)
        }

    fun createDeviceLimitConfirmation(activity: Activity) =
        Intent(activity, DeviceLimitActivity::class.java).apply {
            val intent = activity.intent

            copyOriginExtras(intent)
        }

    fun createChangeMasterPasswordIntent(
        activity: Activity,
        origin: ChangeMasterPasswordOrigin,
        deviceCount: Int
    ): Intent {
        val displayWarning = origin is ChangeMasterPasswordOrigin.Migration || deviceCount > 1
        return if (displayWarning) {
            ChangeMPWarningDesktopActivity.newIntent(activity, origin)
        } else {
            ChangeMasterPasswordActivity.newIntent(activity, origin)
        }.apply {
            clearTask()
            val intent = activity.intent
            copyOriginExtras(intent)
        }
    }

    fun createAccountRecoveryKeyIntent(activity: Activity, registeredUserDevice: RegisteredUserDevice, authTicket: String?): Intent {
        return LoginAccountRecoveryKeyActivity.newIntent(activity).apply {
            putExtra(LoginAccountRecoveryKeyActivity.EXTRA_REGISTERED_USER_DEVICE, registeredUserDevice)
            putExtra(LoginAccountRecoveryKeyActivity.AUTH_TICKET, authTicket)
            clearTask()
            val intent = activity.intent
            copyOriginExtras(intent)
        }
    }

    fun createMigrationToSsoMemberIntent(
        activity: Activity,
        login: String,
        serviceProviderUrl: String,
        isNitroProvider: Boolean,
        totpAuthTicket: String?
    ): Intent {
        val migrationToSsoMemberIntent = MigrationToSsoMemberActivity.newIntent(
            activity,
            login = login,
            serviceProviderUrl = serviceProviderUrl,
            isNitroProvider = isNitroProvider,
            totpAuthTicket = totpAuthTicket
        ).apply {
            clearTask()
            val intent = activity.intent
            copyOriginExtras(intent)
        }

        return MigrationToSsoMemberIntroActivity.newIntent(
            activity,
            migrationToSsoMemberIntent = migrationToSsoMemberIntent
        ).apply {
            clearTask()
            val intent = activity.intent
            copyOriginExtras(intent)
        }
    }

    fun createSettingsActivityIntent(activity: Activity, clearTask: Boolean = true): Intent =
        Intent(activity, LoginSettingsActivity::class.java).apply {
            val intent = activity.intent

            copyOriginExtras(intent)

            if (clearTask) clearTask()
        }

    fun createProgressActivityIntent(activity: Activity): Intent =
        Intent(activity, LoginSyncProgressActivity::class.java).apply {
            val intent = activity.intent

            copyOriginExtras(intent)

            clearTop()
        }

    fun createHomeActivityIntent(activity: Activity): Intent =
        Intent(activity, HomeActivity::class.java).apply {
            val intent = activity.intent
            copyOriginExtras(intent)
            clearTask()
        }

    fun createSsoLoginActivityIntent(
        activity: Activity,
        login: String,
        serviceProviderUrl: String,
        isSsoProvider: Boolean,
        migrateToMasterPasswordUser: Boolean
    ): Intent =
        Intent(activity, LoginSsoActivity::class.java).apply {
            putExtra(LoginSsoActivity.KEY_LOGIN, login)
            putExtra(LoginSsoActivity.KEY_SERVICE_PROVIDER_URL, serviceProviderUrl)
            putExtra(LoginSsoActivity.KEY_IS_SSO_PROVIDER, isSsoProvider)
            putExtra(LoginSsoActivity.KEY_MIGRATE_TO_MASTER_PASSWORD_USER, migrateToMasterPasswordUser)
            copyOriginExtras(activity.intent)
        }

    fun createEnforce2faLimitActivityIntent(activity: Activity, clearTask: Boolean = true): Intent =
        Intent(activity, Enforce2faLimitActivity::class.java).apply {
            val intent = activity.intent

            copyOriginExtras(intent)

            if (clearTask) clearTask()
        }

    fun shouldCloseLoginAfterSuccess(originalIntent: Intent): Boolean {
        return originalIntent.getBooleanExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, false)
    }

    private fun Intent.copyOriginExtras(intent: Intent) {
        copyParcelableExtra<Intent>(intent, NavigationConstants.STARTED_WITH_INTENT)
        copyParcelableArrayExtra(intent, LoginSyncProgressActivity.EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION)

        copyBooleanExtra(intent, NavigationConstants.SESSION_RESTORED_FROM_BOOT)
        copyBooleanExtra(intent, EXTRA_BREACH_NOTIFICATION_FORCE_REFRESH)
        copyStringExtra(intent, LockSetting.EXTRA_DOMAIN)
        copyBooleanExtra(intent, NavigationConstants.FORCED_LOCK_SESSION_RESTORED)
        copyBooleanExtra(intent, LoginSyncProgressActivity.EXTRA_MONOBUCKET_UNREGISTRATION)

        copyPushTokenNotificationExtras(intent)
    }

    private fun Intent.copyPushTokenNotificationExtras(intent: Intent) {
        if (copyBooleanExtra(intent, NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION)) {
            copyBooleanExtra(intent, NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_USER)
            copyBooleanExtra(
                intent,
                NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_ALREADY_LOGGED_IN
            )
        }
    }

    private fun Intent.copyBooleanExtra(from: Intent, name: String): Boolean =
        from.getBooleanExtra(name, false).also { putExtra(name, it) }

    private fun Intent.copyStringExtra(from: Intent, name: String): String? =
        from.getStringExtra(name).also { putExtra(name, it) }

    private fun <T : Parcelable> Intent.copyParcelableExtra(from: Intent, name: String): T? =
        from.getParcelableExtra<T>(name).also { putExtra(name, it) }

    private fun Intent.copyParcelableArrayExtra(from: Intent, name: String) =
        from.getParcelableArrayExtra(name).also { putExtra(name, it) }
}
