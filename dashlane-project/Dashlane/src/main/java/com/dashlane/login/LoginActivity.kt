package com.dashlane.login

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StyleRes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.user.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.createaccount.CreateAccountActivity
import com.dashlane.debug.DaDaDa
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.login.dagger.TrackingId
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.lock.LockSetting.Companion.buildFrom
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferNavigation
import com.dashlane.login.pages.totp.u2f.NfcServiceDetectorImpl
import com.dashlane.login.root.LocalLoginDestination
import com.dashlane.login.root.LocalLoginNavigationHost
import com.dashlane.login.root.LocalLoginNavigationState
import com.dashlane.login.root.LocalLoginViewModel
import com.dashlane.login.root.LoginDestination
import com.dashlane.login.root.LoginLegacyFragment
import com.dashlane.login.root.LoginNavigationHost
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import com.dashlane.login.sso.toMigrationToSsoMemberInfo
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionRestorer
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.applyScreenshotAllowedFlag
import com.dashlane.ui.disableAutoFill
import com.dashlane.ui.endoflife.EndOfLife
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.Toaster
import com.dashlane.util.hideSoftKeyboard
import com.dashlane.util.startActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

const val REQUEST_CODE_SSO_LOGIN = 24_365

@AndroidEntryPoint
class LoginActivity : DashlaneActivity() {

    @Inject
    lateinit var nfcServiceProvider: NfcServiceDetectorImpl

    @Inject
    lateinit var endOfLife: EndOfLife

    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var loginStrategy: LoginStrategy

    @Inject
    lateinit var successIntentFactory: LoginSuccessIntentFactory

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    @Inject
    lateinit var sessionRestorer: SessionRestorer

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var userAccountStorage: UserAccountStorage

    @Inject
    lateinit var lockTypeManager: LockTypeManager

    @Inject
    lateinit var toaster: Toaster

    @Inject
    @TrackingId
    lateinit var trackingId: String

    private var currentThemeResId = 0

    private val extras: Bundle?
        get() = intent?.extras

    companion object {
        internal const val LOCK_SETTING = "lock_setting"
        internal const val CURRENT_THEME_RES_ID = "current_theme_res_id"
        const val ALLOW_SKIP_EMAIL = "allow_skip_email"
        const val SYNC_ERROR = "sync_error"
    }

    private val createAccount = { email: String, skipEmailIfPrefilled: Boolean ->
        startActivity<CreateAccountActivity> {
            putExtra(CreateAccountActivity.EXTRA_PRE_FILLED_EMAIL, email)
            putExtra(CreateAccountActivity.EXTRA_SKIP_EMAIL_IF_PRE_FILLED, skipEmailIfPrefilled)
        }
    }

    private val goToEndOfLife: () -> Unit = {
        endOfLife.showExpiredVersionMessaging(this)
    }

