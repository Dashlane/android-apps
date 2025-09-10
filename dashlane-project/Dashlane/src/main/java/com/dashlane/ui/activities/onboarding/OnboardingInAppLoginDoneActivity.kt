package com.dashlane.ui.activities.onboarding

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.onboarding.screen.InAppLoginDoneBoardingScreen
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingInAppLoginDoneActivity : DashlaneActivity() {
    private val viewModel: OnboardingInAppLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setCurrentPageView(AnyPage.SETTINGS_CONFIRM_AUTOFILL_ACTIVATION)
        setContent {
            DashlaneTheme {
                InAppLoginDoneBoardingScreen {
                    viewModel.onDoneClicked()
                    finish()
                }
            }
        }
    }
}