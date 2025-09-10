package com.dashlane.login

import android.app.Activity
import android.content.Intent
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.createaccount.CreateAccountComposeActivity
import com.dashlane.data.login.Device
import com.dashlane.data.login.RegisteredUserDevice
import com.dashlane.lock.LockSetting
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyActivity
import com.dashlane.login.devicelimit.DeviceLimitActivity
import com.dashlane.login.lock.OnboardingApplicationLockActivity
import com.dashlane.login.monobucket.MonobucketActivity
import com.dashlane.login.pages.enforce2fa.Enforce2faLimitActivity
import com.dashlane.login.progress.LoginSyncProgressActivity
import com.dashlane.login.settings.LoginSettingsActivity
import com.dashlane.masterpassword.ChangeMasterPasswordComposeActivity
import com.dashlane.navigation.LoginIntentFactory
import com.dashlane.navigation.LoginIntentFactory.Companion.copyOriginExtras
import com.dashlane.navigation.LoginIntentFactory.Companion.copyPushTokenNotificationExtras
import com.dashlane.navigation.NavigationConstants
import com.dashlane.preference.PreferencesManager
import com.dashlane.premium.offer.list.view.OffersActivity
import com.dashlane.session.SessionManager
import com.dashlane.sso.login.RemoteLoginSsoActivity
import com.dashlane.sso.migration.MigrationToSsoActivity
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.clearTask
import com.dashlane.util.clearTop
import com.dashlane.util.getBaseActivity
import javax.inject.Inject

class LoginIntentFactoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
) : LoginIntentFactory {
    private val devicesCount get() = preferencesManager[sessionManager.session?.username].devicesCount

    override fun createCreateAccountIntent(activity: Activity, email: String, skipEmailIfPrefilled: Boolean): Intent =
        Intent(activity, CreateAccountComposeActivity::class.java)
            .apply {
                putExtra(CreateAccountComposeActivity.EXTRA_PRE_FILLED_EMAIL, email)
                putExtra(CreateAccountComposeActivity.EXTRA_SKIP_EMAIL_IF_PRE_FILLED, skipEmailIfPrefilled)
            }

    override fun createLoginActivityIntent(activity: Activity): Intent {
        return Intent(activity, LoginActivity::class.java).apply {
            val intent = activity.intent
            putExtra(LockSetting.EXTRA_REDIRECT_TO_HOME, true)
            putExtra(NavigationConstants.FORCED_LOCK_SESSION_RESTORED, false)
            putExtra(NavigationConstants.SESSION_RESTORED_FROM_BOOT, false)
            copyPushTokenNotificationExtras(intent)
        }
    }

    override fun createLoginActivityIntentWithError(activity: Activity): Intent {
        return Intent(activity, LoginActivity::class.java).apply {
            clearTask()
            putExtra(LoginActivity.SYNC_ERROR, true)
        }
    }

    override fun createARKIntent(
        activity: Activity,
        registeredUserDevice: RegisteredUserDevice,
        accountType: UserAccountInfo.AccountType,
        authTicket: String?,
    ): Intent {
        return LoginAccountRecoveryKeyActivity.newIntent(activity)
            .apply {
                putExtra(LoginAccountRecoveryKeyActivity.EXTRA_REGISTERED_USER_DEVICE, registeredUserDevice)
                putExtra(LoginAccountRecoveryKeyActivity.ACCOUNT_TYPE, accountType.toString())
                putExtra(LoginAccountRecoveryKeyActivity.AUTH_TICKET, authTicket)
            }
    }

    override fun createLoginBiometricSetupIntent(activity: Activity): Intent {
        return Intent(activity, LoginSettingsActivity::class.java)
            .apply {
                copyOriginExtras(activity.intent)
                clearTask()
            }
    }

    override fun createLoginSyncProgressIntent(activity: Activity): Intent {
        return Intent(activity, LoginSyncProgressActivity::class.java)
            .apply {
                copyOriginExtras(activity.intent)
                clearTop()
            }
    }

    override fun createMonobucketIntent(activity: Activity, monobucketOwner: Device): Intent {
        return Intent(activity, MonobucketActivity::class.java)
            .apply { copyOriginExtras(activity.intent) }
            .apply { putExtra(MonobucketActivity.EXTRA_BUCKET_OWNER, monobucketOwner) }
    }

    override fun createDeviceLimitIntent(activity: Activity, devices: List<Device>): Intent {
        return Intent(activity, DeviceLimitActivity::class.java)
            .apply { copyOriginExtras(activity.intent) }
            .apply { putExtra(DeviceLimitActivity.EXTRA_DEVICES, devices.toTypedArray()) }
    }

    override fun createApplicationHomeIntent(activity: Activity): Intent {
        return Intent(activity, HomeActivity::class.java).apply {
            copyOriginExtras(activity.intent)
            clearTask()
        }
    }

    override fun createLoginSsoIntent(
        activity: Activity,
        login: String,
        serviceProviderUrl: String,
        isSsoProvider: Boolean,
        migrateToMasterPasswordUser: Boolean,
    ): Intent {
        return Intent(activity, RemoteLoginSsoActivity::class.java).apply {
            putExtra(RemoteLoginSsoActivity.KEY_LOGIN, login)
            putExtra(RemoteLoginSsoActivity.KEY_SERVICE_PROVIDER_URL, serviceProviderUrl)
            putExtra(RemoteLoginSsoActivity.KEY_IS_SSO_PROVIDER, isSsoProvider)
            putExtra(RemoteLoginSsoActivity.KEY_MIGRATE_TO_MASTER_PASSWORD_USER, migrateToMasterPasswordUser)
            activity.getBaseActivity()?.let { copyOriginExtras(it.intent) }
        }
    }

    override fun createMigrationToMasterPasswordUserIntent(
        activity: Activity,
        authTicket: String,
        successIntent: Intent,
    ): Intent {
        val origin = ChangeMasterPasswordOrigin.Migration(authTicket, successIntent)
        return ChangeMasterPasswordComposeActivity.newIntent(activity, origin, showWarningDesktop = true)
            .apply {
                clearTask()
                val intent = activity.intent
                copyOriginExtras(intent)
            }
    }

    override fun createMigrationToSsoMemberIntent(
        activity: Activity,
        login: String,
        serviceProviderUrl: String,
        isNitroProvider: Boolean,
        totpAuthTicket: String?,
    ): Intent {
        return MigrationToSsoActivity
            .newIntent(
                context = activity,
                login = login,
                serviceProviderUrl = serviceProviderUrl,
                isNitroProvider = isNitroProvider,
                totpAuthTicket = totpAuthTicket,
            )
            .apply {
                clearTask()
                val intent = activity.intent
                copyOriginExtras(intent)
            }
    }

    override fun createEnforce2faLimitActivityIntent(activity: Activity, clearTask: Boolean): Intent {
        return Intent(activity, Enforce2faLimitActivity::class.java).apply {
            copyOriginExtras(activity.intent)
            if (clearTask) clearTask()
        }
    }

    override fun createOnboardingApplicationLockIntent(activity: Activity, nextIntent: Intent): Intent {
        return OnboardingApplicationLockActivity.newIntent(activity, nextIntent).clearTask()
    }

    override fun createOfferIntent(activity: Activity): Intent {
        return Intent(activity, OffersActivity::class.java)
    }
}