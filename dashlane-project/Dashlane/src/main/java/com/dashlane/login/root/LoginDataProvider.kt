package com.dashlane.login.root

import android.content.Context
import android.view.LayoutInflater
import com.dashlane.account.UserAccountInfo
import com.dashlane.account.UserAccountStorageImpl
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.authenticator.LoginDashlaneAuthenticatorContract
import com.dashlane.login.pages.authenticator.LoginDashlaneAuthenticatorProvider
import com.dashlane.login.pages.biometric.BiometricContract
import com.dashlane.login.pages.biometric.BiometricDataProvider
import com.dashlane.login.pages.email.LoginEmailContract
import com.dashlane.login.pages.email.LoginEmailDataProvider
import com.dashlane.login.pages.password.LoginPasswordContract
import com.dashlane.login.pages.password.LoginPasswordDataProvider
import com.dashlane.login.pages.pin.PinLockContract
import com.dashlane.login.pages.pin.PinLockDataProvider
import com.dashlane.login.pages.sso.SsoLockContract
import com.dashlane.login.pages.token.LoginTokenContract
import com.dashlane.login.pages.token.LoginTokenDataProvider
import com.dashlane.login.pages.totp.LoginTotpContract
import com.dashlane.login.pages.totp.LoginTotpDataProvider
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionRestorer
import com.dashlane.session.Username
import com.skocken.presentation.provider.BaseDataProvider
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider

class LoginDataProvider @Inject constructor(
    override val layoutInflater: LayoutInflater,
    private val userAccountStorage: UserAccountStorageImpl,
    private val emailDataProvider: Provider<LoginEmailDataProvider>,
    private val tokenDataProvider: Provider<LoginTokenDataProvider>,
    private val totpDataProvider: Provider<LoginTotpDataProvider>,
    private val passwordDataProvider: Provider<LoginPasswordDataProvider>,
    private val biometricDataProvider: Provider<BiometricDataProvider>,
    private val pinLockDataProvider: Provider<PinLockDataProvider>,
    private val ssoLockDataProvider: Provider<SsoLockContract.DataProvider>,
    private val dashlaneAuthenticatorDataProvider: Provider<LoginDashlaneAuthenticatorProvider>,
    private val lockTypeManager: LockTypeManager,
    private val sessionManager: SessionManager,
    private val sessionRestorer: SessionRestorer,
    private val userPreferencesManager: UserPreferencesManager,
    private val globalPreferencesManager: GlobalPreferencesManager
) : BaseDataProvider<LoginContract.Presenter>(), LoginContract.DataProvider {

    override lateinit var lockSetting: LockSetting

    @LockTypeManager.LockType
    override fun getLockType(context: Context): Int =
        if (lockSetting.lockType != LockTypeManager.LOCK_TYPE_UNSPECIFIED) {
            lockSetting.lockType
        } else {
            lockTypeManager.getLockType()
        }

    override val currentUserInfo: UserAccountInfo?
        get() {
            val user = globalPreferencesManager.getDefaultUsername()
            return if (user == null) null else userAccountStorage[user]
        }

    private val pendingMigrationToSsoMemberInfo: MigrationToSsoMemberInfo?
        get() = sessionRestorer.restoredSessionMigrationToSsoMemberInfo
            ?.takeIf { it.login == sessionManager.session?.userId }

    override fun isAlreadyLoggedIn(): Boolean = sessionManager.session != null

    override fun forceMasterPasswordUnlock(unlockReason: UnlockEvent.Reason?) =
        lockTypeManager.shouldEnterMasterPassword(unlockReason)

    override fun canDelayMasterPasswordUnlock(): Boolean {
        val accountCreationDate = userPreferencesManager.accountCreationDate
        
        return accountCreationDate.plus(Duration.ofDays(90)).isBefore(Instant.now())
    }

    override fun createEmailDataProvider(): LoginEmailContract.DataProvider =
        emailDataProvider.get()

    override fun createTokenDataProvider(emailSecondFactor: AuthenticationSecondFactor.EmailToken): LoginTokenContract.DataProvider =
        tokenDataProvider.get().also { it.emailSecondFactor = emailSecondFactor }

    override fun createAuthenticatorProvider(secondFactor: AuthenticationSecondFactor): LoginDashlaneAuthenticatorContract.DataProvider =
        dashlaneAuthenticatorDataProvider.get()
            .also { it.secondFactor = secondFactor }

    override fun createTotpDataProvider(totpSecondFactor: AuthenticationSecondFactor.Totp): LoginTotpContract.DataProvider =
        totpDataProvider.get().also { it.secondFactor = totpSecondFactor }

    override fun createPasswordDataProvider(
        registeredUserDevice: RegisteredUserDevice,
        authTicket: String?,
        migrationToSsoMemberInfo: MigrationToSsoMemberInfo?,
        topicLock: String?,
        allowBypass: Boolean
    ): LoginPasswordContract.DataProvider =
        passwordDataProvider.get().also {
            it.registeredUserDevice = registeredUserDevice
            it.authTicket = authTicket
            it.migrationToSsoMemberInfoProvider =
                { migrationToSsoMemberInfo ?: pendingMigrationToSsoMemberInfo }
            topicLock?.let { topic ->
                lockSetting = lockSetting.copy(topicLock = topic, allowBypass = allowBypass)
            }
            it.initLockSetting(lockSetting)
        }

    override fun createBiometricDataProvider(): BiometricContract.DataProvider =
        biometricDataProvider.get().also {
            it.migrationToSsoMemberInfoProvider = { pendingMigrationToSsoMemberInfo }
            it.initLockSetting(lockSetting)
        }

    override fun createPinLockDataProvider(): PinLockContract.DataProvider =
        pinLockDataProvider.get().also {
            it.migrationToSsoMemberInfoProvider = { pendingMigrationToSsoMemberInfo }
            it.initLockSetting(lockSetting)
        }

    override fun createSsoLockDataProvider(): SsoLockContract.DataProvider =
        ssoLockDataProvider.get().also { it.initLockSetting(lockSetting) }

    override suspend fun initializeStoredSession(login: String, serverKey: String?) {
        
        
        sessionRestorer.restoreSession(Username.ofEmail(login), serverKey)
    }
}