package com.dashlane.guidedonboarding

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.darkweb.DarkWebMonitoringManager
import com.dashlane.guidedonboarding.OnboardingQuestionnaireState.Cancel
import com.dashlane.guidedonboarding.OnboardingQuestionnaireState.End
import com.dashlane.guidedonboarding.OnboardingQuestionnaireState.Error
import com.dashlane.guidedonboarding.OnboardingQuestionnaireState.GoToDWM
import com.dashlane.guidedonboarding.OnboardingQuestionnaireState.GoToEmailConfirmation
import com.dashlane.guidedonboarding.OnboardingQuestionnaireState.HasNavigated
import com.dashlane.guidedonboarding.OnboardingQuestionnaireState.Plan
import com.dashlane.guidedonboarding.OnboardingQuestionnaireState.Question
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer
import com.dashlane.guidedonboarding.widgets.QuestionnaireStep
import com.dashlane.guidedonboarding.widgets.QuestionnaireStep.PLAN
import com.dashlane.guidedonboarding.widgets.QuestionnaireStep.PRE_QUESTION
import com.dashlane.guidedonboarding.widgets.QuestionnaireStep.QUESTION_1
import com.dashlane.guidedonboarding.widgets.QuestionnaireStep.QUESTION_2
import com.dashlane.guidedonboarding.widgets.QuestionnaireStep.QUESTION_3
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.FormName
import com.dashlane.hermes.generated.definitions.PossibleFormAnswers
import com.dashlane.hermes.generated.events.user.SubmitInProductFormAnswer
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.ui.PostAccountCreationCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
internal class OnboardingQuestionnaireViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val darkWebMonitoringManager: DarkWebMonitoringManager,
    private val savedStateHandle: SavedStateHandle,
    private val postAccountCreationCoordinator: PostAccountCreationCoordinator,
    private val logRepository: LogRepository,
    globalPreferencesManager: GlobalPreferencesManager
) : ViewModel() {

    val uiState: StateFlow<OnboardingQuestionnaireState>

    init {
        val currentEmail = globalPreferencesManager.getLastLoggedInUser()
        uiState = savedStateHandle.getStateFlow<OnboardingQuestionnaireState>(
            VIEW_STATE_KEY,
            Question(
                OnboardingQuestionnaireData(
                    step = PRE_QUESTION,
                    answers = emptyMap(),
                    email = currentEmail
                )
            )
        )
    }

    fun hasNavigated() {
        savedStateHandle[VIEW_STATE_KEY] = HasNavigated(uiState.value.viewData)
    }

    fun onClickContinue() {
        val currentViewData = uiState.value.viewData
        when (currentViewData.step) {
            PRE_QUESTION -> {
                showQuestion(currentViewData.copy(step = QUESTION_1))
            }
            QUESTION_1 -> {
                showQuestion(currentViewData.copy(step = QUESTION_2))
            }
            QUESTION_2 -> {
                showQuestion(
                    currentViewData.copy(
                        step = QUESTION_3,
                        answers = currentViewData.answers + (QUESTION_3 to QuestionnaireAnswer.ACCOUNT_EMAIL)
                    )
                )
            }
            QUESTION_3 -> {
                currentViewData.answers[QUESTION_2]?.apply {
                    userPreferencesManager.putInt(
                        QuestionnaireAnswer.KEY_GUIDED_PASSWORD_ONBOARDING_Q2_ANSWER,
                        this.id
                    )
                }
                registerForDarkWebMonitoring()
            }
            PLAN -> {
                endQuestionnaire()
            }
            null -> Unit
        }
    }

    fun onDwmResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            viewModelScope.launch {
                savedStateHandle[VIEW_STATE_KEY] = GoToDWM(uiState.value.viewData)
            }
        }
    }

    fun onEmailConfirmationResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            endQuestionnaire()
        } else {
            showPlanReady()
        }
    }

    fun onBackPressed() {
        val currentData = uiState.value.viewData
        when (currentData.step) {
            PRE_QUESTION -> {
                
                savedStateHandle[VIEW_STATE_KEY] = Cancel(uiState.value.viewData)
            }
            QUESTION_1 -> {
                if (currentData.answers[QUESTION_1] != null) {
                    
                    showQuestion(
                        currentData.copy(
                            step = QUESTION_1,
                            answers = currentData.answers - QUESTION_1
                        )
                    )
                } else {
                    
                    savedStateHandle[VIEW_STATE_KEY] = Cancel(uiState.value.viewData)
                }
            }
            QUESTION_2 -> {
                if (currentData.answers[QUESTION_2] != null) {
                    
                    showQuestion(
                        currentData.copy(
                            step = QUESTION_2,
                            answers = currentData.answers - QUESTION_2
                        )
                    )
                } else {
                    
                    showQuestion(currentData.copy(step = QUESTION_1))
                }
            }
            QUESTION_3 -> showQuestion(
                currentData.copy(
                    step = QUESTION_2,
                    answers = currentData.answers - QUESTION_3
                )
            )
            PLAN -> showQuestion(currentData.copy(step = QUESTION_3))
            else -> Unit
        }
    }

    fun onClickSkipButton() {
        when (uiState.value.viewData.step) {
            PRE_QUESTION,
            QUESTION_1,
            QUESTION_2 -> onClickContinue()
            QUESTION_3 -> showPlanReady()
            PLAN -> endQuestionnaire()
            else -> Unit
        }
    }

    fun onAnswerSelected(
        questionnaireStep: QuestionnaireStep?,
        answer: QuestionnaireAnswer?
    ) {
        if (questionnaireStep == null) return
        val currentData = uiState.value.viewData
        if (questionnaireStep == PRE_QUESTION) {
            logPreQuestionnaireAnswer(answer)
            if (answer == QuestionnaireAnswer.EXISTING_USER) {
                showQuestion(
                    currentData.copy(
                        step = QUESTION_3,
                        answers = currentData.answers + (questionnaireStep to answer)
                    )
                )
            } else {
                onClickContinue()
            }
        } else {
            showQuestion(
                currentData.copy(
                    step = questionnaireStep,
                    answers = currentData.answers + (questionnaireStep to answer)
                )
            )
        }
    }

    private fun logPreQuestionnaireAnswer(answer: QuestionnaireAnswer?) {
        answer ?: return
        val chosen = when (answer) {
            QuestionnaireAnswer.NAIVE_USER -> PossibleFormAnswers.NEVER_USED_BEFORE
            QuestionnaireAnswer.NEW_USER -> PossibleFormAnswers.USED_ANOTHER_PASSWORD_MANAGER
            QuestionnaireAnswer.EXISTING_USER -> PossibleFormAnswers.FAMILIAR_WITH_DASHLANE
            else -> return
        }
        logRepository.queueEvent(
            SubmitInProductFormAnswer(
                formName = FormName.FAMILIARITY_WITH_DASHLANE,
                answerList = listOf(
                    PossibleFormAnswers.NEVER_USED_BEFORE,
                    PossibleFormAnswers.USED_ANOTHER_PASSWORD_MANAGER,
                    PossibleFormAnswers.FAMILIAR_WITH_DASHLANE
                ),
                chosenAnswerList = listOf(chosen)
            )
        )
    }

    private fun showPlanReady() {
        savedStateHandle[VIEW_STATE_KEY] = Plan(OnboardingQuestionnaireData(step = PLAN), true)
    }

    fun onPlanAnimationDone() {
        
        savedStateHandle[VIEW_STATE_KEY] = Plan(uiState.value.viewData, false)
    }

    private fun endQuestionnaire() {
        val intent = postAccountCreationCoordinator.getHomeScreenAfterAccountCreationIntent()
        savedStateHandle[VIEW_STATE_KEY] =
            End(uiState.value.viewData.copy(step = null), intent = intent)
    }

    private fun showQuestion(currentData: OnboardingQuestionnaireData) {
        savedStateHandle[VIEW_STATE_KEY] = Question(currentData)
    }

    private fun registerForDarkWebMonitoring() {
        viewModelScope.launch {
            val currentData = uiState.value.viewData
            val email = currentData.email ?: return@launch
            when (darkWebMonitoringManager.optIn(email)) {
                RESULT_OK, RESULT_ALREADY_ACTIVATED -> {
                    userPreferencesManager.putBoolean(
                        QuestionnaireAnswer.KEY_GUIDED_ONBOARDING_DWM_OPT_IN,
                        true
                    )
                    savedStateHandle[VIEW_STATE_KEY] = GoToEmailConfirmation(currentData)
                }
                else -> {
                    savedStateHandle[VIEW_STATE_KEY] = Error(
                        viewData = currentData,
                        error = OnboardingQuestionnaireError.DarkWebRegistrationError
                    )
                }
            }
        }
    }

    companion object {
        internal const val RESULT_OK = "OK"
        internal const val RESULT_ALREADY_ACTIVATED = "USER_HAS_ALREADY_AN_ACTIVE_SUBSCRIPTION"

        internal const val VIEW_STATE_KEY = "view_state_key"
    }
}