package com.dashlane.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StyleRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.createaccount.CreateAccountActivity
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.endoflife.EndOfLife
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockSetting.Companion.EXTRA_REDIRECT_TO_HOME
import com.dashlane.lock.LockSetting.Companion.buildFrom
import com.dashlane.login.dagger.TrackingId
import com.dashlane.login.root.LocalLoginDestination
import com.dashlane.login.root.LocalLoginNavigationHost
import com.dashlane.login.root.LoginNavigationHost
import com.dashlane.login.root.LoginState
import com.dashlane.login.root.LoginViewModel
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.applyScreenshotAllowedFlag
import com.dashlane.ui.disableAutoFill
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.hideSoftKeyboard
import com.dashlane.util.startActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : DashlaneActivity() {

    @Inject
    lateinit var endOfLife: EndOfLife

    @Inject
    lateinit var successIntentFactory: LoginSuccessIntentFactory

    @Inject
    lateinit var loginStrategy: LoginStrategy

    @Inject
    @TrackingId
    lateinit var trackingId: String

    private val loginViewModel: LoginViewModel by viewModels()

    private var currentThemeResId = 0

    private val extras: Bundle?
        get() = intent?.extras

    companion object {
        internal const val LOCK_SETTING = "lock_setting"
        internal const val CURRENT_THEME_RES_ID = "current_theme_res_id"
        const val ALLOW_SKIP_EMAIL = "allow_skip_email"
        const val SYNC_ERROR = "sync_error"
    }

    override var requireUserUnlock: Boolean
        get() = false
        set(requireUserUnlock) {
            super.requireUserUnlock = requireUserUnlock
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val lockSetting = savedInstanceState?.getParcelable(LOCK_SETTING) ?: buildFrom(extras)
        val leaveAfterSuccess = LoginIntents.shouldCloseLoginAfterSuccess(intent)

        currentThemeResId = savedInstanceState?.getInt(CURRENT_THEME_RES_ID) ?: getAppropriateTheme(lockSetting)
        setTheme(currentThemeResId)
        super.onCreate(savedInstanceState)
        this.disableAutoFill()
        window.applyScreenshotAllowedFlag(screenshotPolicy)

        val allowSkipEmail = intent.getBooleanExtra(ALLOW_SKIP_EMAIL, false)

        loginViewModel.viewStarted(lockSetting, allowSkipEmail = allowSkipEmail, leaveAfterSuccess)

        lifecycleScope.launch {
            loginViewModel.stateFlow.sideEffect
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { sideEffect -> parseSideEffects(sideEffect, lockSetting) }
        }

        lifecycleScope.launch {
            loginViewModel.stateFlow.viewState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { state -> loginContent(state) }
        }

        if (intent.getBooleanExtra(SYNC_ERROR, false)) {
            SnackbarUtils.showSnackbar(this, getString(R.string.login_sync_error))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_THEME_RES_ID, currentThemeResId)
    }

    @StyleRes
    internal fun getAppropriateTheme(lockSetting: LockSetting): Int {
        val themeResId: Int = when {
            lockSetting.shouldThemeAsDialog -> R.style.Theme_Dashlane_Login_Compose
            else -> R.style.Theme_Dashlane_Login_TranslucentWindow
        }
        return themeResId
    }

    private fun loginContent(state: LoginState.View) {
        when (state) {
            is LoginState.View.Initial -> Unit
            is LoginState.View.Local -> {
                localLogin(
                    registeredUserDevice = state.registeredUserDevice,
                    userAccountInfo = state.userAccountInfo,
                    lockSetting = state.lockSetting,
                    startDestination = state.startDestination
                )
            }
            is LoginState.View.Remote -> {
                remoteLogin(
                    email = state.email,
                    allowSkipEmail = state.allowSkipEmail,
                    lockSetting = state.lockSetting,
                )
            }
        }
    }

    private fun remoteLogin(email: String?, allowSkipEmail: Boolean, lockSetting: LockSetting) {
        setContent {
            DashlaneTheme {
                LoginNavigationHost(
                    email = email,
                    allowSkipEmail = allowSkipEmail,
                    lockSetting = lockSetting,
                    goToCreateAccount = loginViewModel::createAccount,
                    endOfLife = loginViewModel::endOfLife,
                    onSuccess = loginViewModel::remoteLoginSuccess,
                )
            }
        }
    }

    private fun localLogin(
        registeredUserDevice: RegisteredUserDevice,
        userAccountInfo: UserAccountInfo,
        lockSetting: LockSetting,
        startDestination: LocalLoginDestination
    ) {
        setContent {
            DashlaneTheme {
                LocalLoginNavigationHost(
                    registeredUserDevice = registeredUserDevice,
                    userAccountInfo = userAccountInfo,
                    startDestination = startDestination,
                    lockSetting = lockSetting,
                    onSuccess = loginViewModel::localLoginSuccess,
                    onCancel = loginViewModel::cancel,
                    onChangeAccount = loginViewModel::changeAccount,
                    onLogout = loginViewModel::logout,
                    onLogoutMPLess = { email -> loginViewModel.logout(email, true) },
                )
            }
        }
    }

    @Suppress("LongMethod")
    private fun parseSideEffects(sideEffect: LoginState.SideEffect, lockSetting: LockSetting) {
        when (sideEffect) {
            is LoginState.SideEffect.LocalSuccess.Cancel -> {
                val result = Intent()
                sideEffect.lockSetting?.unlockReason?.let { result.putExtra(LockSetting.EXTRA_LOCK_REASON, it) }
                setResult(Activity.RESULT_CANCELED, result)
                hideSoftKeyboard()
                finish()
            }
            is LoginState.SideEffect.LocalSuccess.ChangeAccount -> {
                remoteLogin(
                    email = sideEffect.email,
                    allowSkipEmail = true,
                    lockSetting = sideEffect.lockSetting ?: lockSetting,
                )
            }
            is LoginState.SideEffect.LocalSuccess.Logout -> {
                
                val loginIntent = Intent(this@LoginActivity, LoginActivity::class.java)
                loginIntent.putExtra(EXTRA_REDIRECT_TO_HOME, true)
                if (sideEffect.isMPLess) loginIntent.putExtra(ALLOW_SKIP_EMAIL, true)
                this@LoginActivity.startActivity(loginIntent)
                finishAffinity()
            }
            is LoginState.SideEffect.LocalSuccess.Success -> {
                val result = Intent()
                sideEffect.lockSetting?.unlockReason?.let { result.putExtra(LockSetting.EXTRA_LOCK_REASON, it) }
                setResult(Activity.RESULT_OK, result)
                hideSoftKeyboard()
                val intent = createStrategyIntent(
                    strategy = sideEffect.strategy,
                    lockSetting = sideEffect.lockSetting,
                    migrationToSsoMemberInfo = sideEffect.migrationToSsoMemberInfo,
                    ukiRequiresMonobucketConfirmation = sideEffect.ukiRequiresMonobucketConfirmation
                )
                intent?.let { startActivity(intent) }
                finish()
            }
            is LoginState.SideEffect.RemoteSuccess.CreateAccount -> {
                startActivity<CreateAccountActivity> {
                    putExtra(CreateAccountActivity.EXTRA_PRE_FILLED_EMAIL, sideEffect.email)
                    putExtra(CreateAccountActivity.EXTRA_SKIP_EMAIL_IF_PRE_FILLED, sideEffect.skipEmailIfPrefilled)
                }
                finish()
            }
            LoginState.SideEffect.RemoteSuccess.EndOfLife -> endOfLife.showExpiredVersionMessaging(this@LoginActivity)
            is LoginState.SideEffect.RemoteSuccess.Finish -> {
                val intent = createStrategyIntent(
                    strategy = sideEffect.strategy,
                    lockSetting = sideEffect.lockSetting,
                    migrationToSsoMemberInfo = sideEffect.migrationToSsoMemberInfo,
                    ukiRequiresMonobucketConfirmation = sideEffect.ukiRequiresMonobucketConfirmation
                )
                if (intent == null) {
                    
                    finish()
                } else {
                    startActivity(intent)
                    finishAffinity()
                }
            }
            is LoginState.SideEffect.RemoteSuccess.FinishWithSuccess -> {
                val result = Intent()
                sideEffect.unlockReason?.let { result.putExtra(LockSetting.EXTRA_LOCK_REASON, it) }
                setResult(Activity.RESULT_OK, result)
                hideSoftKeyboard()
                finish()
            }
        }
    }

    private fun createStrategyIntent(
        strategy: LoginStrategy.Strategy?,
        lockSetting: LockSetting?,
        migrationToSsoMemberInfo: MigrationToSsoMemberInfo?,
        ukiRequiresMonobucketConfirmation: Boolean
    ): Intent? {
        return when {
            migrationToSsoMemberInfo != null -> successIntentFactory.createMigrationToSsoMemberIntent(
                login = migrationToSsoMemberInfo.login,
                serviceProviderUrl = migrationToSsoMemberInfo.serviceProviderUrl,
                isNitroProvider = migrationToSsoMemberInfo.isNitroProvider,
                totpAuthTicket = migrationToSsoMemberInfo.totpAuthTicket
            )
            strategy is LoginStrategy.Strategy.Monobucket && ukiRequiresMonobucketConfirmation -> successIntentFactory.createMonobucketIntent(strategy.device)
            strategy is LoginStrategy.Strategy.DeviceLimit -> successIntentFactory.createDeviceLimitIntent(loginStrategy.devices)
            strategy is LoginStrategy.Strategy.Enforce2FA -> successIntentFactory.createEnforce2faLimitActivityIntent()
            strategy is LoginStrategy.Strategy.MplessD2D -> successIntentFactory.createLoginSyncProgressIntent()
            strategy is LoginStrategy.Strategy.Unlock -> {
                if (lockSetting?.redirectToHome == true) successIntentFactory.createApplicationHomeIntent() else null
            }
            strategy is LoginStrategy.Strategy.NoStrategy -> successIntentFactory.createLoginBiometricSetupIntent()
            else -> successIntentFactory.createLoginBiometricSetupIntent()
        }
    }
}
