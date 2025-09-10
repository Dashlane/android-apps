package com.dashlane.sso.migration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.LoginIntentFactory
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.clearTask
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MigrationToSsoActivity : DashlaneActivity() {
    @Inject
    lateinit var successIntentFactory: LoginIntentFactory

    override var requireUserUnlock = false

    companion object {
        const val KEY_LOGIN = "login"
        const val KEY_SERVICE_PROVIDER_URL = "service_provider_url"
        const val KEY_IS_NITRO_PROVIDER = "is_nitro_provider"
        const val KEY_TOTP_AUTH_TICKET = "totp_auth_ticket"

        fun newIntent(
            context: Context,
            login: String,
            serviceProviderUrl: String,
            isNitroProvider: Boolean,
            totpAuthTicket: String?,
        ): Intent = Intent(context, MigrationToSsoActivity::class.java)
            .putExtra(KEY_LOGIN, login)
            .putExtra(KEY_SERVICE_PROVIDER_URL, serviceProviderUrl)
            .putExtra(KEY_IS_NITRO_PROVIDER, isNitroProvider)
            .putExtra(KEY_TOTP_AUTH_TICKET, totpAuthTicket)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DashlaneTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.safeDrawingPadding(),
                    containerColor = DashlaneTheme.colors.backgroundAlternate,
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                ) { contentPadding ->
                    MigrationToSsoScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = hiltViewModel(),
                        snackbarHostState = snackbarHostState,
                        finish = { shouldLaunchInitialSync, goToApplicationLock ->
                            val intent = createNextIntent(shouldLaunchInitialSync, goToApplicationLock)
                            startActivity(intent)
                            finish()
                        },
                        cancel = {
                            startActivity(successIntentFactory.createLoginActivityIntent(this).clearTask())
                            finish()
                        },
                    )
                }
            }
        }
    }

    private fun createNextIntent(
        shouldLaunchInitialSync: Boolean,
        goToApplicationLock: Boolean,
    ): Intent {
        return if (shouldLaunchInitialSync) {
            successIntentFactory.createLoginSyncProgressIntent(this)
        } else {
            successIntentFactory.createApplicationHomeIntent(this)
        }.let { nextIntent ->
            if (goToApplicationLock) {
                successIntentFactory.createOnboardingApplicationLockIntent(activity = this, nextIntent = nextIntent)
            } else {
                nextIntent
            }
        }
    }
}