    override var requireUserUnlock: Boolean
        get() = false
        set(requireUserUnlock) {
            super.requireUserUnlock = requireUserUnlock
        }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DadadaEntryPoint {
        
        fun getDadada(): DaDaDa
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var lockSetting: LockSetting? = null
        if (savedInstanceState != null) {
            lockSetting = savedInstanceState.getParcelable(LOCK_SETTING)
        }
        if (lockSetting == null) {
            lockSetting = buildFrom(extras)
        }

        val leaveAfterSuccess = LoginIntents.shouldCloseLoginAfterSuccess(intent)
        val dadada = EntryPointAccessors.fromApplication(this, DadadaEntryPoint::class.java).getDadada()

        currentThemeResId = savedInstanceState?.getInt(CURRENT_THEME_RES_ID) ?: getAppropriateTheme(lockSetting, dadada.isLocalLoginComposeEnabled)
        setTheme(currentThemeResId)
        super.onCreate(savedInstanceState)
        this.disableAutoFill()
        window.applyScreenshotAllowedFlag(screenshotPolicy)

        if (intent.getBooleanExtra(SYNC_ERROR, false)) {
            SnackbarUtils.showSnackbar(this, getString(R.string.login_sync_error))
        }

        val currentUser: UserAccountInfo? = globalPreferencesManager.getDefaultUsername()?.let { userAccountStorage[it] }
        val isSSO = sessionManager.session == null && currentUser?.sso == true
        val allowSkipEmail = intent.getBooleanExtra(ALLOW_SKIP_EMAIL, false)

        when {
            currentUser == null || isSSO || globalPreferencesManager.isUserLoggedOut -> {
                remoteLogin(
                    email = currentUser?.username,
                    allowSkipEmail = allowSkipEmail,
                    lockSetting = lockSetting,
                    leaveAfterSuccess = leaveAfterSuccess
                )
            }
            else -> {
                if (dadada.isLocalLoginComposeEnabled) {
                    val lockType = if (lockSetting.lockType == LockTypeManager.LOCK_TYPE_UNSPECIFIED) {
                        lockTypeManager.getLockType()
                    } else {
                        lockSetting.lockType
                    }
                    localLogin(currentUser, lockSetting.copy(lockType = lockType), leaveAfterSuccess)
                } else {
                    setContentView(R.layout.activity_login)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        if (NfcAdapter.ACTION_TECH_DISCOVERED != intent.action) {
            setIntent(intent) 
        }
        nfcServiceProvider.onNewIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_THEME_RES_ID, currentThemeResId)
    }

    @StyleRes
    internal fun getAppropriateTheme(lockSetting: LockSetting, isCompose: Boolean): Int {
        val themeResId: Int = when {
            lockSetting.shouldThemeAsDialog -> {
                if (isCompose) R.style.Theme_Dashlane_Login_Compose else R.style.Theme_Dashlane_Login_Dialog
            }
            else -> R.style.Theme_Dashlane_Login_TranslucentWindow
        }
        return themeResId
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        
        val loginHostFragment =
            supportFragmentManager.fragments.firstOrNull()?.childFragmentManager?.fragments?.firstOrNull() as? LoginLegacyFragment
        loginHostFragment?.onActivityResult(requestCode, resultCode, data)
    }

    private fun remoteLogin(email: String?, allowSkipEmail: Boolean, lockSetting: LockSetting, leaveAfterSuccess: Boolean) {
        val onSuccess: (LoginStrategy.Strategy?, SsoInfo?) -> Unit = { strategy, ssoInfo ->
            when {
                leaveAfterSuccess -> {
                    val result = Intent()
                    lockSetting.unlockReason?.let {
                        result.putExtra(LockSetting.EXTRA_LOCK_REASON, it)
                    }
                    setResult(Activity.RESULT_OK, result)
                    hideSoftKeyboard()
                    finish()
                }
                else -> {
                    val intent = createStrategyIntent(
                        strategy = strategy,
                        lockSetting = lockSetting,
                        migrationToSsoMemberInfo = ssoInfo?.toMigrationToSsoMemberInfo()
                    )
                    if (intent == null) {
                        
                        finish()
                    } else {
                        startActivity(intent)
                        finishAffinity()
                    }
                }
            }
        }

        setContent {
            DashlaneTheme {
                LoginNavigationHost(
                    email = email,
                    allowSkipEmail = allowSkipEmail,
                    lockSetting = lockSetting,
                    goToCreateAccount = createAccount,
                    endOfLife = goToEndOfLife,
                    onSuccess = onSuccess,
                )
            }
        }
    }

    @Suppress("LongMethod")
    private fun localLogin(userAccountInfo: UserAccountInfo, lockSetting: LockSetting, leaveAfterSuccess: Boolean) {
        val localLoginViewModel: LocalLoginViewModel by viewModels()

        val shouldShowPasswordForRemember = when {
            userAccountInfo.accountType is UserAccountInfo.AccountType.InvisibleMasterPassword -> false
            lockSetting.lockType == LockTypeManager.LOCK_TYPE_MASTER_PASSWORD -> false
            else -> userPreferencesManager.credentialsSaveDate.plus(Duration.ofDays(14L)) < Instant.now()
        }

        val lockSetting = lockSetting.copy(
            isShowMPForRemember = shouldShowPasswordForRemember,
            isLoggedIn = sessionManager.session != null
        )

        val showOtpScreen =
            
            (userAccountInfo.otp2 && sessionManager.session == null) ||
                
                ((userAccountInfo.securitySettings?.isTotp == true || userAccountInfo.otp2) && lockSetting.isMasterPasswordReset)

        val startDestination = when {
            showOtpScreen -> LocalLoginDestination.otp2Destination
            lockSetting.lockType == LockTypeManager.LOCK_TYPE_PIN_CODE && !shouldShowPasswordForRemember -> LocalLoginDestination.pinDestination
            lockSetting.lockType == LockTypeManager.LOCK_TYPE_BIOMETRIC && !shouldShowPasswordForRemember -> LocalLoginDestination.biometricDestination
            userAccountInfo.sso -> LocalLoginDestination.ssoDestination
            else -> {
                when (userAccountInfo.accountType) {
                    UserAccountInfo.AccountType.MasterPassword -> LocalLoginDestination.passwordDestination
                    
                    UserAccountInfo.AccountType.InvisibleMasterPassword ->
                        "${LocalLoginDestination.secretTransferDestination}?" +
                            "${LoginSecretTransferNavigation.START_DESTINATION_KEY}=${LoginSecretTransferNavigation.chooseTypeDestination}&" +
                            "${LoginDestination.LOGIN_KEY}=${userAccountInfo.username}"
                }
            }
        }

        setContent {
            DashlaneTheme {
                val navigationState by localLoginViewModel.navigationState.collectAsStateWithLifecycle(initialValue = null)

                LaunchedEffect(navigationState) {
                    when (val state = navigationState) {
                        LocalLoginNavigationState.Cancel -> {
                            val result = Intent()
                            lockSetting.unlockReason?.let { result.putExtra(LockSetting.EXTRA_LOCK_REASON, it) }
                            setResult(Activity.RESULT_CANCELED, result)
                            hideSoftKeyboard()
                            finish()
                        }
                        is LocalLoginNavigationState.ChangeAccount -> {
                            remoteLogin(email = state.email, allowSkipEmail = true, lockSetting = lockSetting, leaveAfterSuccess = leaveAfterSuccess)
                        }
                        is LocalLoginNavigationState.Logout -> {
                            
                            val loginIntent = Intent(this@LoginActivity, LoginActivity::class.java)
                            this@LoginActivity.startActivity(loginIntent)
                            finishAffinity()
                        }
                        is LocalLoginNavigationState.Success -> {
                            val result = Intent()
                            lockSetting.unlockReason?.let { result.putExtra(LockSetting.EXTRA_LOCK_REASON, it) }
                            setResult(Activity.RESULT_OK, result)
                            hideSoftKeyboard()
                            createStrategyIntent(
                                strategy = state.strategy,
                                lockSetting = lockSetting,
                                migrationToSsoMemberInfo = null,
                            )?.let { intent -> startActivity(intent) }
                            finish()
                        }
                        null -> Unit
                    }
                }

                LocalLoginNavigationHost(
                    userAccountInfo = userAccountInfo,
                    startDestination = startDestination,
                    lockSetting = lockSetting,
                    onSuccess = localLoginViewModel::loginSuccess,
                    onCancel = localLoginViewModel::cancel,
                    onChangeAccount = localLoginViewModel::changeAccount,
                    onLogout = localLoginViewModel::logout
                )
            }
        }
    }

    private fun createStrategyIntent(
        strategy: LoginStrategy.Strategy?,
        lockSetting: LockSetting,
        migrationToSsoMemberInfo: MigrationToSsoMemberInfo?
    ): Intent? {
        val ssoMigration =
            migrationToSsoMemberInfo ?: sessionRestorer.restoredSessionMigrationToSsoMemberInfo?.takeIf { it.login == sessionManager.session?.userId }
        return when {
            ssoMigration != null -> successIntentFactory.createMigrationToSsoMemberIntent(
                login = ssoMigration.login,
                serviceProviderUrl = ssoMigration.serviceProviderUrl,
                isNitroProvider = ssoMigration.isNitroProvider,
                totpAuthTicket = ssoMigration.totpAuthTicket
            )
            strategy == LoginStrategy.Strategy.MONOBUCKET && userPreferencesManager.ukiRequiresMonobucketConfirmation ->
                successIntentFactory.createMonobucketIntent(loginStrategy.monobucketHelper.getMonobucketOwner()!!)
            strategy == LoginStrategy.Strategy.DEVICE_LIMIT -> successIntentFactory.createDeviceLimitIntent(loginStrategy.devices)
            strategy == LoginStrategy.Strategy.ENFORCE_2FA -> successIntentFactory.createEnforce2faLimitActivityIntent()
            strategy == LoginStrategy.Strategy.UNLOCK -> {
                if (lockSetting.redirectToHome) successIntentFactory.createApplicationHomeIntent() else null
            }
            strategy == LoginStrategy.Strategy.NO_STRATEGY -> {
                successIntentFactory.createLoginBiometricSetupIntent()
            }
            else -> successIntentFactory.createLoginBiometricSetupIntent()
        }
    }
}
