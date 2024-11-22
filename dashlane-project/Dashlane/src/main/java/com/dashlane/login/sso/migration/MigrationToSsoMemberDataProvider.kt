package com.dashlane.login.sso.migration

import android.content.Context
import android.content.Intent
import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingStateRefresher
import com.dashlane.authentication.SsoServerKeyFactory
import com.dashlane.authentication.login.AuthenticationAuthTicketHelper
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.changemasterpassword.MasterPasswordChanger
import com.dashlane.crypto.keys.AppKey
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.hermes.generated.definitions.DeleteKeyReason
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockType
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.OnboardingApplicationLockActivity
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.user.Username
import com.skocken.presentation.provider.BaseDataProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MigrationToSsoMemberDataProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authTicketHelper: AuthenticationAuthTicketHelper,
    private val ssoServerKeyFactory: SsoServerKeyFactory,
    private val masterPasswordChanger: MasterPasswordChanger,
    private val preferencesManager: PreferencesManager,
    private val successIntentFactory: LoginSuccessIntentFactory,
    private val sessionManager: SessionManager,
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

        val shouldLaunchInitialSync = preferencesManager[login].getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0) == 0

        accountRecoveryKeyRepository.disableRecoveryKey(DeleteKeyReason.VAULT_KEY_CHANGED)
        accountRecoveryKeySettingStateRefresher.refresh()

        sessionManager.session?.let { lockManager.unlock(it, LockPass.ofPassword(ssoKey)) }

        return if (shouldLaunchInitialSync) {
            successIntentFactory.createLoginSyncProgressIntent()
        } else {
            successIntentFactory.createApplicationHomeIntent()
        }.let { nextIntent ->
            val locks = lockManager.getLocks(Username.ofEmail(login))
            if (LockType.PinCode !in locks && LockType.Biometric !in locks) {
                OnboardingApplicationLockActivity.newIntent(context, nextIntent)
            } else {
                nextIntent
            }
        }
    }
}