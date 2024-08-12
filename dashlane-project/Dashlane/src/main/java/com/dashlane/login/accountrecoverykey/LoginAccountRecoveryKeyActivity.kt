package com.dashlane.login.accountrecoverykey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.login.LoginIntents
import com.dashlane.login.LoginStrategy
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginAccountRecoveryKeyActivity : DashlaneActivity() {

    @Inject
    lateinit var loginStrategy: LoginStrategy

    @Inject
    lateinit var successIntentFactory: LoginSuccessIntentFactory

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    private val viewModel: LoginAccountRecoveryKeyViewModel by viewModels()

    companion object {
        const val ACCOUNT_RECOVERY_PASSWORD_RESULT = "account_recovery_password"
        const val EXTRA_REGISTERED_USER_DEVICE = "registered_user_device"
        const val ACCOUNT_TYPE = "account_type"
        const val AUTH_TICKET = "auth_ticket"
        fun newIntent(context: Context): Intent = Intent(context, LoginAccountRecoveryKeyActivity::class.java)
    }

    override var requireUserUnlock: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressed()
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })

        val registeredUserDevice = intent.extras?.getParcelable<RegisteredUserDevice>(EXTRA_REGISTERED_USER_DEVICE)
        val authTicket = intent.extras?.getString(AUTH_TICKET)
        val accountType = intent.extras?.getString(ACCOUNT_TYPE)
            ?.let { UserAccountInfo.AccountType.fromString(it) }
            ?: UserAccountInfo.AccountType.MasterPassword

        if (registeredUserDevice == null) {
            finish()
            return
        }

        val onSuccess = { strategy: LoginStrategy.Strategy? ->
            val intent = createStrategyIntent(strategy)
            startActivity(intent)
            finishAffinity()
        }

        setContent {
            DashlaneTheme {
                LoginAccountRecoveryNavigation(
                    mainViewModel = viewModel,
                    registeredUserDevice = registeredUserDevice,
                    authTicket = authTicket,
                    accountType = accountType,
                    onSuccess = onSuccess,
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun createStrategyIntent(strategy: LoginStrategy.Strategy?): Intent {
        return when {
            strategy == null -> successIntentFactory.createApplicationHomeIntent()
            strategy == LoginStrategy.Strategy.DEVICE_LIMIT -> successIntentFactory.createDeviceLimitIntent(loginStrategy.devices)
            strategy == LoginStrategy.Strategy.MONOBUCKET && userPreferencesManager.ukiRequiresMonobucketConfirmation ->
                successIntentFactory.createMonobucketIntent(loginStrategy.monobucketHelper.getMonobucketOwner()!!)
            strategy == LoginStrategy.Strategy.ENFORCE_2FA -> successIntentFactory.createEnforce2faLimitActivityIntent()
            else -> LoginIntents.createHomeActivityIntent(this)
        }
    }
}
