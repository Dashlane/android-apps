package com.dashlane.ui.activities.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dashlane.autofillapi.ChromeAutofillDetector
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.inappautofill.AutoFillApiManager
import com.dashlane.inappautofill.AutoFillApiManager.AutofillStatus.ENABLED
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.onboarding.screen.DefaultInAppLoginBoardingScreen
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InAppLoginIntroActivity : DashlaneActivity() {
    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var autoFillNotificationCreator: AutoFillNotificationCreator

    @Inject
    lateinit var autofillApiManager: AutoFillApiManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var chromeAutofillDetector: ChromeAutofillDetector

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setCurrentPageView(AnyPage.SETTINGS_ASK_AUTOFILL_ACTIVATION)
        setContent {
            DashlaneTheme {
                DefaultInAppLoginBoardingScreen(
                    onActivateAutofillClick = ::onActivateAutofillClick,
                    onSkipClick = ::onSkipClick,
                )
            }
        }
        
        
        userPreferencesManager.canShowAutofillAnnouncement = true
        onBackPressedDispatcher.addCallback {
            
        }
    }

    override fun onResume() {
        super.onResume()
        if (autofillApiManager.getAutofillStatus() == ENABLED) goToNextStep(RESULT_OK)
    }

    private fun onActivateAutofillClick() {
        autoFillNotificationCreator.cancelAutofillNotificationWorkers()
        globalPreferencesManager.saveActivatedAutofillOnce()
        lockHelper.startAutoLockGracePeriod()
        autofillApiManager.createAutofillSettingsPendingIntent(true).send()
    }

    private fun onSkipClick() {
        goToNextStep(RESULT_CANCELED)
    }

    private fun goToNextStep(resultCode: Int) {
        setResult(resultCode)
        if (resultCode == RESULT_CANCELED) {
            finish()
            return
        }
        if (chromeAutofillDetector.isThirdPartyAutofillDeactivated(contentResolver, packageManager)) {
            finish()
            startActivity(Intent(this, ChromeInAppLoginIntroActivity::class.java))
        } else {
            
            startActivity(Intent(this, OnboardingInAppLoginDoneActivity::class.java))
            finish()
        }
    }
}