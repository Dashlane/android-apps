package com.dashlane.guidedonboarding

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.dashlane.darkweb.ui.result.DarkWebSetupResultActivity
import com.dashlane.darkweb.ui.setup.DarkWebSetupMailActivity
import com.dashlane.guidedonboarding.darkwebmonitoring.OnboardingDarkWebMonitoringActivity
import com.dashlane.guidedonboarding.databinding.ActivityOnboardingQuestionnaireBinding
import com.dashlane.navigation.NavigationConstants
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.startActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingQuestionnaireActivity : DashlaneActivity() {
    override var requireUserUnlock: Boolean = false
    private lateinit var binding: ActivityOnboardingQuestionnaireBinding

    private val viewModel by viewModels<OnboardingQuestionnaireViewModel>()
    private val onboardingDWMLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.onEmailConfirmationResult(it)
        }
    private val darkWebSetupLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.onDwmResult(it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onBackPressed()
        })

        OnboardingQuestionnaireViewProxy(
            context = this,
            lifecycle = this.lifecycle,
            binding = binding,
            viewModel = viewModel,
            onBackClick = {
                onBackPressedDispatcher.onBackPressed()
            },
            onTrustClick = {
                startActivity<TrustFAQActivity>(R.anim.slide_in_bottom, R.anim.no_animation) {}
            },
            onEnded = {
                val intent = it.putExtra(
                    NavigationConstants.STARTED_WITH_INTENT,
                    this.intent.getParcelableExtra<Parcelable?>(NavigationConstants.STARTED_WITH_INTENT)
                )
                startActivity(intent)
                finish()
            },
            onGoToDWM = {
                viewModel.hasNavigated()
                onboardingDWMLauncher.launch(
                    Intent(
                        this@OnboardingQuestionnaireActivity,
                        OnboardingDarkWebMonitoringActivity::class.java
                    )
                )
            },
            onGoToEmailConfirmation = {
                viewModel.hasNavigated()
                val intent = Intent(
                    this@OnboardingQuestionnaireActivity,
                    DarkWebSetupResultActivity::class.java
                ).apply {
                    putExtra(DarkWebSetupMailActivity.INTENT_SIGN_UP_MAIL, it)
                }
                darkWebSetupLauncher.launch(intent)
            },
            onCancel = { finish() },
            onDarkWebRegistrationError = {
                SnackbarUtils.showSnackbar(this, getString(R.string.darkweb_setup_mail_error))
            }
        )
    }
}