package com.dashlane.authenticator

import android.os.Bundle
import androidx.activity.compose.setContent
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterLink
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.launchUrl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthenticatorSunsetActivity : DashlaneActivity() {

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                AuthenticatorSunsetInfoScreen(
                    onLearnMoreClick = {
                        launchUrl(
                            HelpCenterLink.ARTICLE_AUTHENTICATOR_SUNSET.androidUri
                        )
                    },
                    onSettingClick = {
                        navigator.goToSettings(
                            NavigationHelper.Destination.SecondaryPath.SettingsPath.SECURITY
                        )
                        finish()
                    },
                    onNavigationClick = { finish() }
                )
            }
        }
    }
}
