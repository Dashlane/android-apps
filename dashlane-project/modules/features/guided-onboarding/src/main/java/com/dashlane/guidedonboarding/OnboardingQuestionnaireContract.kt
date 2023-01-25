package com.dashlane.guidedonboarding

import android.content.Intent
import android.os.Bundle
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer
import com.skocken.presentation.definition.Base
import java.util.Locale

internal interface OnboardingQuestionnaireContract {

    interface Presenter : Base.IPresenter {
        

        fun onClickContinue()

        

        fun onBackPressed(): Boolean

        

        fun onAnswerSelected(indexQuestion: Int, answer: QuestionnaireAnswer?)

        

        fun restore(savedInstanceState: Bundle?)

        

        fun onSaveInstanceState(outState: Bundle)

        

        fun onClickTrustButton()

        

        fun onClickSkipButton()

        

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    interface View : Base.IView {
        

        fun showQuestion1(q1SelectedAnswer: QuestionnaireAnswer?)

        

        fun showQuestion2(q2SelectedAnswer: QuestionnaireAnswer?)

        

        fun showQuestion3(q3SelectedAnswer: QuestionnaireAnswer)

        

        fun showPlanReady(animate: Boolean)

        

        fun showDarkWebRegistrationError()
    }

    interface DataProvider : Base.IDataProvider {
        

        fun savePlanResult(q2SelectedAnswer: QuestionnaireAnswer)

        

        fun saveDarkWebMonitoringResult(hasAlerts: Boolean)

        

        suspend fun optIn(email: String): String
    }

    interface Logger {
        fun logDisplay(step: String) = log(DISPLAY, step.lowercase(Locale.US))

        fun logClickAnswer(answer: String) = log(CLICK, answer.lowercase(Locale.US))

        fun logClickContinue(step: String, answer: String?) {
            val subtype = answer?.let { "${step}_$it" } ?: step
            log(CLICK, subtype.lowercase(Locale.US), "continue")
        }

        fun logClickSkip(step: String) = log(CLICK, step.lowercase(Locale.US), "skip")

        fun log(action: String, subtype: String? = null, subaction: String? = null)

        companion object {
            private const val CLICK = "click"
            private const val DISPLAY = "display"
        }
    }
}