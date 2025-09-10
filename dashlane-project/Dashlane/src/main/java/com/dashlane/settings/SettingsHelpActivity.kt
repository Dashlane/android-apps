package com.dashlane.settings

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.Legal
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterLink
import com.dashlane.help.newIntent
import com.dashlane.settings.help.SettingsHelpScreen
import com.dashlane.settings.help.SettingsHelpState
import com.dashlane.settings.ui.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.common.compose.components.TopBarScaffold
import com.dashlane.ui.common.compose.components.TopBarState
import com.dashlane.ui.screens.settings.LicensesActivity
import com.dashlane.util.Toaster
import com.dashlane.util.compose.BackPressedDispatcherBridge
import com.dashlane.util.launchUrl
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.startActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsHelpActivity : DashlaneActivity() {
    @Inject
    lateinit var toaster: Toaster

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val menuProvider = BackPressedDispatcherBridge.getMenuProvider(this)
        addMenuProvider(menuProvider, this)

        setContent {
            DashlaneTheme {
                val topBarState by rememberSaveable { mutableStateOf(TopBarState(title = getString(R.string.settings_category_help))) }

                TopBarScaffold(
                    topBarState = topBarState,
                    back = ::finish,
                ) { contentPadding ->
                    SettingsHelpScreen(
                        modifier = Modifier.padding(contentPadding),
                        helpViewModel = hiltViewModel(),
                        checkHelpSideEffect = { checkHelpSideEffect(toaster, it) },
                    )
                }
            }
        }
    }
}

internal fun Activity.checkHelpSideEffect(toaster: Toaster, sideEffect: SettingsHelpState.SideEffect) {
    when (sideEffect) {
        SettingsHelpState.SideEffect.GoToLicences -> startActivity<LicensesActivity>()
        SettingsHelpState.SideEffect.GoToPrivacyPolicy -> launchUrl(Legal.URL_PRIVACY_POLICY)
        SettingsHelpState.SideEffect.GoToTermsOfService -> launchUrl(Legal.URL_TERMS_OF_SERVICE)
        SettingsHelpState.SideEffect.GoToTroubleShooting -> {
            val intent = HelpCenterLink.Base.newIntent(context = this)
            safelyStartBrowserActivity(intent)
        }
        SettingsHelpState.SideEffect.DisplayToast -> {
            toaster.show(getString(R.string.copied_), Toast.LENGTH_LONG)
        }
    }
}