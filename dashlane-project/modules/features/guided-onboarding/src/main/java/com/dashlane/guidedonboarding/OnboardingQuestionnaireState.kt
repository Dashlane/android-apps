package com.dashlane.guidedonboarding

import android.content.Intent
import android.os.Parcelable
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer
import com.dashlane.guidedonboarding.widgets.QuestionnaireStep
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class OnboardingQuestionnaireState : Parcelable {
    abstract val viewData: OnboardingQuestionnaireData

    data class Cancel(override val viewData: OnboardingQuestionnaireData) : OnboardingQuestionnaireState()
    data class HasNavigated(override val viewData: OnboardingQuestionnaireData) : OnboardingQuestionnaireState()
    data class Question(override val viewData: OnboardingQuestionnaireData) : OnboardingQuestionnaireState()
    data class Plan(override val viewData: OnboardingQuestionnaireData, val animate: Boolean) : OnboardingQuestionnaireState()
    data class End(override val viewData: OnboardingQuestionnaireData, val intent: Intent) : OnboardingQuestionnaireState()
    data class GoToEmailConfirmation(override val viewData: OnboardingQuestionnaireData) : OnboardingQuestionnaireState()
    data class GoToDWM(override val viewData: OnboardingQuestionnaireData) : OnboardingQuestionnaireState()
    data class Error(override val viewData: OnboardingQuestionnaireData, val error: OnboardingQuestionnaireError) : OnboardingQuestionnaireState()
}

@Parcelize
data class OnboardingQuestionnaireData(
    val step: QuestionnaireStep? = null,
    val answers: Map<QuestionnaireStep, QuestionnaireAnswer?> = emptyMap(),
    val email: String? = null
) : Parcelable

@Parcelize
sealed class OnboardingQuestionnaireError : Parcelable {
    object DarkWebRegistrationError : OnboardingQuestionnaireError()
}
