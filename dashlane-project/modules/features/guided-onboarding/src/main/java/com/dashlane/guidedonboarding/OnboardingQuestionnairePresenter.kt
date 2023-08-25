package com.dashlane.guidedonboarding

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dashlane.darkweb.ui.result.DarkWebSetupResultActivity
import com.dashlane.darkweb.ui.setup.DarkWebSetupMailActivity
import com.dashlane.guidedonboarding.darkwebmonitoring.OnboardingDarkWebMonitoringActivity
import com.dashlane.guidedonboarding.darkwebmonitoring.OnboardingDarkWebMonitoringActivity.Companion.EXTRA_HAS_ALERTS
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer
import com.dashlane.ui.PostAccountCreationCoordinator
import com.dashlane.util.startActivity
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class OnboardingQuestionnairePresenter(
    private val logger: OnboardingQuestionnaireContract.Logger,
    private val postAccountCreationCoordinator: PostAccountCreationCoordinator,
    private val accountEmail: String
) : OnboardingQuestionnaireContract.Presenter,
    BasePresenter<OnboardingQuestionnaireDataProvider, OnboardingQuestionnaireContract.View>() {

    private enum class QuestionnaireStep {
        QUESTION_1,
        QUESTION_2,
        QUESTION_3,
        PLAN
    }

    private var currentStep: QuestionnaireStep = QuestionnaireStep.QUESTION_1
    private var selectedAnswers = arrayOf<QuestionnaireAnswer?>(null, null, null)

    
    private var planAnimPlayed = false

    override fun restore(savedInstanceState: Bundle?) {
        savedInstanceState?.let { bundle ->
            val stepName = bundle.getString(KEY_CURRENT_QUESTIONNAIRE_STEP)
            if (stepName != null) {
                currentStep = QuestionnaireStep.valueOf(stepName)

                planAnimPlayed = currentStep == QuestionnaireStep.PLAN
            }
            val selectedIds = bundle.getIntArray(KEY_SELECTED_ANSWERS) ?: intArrayOf(-1, -1, -1)
            selectedAnswers[0] = QuestionnaireAnswer.fromId(selectedIds[0])
            selectedAnswers[1] = QuestionnaireAnswer.fromId(selectedIds[1])
            selectedAnswers[2] = QuestionnaireAnswer.fromId(selectedIds[2])
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_CURRENT_QUESTIONNAIRE_STEP, currentStep.name)
        outState.putIntArray(
            KEY_SELECTED_ANSWERS,
            selectedAnswers.map { it?.id ?: -1 }.toIntArray()
        )
    }

    override fun onViewChanged() {
        super.onViewChanged()
        when (currentStep) {
            QuestionnaireStep.QUESTION_1 -> showQuestion1()
            QuestionnaireStep.QUESTION_2 -> showQuestion2()
            QuestionnaireStep.QUESTION_3 -> showQuestion3()
            QuestionnaireStep.PLAN -> showPlanReady()
        }
    }

    override fun onClickContinue() {
        when (currentStep) {
            QuestionnaireStep.QUESTION_1 -> {
                logger.logClickContinue(currentStep.name, selectedAnswers[0]?.name)
                showQuestion2()
            }
            QuestionnaireStep.QUESTION_2 -> {
                logger.logClickContinue(currentStep.name, selectedAnswers[1]?.name)
                showQuestion3()
            }
            QuestionnaireStep.QUESTION_3 -> {
                provider.savePlanResult(selectedAnswers[1]!!)
                logger.logClickContinue(currentStep.name, QuestionnaireAnswer.ACCOUNT_EMAIL.name)
                registerForDarkWebMonitoring()
            }
            QuestionnaireStep.PLAN -> {
                logger.logClickContinue(currentStep.name, selectedAnswers[2]?.name)
                endQuestionnaire()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            DWM_SETUP_REQUEST_CODE -> {
                
                activity?.startActivityForResult(
                    Intent(activity, OnboardingDarkWebMonitoringActivity::class.java),
                    DWM_EMAIL_CONFIRMATION_REQUEST_CODE
                )
            }
            DWM_EMAIL_CONFIRMATION_REQUEST_CODE -> {
                val emailConfirmed = resultCode == Activity.RESULT_OK
                val hasAlerts = data!!.getBooleanExtra(EXTRA_HAS_ALERTS, false)
                provider.saveDarkWebMonitoringResult(hasAlerts)
                if (emailConfirmed) {
                    endQuestionnaire()
                } else {
                    showPlanReady()
                }
            }
        }
    }

    override fun onBackPressed(): Boolean = when (currentStep) {
        QuestionnaireStep.QUESTION_1 -> {
            if (selectedAnswers[0] != null) {
                selectedAnswers[0] = null
                showQuestion1()
                true
            } else {
                false
            }
        }
        QuestionnaireStep.QUESTION_2 -> {
            if (selectedAnswers[1] != null) {
                selectedAnswers[1] = null
                showQuestion2()
            } else {
                showQuestion1()
            }
            true
        }
        QuestionnaireStep.QUESTION_3 -> {
            showQuestion2()
            true
        }
        QuestionnaireStep.PLAN -> {
            showQuestion3()
            true
        }
    }

    override fun onClickTrustButton() {
        context?.startActivity<TrustFAQActivity>(R.anim.slide_in_bottom, R.anim.no_animation) {}
    }

    override fun onClickSkipButton() {
        logger.logClickSkip(currentStep.name)
        when (currentStep) {
            QuestionnaireStep.QUESTION_3 -> showPlanReady()
            QuestionnaireStep.PLAN -> endQuestionnaire()
            else -> Unit
        }
    }

    override fun onAnswerSelected(
        indexQuestion: Int,
        answer: QuestionnaireAnswer?
    ) {
        selectedAnswers[indexQuestion] = answer
        if (answer != null) {
            logger.logClickAnswer(answer.name)
        }
    }

    private fun showQuestion3() {
        currentStep = QuestionnaireStep.QUESTION_3
        logCurrentStep()
        view.showQuestion3(QuestionnaireAnswer.ACCOUNT_EMAIL)
    }

    private fun showQuestion2() {
        currentStep = QuestionnaireStep.QUESTION_2
        logCurrentStep()
        view.showQuestion2(selectedAnswers[1])
    }

    private fun showQuestion1() {
        currentStep = QuestionnaireStep.QUESTION_1
        logCurrentStep()
        view.showQuestion1(selectedAnswers[0])
    }

    private fun showPlanReady() {
        currentStep = QuestionnaireStep.PLAN
        view.showPlanReady(!planAnimPlayed)
    }

    private fun logCurrentStep() {
        logger.logDisplay(currentStep.name)
    }

    private fun endQuestionnaire() = activity?.apply {
        postAccountCreationCoordinator.startHomeScreenAfterAccountCreation(this)
        finish()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun registerForDarkWebMonitoring() {
        GlobalScope.launch(Dispatchers.Main) {
            when (provider.optIn(accountEmail)) {
                RESULT_OK, RESULT_ALREADY_ACTIVATED -> {
                    
                    val intent = Intent(activity, DarkWebSetupResultActivity::class.java).apply {
                        putExtra(DarkWebSetupMailActivity.INTENT_SIGN_UP_MAIL, accountEmail)
                    }
                    activity?.startActivityForResult(intent, DWM_SETUP_REQUEST_CODE)
                }
                else -> view.showDarkWebRegistrationError()
            }
        }
    }

    companion object {
        const val KEY_CURRENT_QUESTIONNAIRE_STEP = "current_questionnaire_step"
        const val KEY_SELECTED_ANSWERS = "selected_answers"
        private const val RESULT_OK = "OK"
        private const val RESULT_ALREADY_ACTIVATED = "USER_HAS_ALREADY_AN_ACTIVE_SUBSCRIPTION"
        private const val DWM_SETUP_REQUEST_CODE = 7395
        private const val DWM_EMAIL_CONFIRMATION_REQUEST_CODE = 7396
    }
}