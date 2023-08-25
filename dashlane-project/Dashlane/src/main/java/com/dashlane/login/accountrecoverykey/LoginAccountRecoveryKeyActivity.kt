package com.dashlane.login.accountrecoverykey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginAccountRecoveryKeyActivity : DashlaneActivity() {

    private val viewModel: LoginAccountRecoveryKeyViewModel by viewModels()

    companion object {
        const val ACCOUNT_RECOVERY_PASSWORD_RESULT = "account_recovery_password"
        const val EXTRA_REGISTERED_USER_DEVICE = "registered_user_device"
        const val AUTH_TICKET = "auth_ticket"
        fun newIntent(context: Context): Intent = Intent(context, LoginAccountRecoveryKeyActivity::class.java)
    }

    override var requireUserUnlock: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val registeredUserDevice = intent.extras?.getParcelable<RegisteredUserDevice>(EXTRA_REGISTERED_USER_DEVICE)
        val authTicket = intent.extras?.getString(AUTH_TICKET)
        if (registeredUserDevice == null) {
            finish()
            return
        }
        viewModel.arkFlowStarted(registeredUserDevice, authTicket)

        setContent {
            DashlaneTheme {
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(uiState) {
                    when (val state = uiState) {
                        is LoginAccountRecoveryKeyState.FinishWithSuccess -> {
                            setResult(
                                RESULT_OK,
                                Intent().apply {
                                    putExtra(
                                        ACCOUNT_RECOVERY_PASSWORD_RESULT,
                                        ObfuscatedByteArray(state.decryptedVaultKey.toByteArray())
                                    )
                                }
                            )
                            finish()
                        }
                        else -> Unit
                    }
                }

                LoginAccountRecoveryNavigation(viewModel, registeredUserDevice.login)
            }
        }
    }
}
