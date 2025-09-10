package com.dashlane.sso.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.data.login.SsoInfo
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.LoginIntentFactory
import com.dashlane.navigation.LoginIntentFactory.Companion.copyOriginExtras
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.clearTask
import com.dashlane.util.getBaseActivity
import com.dashlane.util.getParcelableExtraCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class RemoteLoginSsoActivity : DashlaneActivity() {
    @Inject
    lateinit var successIntentFactory: LoginIntentFactory

    override var requireUserUnlock = false

    companion object {
        const val KEY_LOGIN = "login"
        const val KEY_IS_SSO_PROVIDER = "is_sso_provider"
        const val KEY_SERVICE_PROVIDER_URL = "service_provider_url"
        const val KEY_MIGRATE_TO_MASTER_PASSWORD_USER = "migrate_to_master_password_user"

        const val KEY_RESULT = "result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DashlaneTheme {
                Scaffold(
                    modifier = Modifier.safeDrawingPadding(),
                    containerColor = DashlaneTheme.colors.backgroundAlternate,
                ) { contentPadding ->
                    RemoteLoginSsoScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = hiltViewModel(),
                        goToMigrationToMasterPasswordUser = ::createMigrationToMasterPasswordUserIntent,
                        goToHome = {
                            startActivity(successIntentFactory.createApplicationHomeIntent(this))
                            finishAffinity()
                        },
                        goToInitialSync = {
                            startActivity(successIntentFactory.createLoginSyncProgressIntent(this))
                            finishAffinity()
                        },
                        goToOnboardingLock = {
                            startActivity(
                                successIntentFactory.createOnboardingApplicationLockIntent(
                                    activity = this,
                                    nextIntent = successIntentFactory.createLoginSyncProgressIntent(this),
                                ).clearTask(),
                            )
                            finishAffinity()
                        },
                        finishWithResult = { result ->
                            setResult(RESULT_OK, Intent().putExtra(KEY_RESULT, result))
                            finish()
                        },
                        cancel = { finish() },
                    )
                }
            }
        }
    }

    private fun createMigrationToMasterPasswordUserIntent(
        authTicket: String,
        shouldLaunchInitialSync: Boolean,
        hasPinOrBiometric: Boolean,
    ) {
        val successIntent = if (shouldLaunchInitialSync) {
            successIntentFactory.createLoginSyncProgressIntent(this)
        } else {
            successIntentFactory.createApplicationHomeIntent(this)
        }.let {
            if (hasPinOrBiometric) {
                successIntentFactory.createOnboardingApplicationLockIntent(
                    activity = this,
                    nextIntent = it,
                ).clearTask()
            } else {
                it
            }
        }

        val intent = successIntentFactory.createMigrationToMasterPasswordUserIntent(this, authTicket, successIntent)
        startActivity(intent)
        
        finishAffinity()
    }

    sealed class Result : Parcelable {
        @Parcelize
        data object Success : Result()

        sealed class Error : Result() {
            @Parcelize
            data object InvalidSso : Error()

            @Parcelize
            data object ForbiddenNavigation : Error()

            @Parcelize
            data object SamlResponseNotFound : Error()

            @Parcelize
            data object CannotOpenServiceProvider : Error()

            @Parcelize
            data object Network : Error()

            @Parcelize
            data object Offline : Error()

            @Parcelize
            data object ExpiredVersion : Error()

            @Parcelize
            data object Unknown : Error()
        }
    }
}

class SSOContract : ActivityResultContract<SsoInfo, RemoteLoginSsoActivity.Result>() {
    override fun createIntent(context: Context, input: SsoInfo): Intent {
        val isMigration = input.migration == SsoInfo.Migration.TO_MASTER_PASSWORD_USER
        return Intent(context, RemoteLoginSsoActivity::class.java).apply {
            putExtra(RemoteLoginSsoActivity.KEY_LOGIN, input.login)
            putExtra(RemoteLoginSsoActivity.KEY_SERVICE_PROVIDER_URL, input.serviceProviderUrl)
            putExtra(RemoteLoginSsoActivity.KEY_IS_SSO_PROVIDER, input.isNitroProvider)
            putExtra(RemoteLoginSsoActivity.KEY_MIGRATE_TO_MASTER_PASSWORD_USER, isMigration)
            context.getBaseActivity()?.let { copyOriginExtras(it.intent) }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): RemoteLoginSsoActivity.Result {
        if (resultCode == Activity.RESULT_CANCELED) return RemoteLoginSsoActivity.Result.Error.Unknown

        return intent?.getParcelableExtraCompat<RemoteLoginSsoActivity.Result>(RemoteLoginSsoActivity.KEY_RESULT)
            ?: RemoteLoginSsoActivity.Result.Error.Unknown
    }
}