package com.dashlane.masterpassword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation.ChangeMasterPassword
import com.dashlane.changemasterpassword.ChangeMasterPasswordNavigation.Warning
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.compose.BackPressedDispatcherBridge
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeMasterPasswordComposeActivity : DashlaneActivity() {

    @Inject
    lateinit var navigator: Navigator

    companion object {
        private const val EXTRA_ORIGIN = "origin"
        private const val EXTRA_SHOW_WARNING_DESKTOP = "warning_desktop_shown"

        fun newIntent(
            context: Context,
            origin: ChangeMasterPasswordOrigin?,
            showWarningDesktop: Boolean = true
        ): Intent {
            return Intent(context, ChangeMasterPasswordComposeActivity::class.java)
                .putExtra(EXTRA_ORIGIN, origin)
                .putExtra(EXTRA_SHOW_WARNING_DESKTOP, showWarningDesktop)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val origin = intent.getParcelableExtraCompat<ChangeMasterPasswordOrigin>(EXTRA_ORIGIN) ?: ChangeMasterPasswordOrigin.Settings

        val page = if (origin is ChangeMasterPasswordOrigin.Settings) {
            AnyPage.SETTINGS_SECURITY_CHANGE_MASTER_PASSWORD
        } else {
            null
        }
        if (page != null) {
            this.setCurrentPageView(page)
        }

        val menuProvider = BackPressedDispatcherBridge.getMenuProvider(this)
        addMenuProvider(menuProvider, this)

        val startDestination = if (intent.getBooleanExtra(EXTRA_SHOW_WARNING_DESKTOP, false)) {
            Warning
        } else {
            ChangeMasterPassword
        }

        setContent {
            DashlaneTheme {
                ChangeMasterPasswordNavigation(
                    startDestination = startDestination,
                    onSuccess = { finish() },
                    onCancel = { finish() },
                    logout = { navigator.logoutAndCallLoginScreen(this) }
                )
            }
        }
    }
}