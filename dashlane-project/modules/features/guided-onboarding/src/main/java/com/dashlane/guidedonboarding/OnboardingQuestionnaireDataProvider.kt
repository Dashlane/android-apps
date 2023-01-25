package com.dashlane.guidedonboarding

import com.dashlane.darkweb.DarkWebMonitoringManager
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.Companion.KEY_GUIDED_ONBOARDING_DWM_USER_HAS_ALERTS
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.Companion.KEY_GUIDED_PASSWORD_ONBOARDING_Q2_ANSWER
import com.dashlane.preference.UserPreferencesManager
import com.skocken.presentation.provider.BaseDataProvider

internal class OnboardingQuestionnaireDataProvider(
    private val userPreferencesManager: UserPreferencesManager,
    private val darkWebMonitoringManager: DarkWebMonitoringManager
) :
    BaseDataProvider<OnboardingQuestionnaireContract.Presenter>(),
    OnboardingQuestionnaireContract.DataProvider {

    override fun savePlanResult(q2SelectedAnswer: QuestionnaireAnswer) {
        userPreferencesManager.putInt(KEY_GUIDED_PASSWORD_ONBOARDING_Q2_ANSWER, q2SelectedAnswer.id)
    }

    override fun saveDarkWebMonitoringResult(hasAlerts: Boolean) {
        userPreferencesManager.putBoolean(KEY_GUIDED_ONBOARDING_DWM_USER_HAS_ALERTS, hasAlerts)
    }

    override suspend fun optIn(email: String) = darkWebMonitoringManager.optIn(email)
}