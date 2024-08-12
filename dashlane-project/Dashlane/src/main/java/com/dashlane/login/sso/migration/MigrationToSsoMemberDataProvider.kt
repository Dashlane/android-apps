package com.dashlane.login.sso.migration

import android.content.Context
import android.content.Intent
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.setting.AccountRecoveryKeySettingStateRefresher
import com.dashlane.authentication.SsoServerKeyFactory
import com.dashlane.authentication.login.AuthenticationAuthTicketHelper
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.hermes.generated.definitions.DeleteKeyReason
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.lock.OnboardingApplicationLockActivity
import com.dashlane.masterpassword.MasterPasswordChanger
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.crypto.keys.AppKey
import com.skocken.presentation.provider.BaseDataProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MigrationToSsoMemberDataProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authTicketHelper: AuthenticationAuthTicketHelper,
    private val ssoServerKeyFactory: SsoServerKeyFactory,
    private val masterPasswordChanger: MasterPasswordChanger,
    private val userPreferencesManager: UserPreferencesManager,
    private val successIntentFactory: LoginSuccessIntentFactory,
    private val lockManager: LockManager,
    private val intent: Intent,
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val accountRecoveryKeySettingStateRefresher: AccountRecoveryKeySettingStateRefresher,
) : BaseDataProvider<MigrationToSsoMemberContract.Presenter>(),
    MigrationToSsoMemberContract.DataProvider {

    private val totpAuthTicket: String?
        get() = intent.getStringExtra(MigrationToSsoMemberActivity.KEY_TOTP_AUTH_TICKET)

    override suspend fun migrateToSsoMember(login: String, userSsoInfo: UserSsoInfo): Intent {
        check(login == userSsoInfo.login)

        val serviceProviderKey = userSsoInfo.key.decodeBase64ToByteArray()

        val authTicket = totpAuthTicket ?: authTicketHelper.verifySso(login, userSsoInfo.ssoToken).authTicket

        val serverKey = ssoServerKeyFactory.generateSsoServerKey()

        val ssoKey = AppKey.SsoKey.create(serverKey = serverKey, serviceProviderKey = serviceProviderKey)

        masterPasswordChanger.migrateToSsoMember(
            ssoKey = ssoKey,
            authTicket = authTicket,
            ssoServerKey = serverKey
        )

        val shouldLaunchInitialSync = userPreferencesManager.getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0

        accountRecoveryKeyRepository.disableRecoveryKey(DeleteKeyReason.VAULT_KEY_CHANGED)
        accountRecoveryKeySettingStateRefresher.refresh()
        lockManager.unlock(LockPass.ofPassword(ssoKey))

        return if (shouldLaunchInitialSync) {
            successIntentFactory.createLoginSyncProgressIntent()
        } else {
            successIntentFactory.createApplicationHomeIntent()
        }.let { nextIntent ->
            if (lockManager.getLockType() == LockTypeManager.LOCK_TYPE_MASTER_PASSWORD) {
                OnboardingApplicationLockActivity.newIntent(context, nextIntent)
            } else {
                nextIntent
            }
        }
    }
}