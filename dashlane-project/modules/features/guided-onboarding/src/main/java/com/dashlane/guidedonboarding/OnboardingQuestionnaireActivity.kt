package com.dashlane.guidedonboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.dashlane.darkweb.DarkWebMonitoringManager
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.ui.PostAccountCreationCoordinator
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.inject.UserActivityComponent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingQuestionnaireActivity : DashlaneActivity() {
    override var requireUserUnlock: Boolean = false

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var darkWebMonitoringManager: DarkWebMonitoringManager

    @Inject
    lateinit var postAccountCreationCoordinator: PostAccountCreationCoordinator

    private lateinit var presenter: OnboardingQuestionnaireContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_questionnaire)

        val userActivityComponent = UserActivityComponent(this)
        val currentSessionUsageLogRepository =
            userActivityComponent.currentSessionUsageLogRepository
        val logger = GuidedOnboardingUsageLogger(currentSessionUsageLogRepository)

        val currentAccountEmail = globalPreferencesManager.getLastLoggedInUser()
        presenter = OnboardingQuestionnairePresenter(
            logger,
            postAccountCreationCoordinator,
            currentAccountEmail
        ).apply {
            restore(savedInstanceState)
            setProvider(OnboardingQuestionnaireDataProvider(userPreferencesManager, darkWebMonitoringManager))
            setView(OnboardingQuestionnaireViewProxy(this@OnboardingQuestionnaireActivity, currentAccountEmail))
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!presenter.onBackPressed()) {
                    finish()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }
}