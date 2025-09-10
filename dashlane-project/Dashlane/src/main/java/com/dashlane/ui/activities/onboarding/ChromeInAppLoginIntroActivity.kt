package com.dashlane.ui.activities.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationManagerCompat
import com.dashlane.autofillapi.ChromeAutofillDetector
import com.dashlane.autofillapi.ChromeAutofillSettingsHelper.buildChromeAutofillSettingsIntent
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.CHROME_NOTIFICATION_ID
import com.dashlane.notification.creator.ChromeAutofillNotificationCreator
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.onboarding.screen.ChromeInAppLoginBoardingScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChromeInAppLoginIntroActivity : DashlaneActivity() {
    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var autoFillNotificationCreator: ChromeAutofillNotificationCreator

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var chromeAutofillDetector: ChromeAutofillDetector

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                ChromeInAppLoginBoardingScreen(
                    onActivateAutofillClick = ::onActivateChromeAutofillClick,
                    onSkipClick = ::onSkipClick,
                )
            }
        }
        
        
        userPreferencesManager.canShowChromeAnnouncement = true
        NotificationManagerCompat.from(this).cancel(CHROME_NOTIFICATION_ID)

        onBackPressedDispatcher.addCallback {
            
        }
    }

    override fun onResume() {
        super.onResume()
        if (!chromeAutofillDetector.isThirdPartyAutofillDeactivated(contentResolver, packageManager)) goToNextStep(RESULT_OK)
    }

    private fun onActivateChromeAutofillClick() {
        autoFillNotificationCreator.cancelAutofillNotificationWorkers()
        lockHelper.startAutoLockGracePeriod()
        val status = chromeAutofillDetector.getChromeAutofillStatus(contentResolver, packageManager)
        startActivity(buildChromeAutofillSettingsIntent(status.packageName))
    }

    private fun onSkipClick() {
        goToNextStep(RESULT_CANCELED)
    }

    private fun goToNextStep(resultCode: Int) {
        setResult(resultCode)
        
        startActivity(Intent(this, OnboardingInAppLoginDoneActivity::class.java))
        finish()
    }
}