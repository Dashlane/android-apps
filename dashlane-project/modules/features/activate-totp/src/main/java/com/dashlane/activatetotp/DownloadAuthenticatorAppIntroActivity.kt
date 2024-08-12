package com.dashlane.activatetotp

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import com.dashlane.help.HelpCenterLink
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.setIntroScreenContent
import com.dashlane.util.launchUrl
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.startActivity
import com.dashlane.util.tryOrNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadAuthenticatorAppIntroActivity : DashlaneActivity() {
    @Inject
    internal lateinit var logger: ActivateTotpLogger

    @Inject
    internal lateinit var authenticatorConnection: ActivateTotpAuthenticatorConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            lifecycle.whenResumed {
                val installed = tryOrNull { authenticatorConnection.hasSaveDashlaneTokenAsync().await() }
                    ?: false

                if (installed) {
                    startActivity<EnableTotpActivity>()
                    overridePendingTransition(0, 0)
                    finish()
                } else {
                    setIntroScreenContent(
                        imageResId = R.drawable.picto_authenticator,
                        titleResId = R.string.download_authenticator_app_intro_title,
                        descriptionResId = R.string.download_authenticator_app_intro_description,
                        linkResIds = listOf(R.string.download_authenticator_app_intro_link),
                        positiveButtonResId = R.string.download_authenticator_app_intro_cta_positive,
                        onClickPositiveButton = { launchUrl("https://play.google.com/store/apps/details?id=com.dashlane.authenticator") },
                        onClickLink = { _, _ -> launchUrl(HelpCenterLink.ARTICLE_AUTHENTICATOR_APP.androidUri) }
                    )

                    setCurrentPageView(AnyPage.SETTINGS_SECURITY_TWO_FACTOR_AUTHENTICATION_ENABLE_DOWNLOAD_AUTHENTICATOR)
                }
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                logger.logActivationCancel()
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }
}