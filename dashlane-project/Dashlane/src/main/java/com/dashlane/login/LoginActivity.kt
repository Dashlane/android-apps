package com.dashlane.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StyleRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.authentication.login.AuthenticationDeviceRepository
import com.dashlane.data.login.MigrationToSsoMemberInfo
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.endoflife.EndOfLife
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockSetting.Companion.EXTRA_AS_DIALOG
import com.dashlane.login.dagger.TrackingId
import com.dashlane.login.root.LocalLoginNavigationHost
import com.dashlane.login.root.LoginNavigationHost
import com.dashlane.login.root.LoginState
import com.dashlane.login.root.LoginViewModel
import com.dashlane.navigation.LoginIntentFactory
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.applyScreenshotAllowedFlag
import com.dashlane.ui.disableAutoFill
import com.dashlane.util.hideSoftKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : DashlaneActivity() {
    @Inject
    lateinit var endOfLife: EndOfLife

    @Inject
    lateinit var loginIntentFactory: LoginIntentFactory

    @Inject
    lateinit var loginStrategy: LoginStrategy

    @Inject
    lateinit var authenticationDeviceRepository: AuthenticationDeviceRepository

    @Inject
    @TrackingId
    lateinit var trackingId: String

    private val viewModel: LoginViewModel by viewModels()

    private var currentThemeResId = 0

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

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        val shouldThemeAsDialog = intent.extras?.getBoolean(EXTRA_AS_DIALOG, false) ?: false
        val syncError = intent.getBooleanExtra(SYNC_ERROR, false)

        currentThemeResId = savedInstanceState?.getInt(CURRENT_THEME_RES_ID) ?: getAppropriateTheme(shouldThemeAsDialog)
        setTheme(currentThemeResId)
        super.onCreate(savedInstanceState)
        this.disableAutoFill()
        window.applyScreenshotAllowedFlag(screenshotPolicy)

        setContent {
            DashlaneTheme {
                val snackBarHostState = remember { SnackbarHostState() }

                LaunchedEffect(syncError) {
                    if (syncError) snackBarHostState.showSnackbar(this@LoginActivity.getString(R.string.login_sync_error))
                }

                Scaffold(
                    modifier = Modifier.safeDrawingPadding(),
                    containerColor = Color.Transparent,
                    snackbarHost = {
                        DashlaneSnackbarWrapper {
                            SnackbarHost(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                hostState = snackBarHostState,
                            )
                        }
                    },
                ) { contentPadding ->
                    LaunchedEffect(viewModel) {
                        viewModel.viewStarted()
                        viewModel.stateFlow.sideEffect
                            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                            .collect { sideEffect -> parseSideEffects(sideEffect, snackBarHostState) }
                    }

                    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

                    when (val state = uiState) {
                        is LoginState.View.Initial -> Unit
                        is LoginState.View.Local -> {
                            LocalLoginNavigationHost(
                                contentPadding = contentPadding,
                                userAccountInfo = state.userAccountInfo,
                                startDestination = state.startDestination,
                                lockSetting = state.lockSetting,
                                showSnackBar = viewModel::showSnackBar,
                                onSuccess = viewModel::localLoginSuccess,
                                onCancel = viewModel::cancel,
                                onChangeAccount = viewModel::changeAccount,
                                onLogout = viewModel::logout,
                                onLogoutMPLess = { email -> viewModel.logout(email, true) },
                                goToArk = { viewModel.goToArk(null, it) },
                            )
                        }
                        is LoginState.View.Remote -> {
                            LoginNavigationHost(
                                contentPadding = contentPadding,
                                email = state.email,
                                allowSkipEmail = state.allowSkipEmail,
                                lockSetting = state.lockSetting,
                                goToCreateAccount = viewModel::createAccount,
                                endOfLife = viewModel::endOfLife,
                                onSuccess = viewModel::remoteLoginSuccess,
                                goToArk = viewModel::goToArk,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_THEME_RES_ID, currentThemeResId)
    }

    @StyleRes
    internal fun getAppropriateTheme(shouldThemeAsDialog: Boolean): Int {
        val themeResId: Int = when {
            shouldThemeAsDialog -> R.style.Theme_Dashlane_Login_Compose
            else -> R.style.Theme_Dashlane_Login_TranslucentWindow
        }
        return themeResId
    }

    @Suppress("LongMethod")
    private fun parseSideEffects(sideEffect: LoginState.SideEffect, snackBarHostState: SnackbarHostState) {
        val cancel = {
            setResult(RESULT_CANCELED, Intent())
            hideSoftKeyboard()
            finish()
        }

        when (sideEffect) {
            is LoginState.SideEffect.LocalSuccess.Cancel -> cancel()
            is LoginState.SideEffect.LocalSuccess.Success -> {
                val result = Intent()
                sideEffect.lockSetting.unlockReason?.let { result.putExtra(LockSetting.EXTRA_LOCK_REASON, it) }
                setResult(RESULT_OK, result)
                hideSoftKeyboard()
                val intent = createStrategyIntent(
                    email = sideEffect.email,
                    strategy = sideEffect.strategy,
                    lockSetting = sideEffect.lockSetting,
                    migrationToSsoMemberInfo = sideEffect.migrationToSsoMemberInfo,
                    ukiRequiresMonobucketConfirmation = sideEffect.ukiRequiresMonobucketConfirmation,
                )
                intent?.let { startActivity(intent) }
                finish()
            }
            is LoginState.SideEffect.RemoteSuccess.CreateAccount -> {
                startActivity(loginIntentFactory.createCreateAccountIntent(this, sideEffect.email, sideEffect.skipEmailIfPrefilled))
                finish()
            }
            LoginState.SideEffect.RemoteSuccess.EndOfLife -> endOfLife.showExpiredVersionMessaging(this@LoginActivity)
            is LoginState.SideEffect.RemoteSuccess.Finish -> {
                val intent = createStrategyIntent(
                    email = sideEffect.email,
                    strategy = sideEffect.strategy,
                    lockSetting = sideEffect.lockSetting,
                    migrationToSsoMemberInfo = sideEffect.migrationToSsoMemberInfo,
                    ukiRequiresMonobucketConfirmation = sideEffect.ukiRequiresMonobucketConfirmation,
                )
                if (intent == null) {
                    
                    finish()
                } else {
                    startActivity(intent)
                    finishAffinity()
                }
            }
            is LoginState.SideEffect.RemoteSuccess.FinishWithSuccess -> {
                setResult(RESULT_OK, Intent())
                hideSoftKeyboard()
                finish()
            }
            is LoginState.SideEffect.ShowSnackBar -> lifecycleScope.launch { snackBarHostState.showSnackbar(sideEffect.message) }
            is LoginState.SideEffect.GoToARK -> {
                startActivity(
                    loginIntentFactory.createARKIntent(
                        activity = this,
                        registeredUserDevice = sideEffect.registeredUserDevice,
                        accountType = sideEffect.accountType,
                        authTicket = sideEffect.authTicket,
                    ),
                )
            }
        }
    }

    private fun createStrategyIntent(
        email: String?,
        strategy: LoginStrategy.Strategy?,
        lockSetting: LockSetting?,
        migrationToSsoMemberInfo: MigrationToSsoMemberInfo?,
        ukiRequiresMonobucketConfirmation: Boolean,
    ): Intent? {
        val ssoMigration = migrationToSsoMemberInfo
            ?: authenticationDeviceRepository.restoredSessionMigrationToSsoMemberInfo
                ?.takeIf { it.login == email }

        return when {
            ssoMigration != null -> loginIntentFactory.createMigrationToSsoMemberIntent(
                activity = this,
                login = ssoMigration.login,
                serviceProviderUrl = ssoMigration.serviceProviderUrl,
                isNitroProvider = ssoMigration.isNitroProvider,
                totpAuthTicket = ssoMigration.totpAuthTicket,
            )
            strategy is LoginStrategy.Strategy.Monobucket && ukiRequiresMonobucketConfirmation -> {
                loginIntentFactory.createMonobucketIntent(this, strategy.device)
            }
            strategy is LoginStrategy.Strategy.DeviceLimit -> loginIntentFactory.createDeviceLimitIntent(this, strategy.devices)
            strategy is LoginStrategy.Strategy.Enforce2FA -> loginIntentFactory.createEnforce2faLimitActivityIntent(this)
            strategy is LoginStrategy.Strategy.MplessD2D -> loginIntentFactory.createLoginSyncProgressIntent(this)
            strategy is LoginStrategy.Strategy.Unlock -> {
                if (lockSetting?.redirectToHome == true) loginIntentFactory.createApplicationHomeIntent(this) else null
            }
            strategy is LoginStrategy.Strategy.NoStrategy -> loginIntentFactory.createLoginBiometricSetupIntent(this)
            else -> loginIntentFactory.createLoginBiometricSetupIntent(this)
        }
    }
}