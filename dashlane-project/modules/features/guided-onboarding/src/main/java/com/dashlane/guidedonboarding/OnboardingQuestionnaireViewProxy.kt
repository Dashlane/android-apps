package com.dashlane.guidedonboarding

import android.animation.Animator
import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.dashlane.guidedonboarding.databinding.ActivityOnboardingQuestionnaireBinding
import com.dashlane.guidedonboarding.widgets.AnswerView
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer
import com.dashlane.guidedonboarding.widgets.QuestionnaireStep
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.logPageView
import kotlinx.coroutines.launch

internal class OnboardingQuestionnaireViewProxy(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val binding: ActivityOnboardingQuestionnaireBinding,
    private val viewModel: OnboardingQuestionnaireViewModel,
    onBackClick: () -> Unit,
    onTrustClick: () -> Unit,
    private val onEnded: (Intent) -> Unit,
    private val onGoToDWM: () -> Unit,
    private val onGoToEmailConfirmation: (String?) -> Unit,
    private val onCancel: () -> Unit,
    private val onDarkWebRegistrationError: () -> Unit
) {
    private var progressAnimation: LottieAnimationView
    private var preQuestionAnswers: Map<AnswerView, QuestionnaireAnswer>
    private var q1Answers: Map<AnswerView, QuestionnaireAnswer>
    private var q2Answers: Map<AnswerView, QuestionnaireAnswer>
    private lateinit var q3Answers: Map<AnswerView, QuestionnaireAnswer>
    private lateinit var allAnswers: Set<AnswerView>

    init {
        binding.buttonContinue.onClick = {
            viewModel.onClickContinue()
        }
        binding.buttonBack.onClick = onBackClick

        binding.buttonTrust.onClick = onTrustClick

        binding.buttonSkip.onClick = {
            viewModel.onClickSkipButton()
        }

        progressAnimation = binding.progressAnimation.apply {
            addLottieOnCompositionLoadedListener {
                addValueCallback(KeyPath("**", "Fill 1"), LottieProperty.COLOR) {
                    context.getThemeAttrColor(R.attr.colorOnBackground)
                }
            }
        }

        preQuestionAnswers = mapOf(
            binding.preQ1A1 to QuestionnaireAnswer.NAIVE_USER,
            binding.preQ1A2 to QuestionnaireAnswer.NEW_USER,
            binding.preQ1A3 to QuestionnaireAnswer.EXISTING_USER
        )
        q1Answers = mapOf(
            binding.q1A1 to QuestionnaireAnswer.AUTOFILL,
            binding.q1A2 to QuestionnaireAnswer.M2W,
            binding.q1A3 to QuestionnaireAnswer.DWM
        )
        q2Answers = mapOf(
            binding.q2A1 to QuestionnaireAnswer.MEMORY,
            binding.q2A2 to QuestionnaireAnswer.DIGITAL_TOOL,
            binding.q2A3 to QuestionnaireAnswer.OTHER
        )

        ViewCompat.setAccessibilityHeading(binding.onboardingQuestionnaireTitle, true)

        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    collectUiState()
                }
            }
        }
    }

    private suspend fun collectUiState() {
        viewModel.uiState.collect { viewState ->

            if (!this::q3Answers.isInitialized || !this::allAnswers.isInitialized) {
                viewState.viewData.email?.let { email ->
                    q3Answers =
                        mapOf(createAccountEmailAnswer(email) to QuestionnaireAnswer.ACCOUNT_EMAIL)
                    allAnswers = preQuestionAnswers.keys.union(q1Answers.keys).union(q2Answers.keys)
                        .union(q3Answers.keys)
                }
            }

            when (viewState) {
                is OnboardingQuestionnaireState.Question -> {
                    showQuestion(viewState.viewData)
                }
                is OnboardingQuestionnaireState.Plan -> {
                    showPlanReady(viewState.animate)
                }
                is OnboardingQuestionnaireState.Error -> {
                    if (viewState.error is OnboardingQuestionnaireError.DarkWebRegistrationError) {
                        onDarkWebRegistrationError()
                    }
                }
                is OnboardingQuestionnaireState.End -> onEnded(viewState.intent)
                is OnboardingQuestionnaireState.GoToDWM -> onGoToDWM()
                is OnboardingQuestionnaireState.GoToEmailConfirmation -> onGoToEmailConfirmation(
                    viewState.viewData.email
                )
                is OnboardingQuestionnaireState.Cancel -> onCancel()
                is OnboardingQuestionnaireState.HasNavigated -> Unit
            }
        }
    }

    private fun showQuestion(viewData: OnboardingQuestionnaireData) {
        when (viewData.step) {
            QuestionnaireStep.PRE_QUESTION -> showPreQuestion(viewData.answers[QuestionnaireStep.PRE_QUESTION])
            QuestionnaireStep.QUESTION_1 -> showQuestion1(viewData.answers[QuestionnaireStep.QUESTION_1])
            QuestionnaireStep.QUESTION_2 -> showQuestion2(viewData.answers[QuestionnaireStep.QUESTION_2])
            QuestionnaireStep.QUESTION_3 -> showQuestion3(viewData.answers[QuestionnaireStep.PRE_QUESTION])
            else -> {
                
            }
        }
    }

    private fun showPreQuestion(q1SelectedAnswer: QuestionnaireAnswer?) {
        context.logPageView(AnyPage.USER_PROFILING_FAMILIARITY_WITH_DASHLANE)
        showQuestion(
            selectedAnswer = q1SelectedAnswer,
            answerMap = preQuestionAnswers,
            currentQuestionNumber = null,
            resTitle = R.string.guided_onboarding_pre_question_title,
            defaultResAnimation = null
        )
        binding.buttonContinue.visibility = View.GONE
        binding.buttonSkip.visibility = View.GONE
        binding.buttonTrust.visibility = View.GONE
        binding.questionSubtitle.visibility = View.GONE
    }

    private fun showQuestion1(q1SelectedAnswer: QuestionnaireAnswer?) {
        showQuestion(
            selectedAnswer = q1SelectedAnswer,
            answerMap = q1Answers,
            currentQuestionNumber = 1,
            resTitle = R.string.guided_onboarding_brings_title,
            defaultResAnimation = R.raw.guided_onboarding_lottie_04_onlinelife
        )
        binding.buttonSkip.visibility = View.VISIBLE
        binding.buttonTrust.visibility = View.GONE
        binding.questionSubtitle.visibility = View.VISIBLE
    }

    private fun showQuestion2(q2SelectedAnswer: QuestionnaireAnswer?) {
        showQuestion(
            selectedAnswer = q2SelectedAnswer,
            answerMap = q2Answers,
            currentQuestionNumber = 2,
            resTitle = R.string.guided_onboarding_handle_title,
            defaultResAnimation = R.raw.guided_onboarding_lottie_06_bouncing_logos
        )
        binding.animationView.visibility = View.VISIBLE
        binding.questionSubtitle.visibility = View.GONE
        binding.buttonTrust.visibility = if (q2SelectedAnswer != null) View.VISIBLE else View.GONE
        binding.buttonSkip.visibility = if (q2SelectedAnswer != null) View.GONE else View.VISIBLE
    }

    private fun showQuestion3(q1SelectedAnswer: QuestionnaireAnswer?) {
        showQuestion(
            selectedAnswer = QuestionnaireAnswer.ACCOUNT_EMAIL,
            answerMap = q3Answers,
            currentQuestionNumber = if (q1SelectedAnswer == QuestionnaireAnswer.EXISTING_USER) {
                null
            } else {
                3
            },
            resTitle = R.string.guided_onboarding_dwm_title,
            defaultResAnimation = null
        )
        binding.questionSubtitle.visibility = View.VISIBLE
        binding.planReadyTitle.apply {
            (parent as ViewGroup).layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
            visibility = View.GONE
        }
        progressAnimation.visibility = View.GONE
        binding.buttonTrust.visibility = View.GONE
        binding.animationView.visibility = View.GONE
        binding.onboardingQuestionnaireTitle.visibility = View.VISIBLE
        binding.buttonSkip.visibility = View.VISIBLE
        binding.onboardingQuestionnaireQuestionNumber.visibility = View.VISIBLE
        binding.buttonBack.visibility = View.VISIBLE
        binding.onboardingQuestionnaireTitle.visibility = View.VISIBLE
    }

    private fun showQuestion(
        selectedAnswer: QuestionnaireAnswer?,
        answerMap: Map<AnswerView, QuestionnaireAnswer>,
        currentQuestionNumber: Int?,
        @StringRes resTitle: Int,
        @RawRes defaultResAnimation: Int?
    ) {
        if (currentQuestionNumber == null) {
            binding.onboardingQuestionnaireQuestionNumber.visibility = View.GONE
        } else {
            binding.onboardingQuestionnaireQuestionNumber.visibility = View.VISIBLE
            binding.onboardingQuestionnaireQuestionNumber.text =
                context.getString(
                    R.string.guided_onboarding_question_number,
                    currentQuestionNumber,
                    3
                )
        }
        binding.onboardingQuestionnaireTitle.setText(resTitle)
        binding.onboardingQuestionnaireQuestionNumber.visibility = View.VISIBLE
        if (defaultResAnimation == null) {
            binding.animationView.visibility = View.GONE
        } else {
            binding.animationView.visibility = View.VISIBLE
            binding.animationView.changeAnimation(selectedAnswer?.lottieRes ?: defaultResAnimation)
        }

        allAnswers.filterNot { it in answerMap }.forEach {
            it.visibility = View.GONE
        }

        answerMap.forEach {
            val view = it.key
            val answer = it.value
            view.showDetail = answer == selectedAnswer
            view.visibility = if (selectedAnswer == null || answer == selectedAnswer) {
                View.VISIBLE
            } else {
                View.GONE
            }
            view.setOnClickListener(this::onAnswerClicked)
        }

        if (selectedAnswer != null) {
            val positiveText = if (selectedAnswer == QuestionnaireAnswer.ACCOUNT_EMAIL) {
                R.string.guided_onboarding_dwm_positive_button
            } else {
                R.string.guided_onboarding_continue
            }
            binding.buttonContinue.text = context.getString(positiveText)
            binding.buttonContinue.visibility = View.VISIBLE
        } else {
            binding.buttonContinue.visibility = View.GONE
        }
    }

    private fun showPlanReady(animate: Boolean) {
        fun showAnimEnd() {
            binding.planReadyTitle.setText(R.string.guided_onboarding_plan_ready_title)
            binding.buttonContinue.visibility = View.VISIBLE
            binding.buttonSkip.visibility = View.GONE
            viewModel.onPlanAnimationDone()
        }
        binding.onboardingQuestionnaireQuestionNumber.visibility = View.GONE
        binding.animationView.visibility = View.GONE
        binding.questionSubtitle.visibility = View.GONE
        allAnswers.forEach {
            it.visibility = View.GONE
        }
        binding.buttonTrust.visibility = View.GONE
        binding.buttonBack.visibility = View.GONE
        binding.onboardingQuestionnaireTitle.visibility = View.GONE
        binding.buttonContinue.apply {
            visibility = View.GONE
            text = context.getString(R.string.guided_onboarding_continue)
        }
        binding.buttonSkip.visibility = View.VISIBLE
        binding.planReadyTitle.apply {
            setText(R.string.guided_onboarding_building_plan_title)
            visibility = View.VISIBLE
            (parent as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        }
        progressAnimation.apply {
            visibility = View.VISIBLE
            if (animate) {
                repeatCount = 2
                removeAllAnimatorListeners()
                addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationCancel(p0: Animator) = Unit
                    override fun onAnimationStart(p0: Animator) = Unit
                    override fun onAnimationRepeat(p0: Animator) = Unit
                    override fun onAnimationEnd(p0: Animator) = showAnimEnd()
                })
                playAnimation()
            } else {
                progress = 1f
                showAnimEnd()
            }
        }
    }

    private fun onAnswerClicked(view: View) {
        if (view !is AnswerView) return
        val map = getCorrespondingAnswerMap(view)
        val answer = map[view]
        viewModel.onAnswerSelected(getQuestionStepForAnswerView(view), answer)
    }

    private fun getCorrespondingAnswerMap(answer: AnswerView) = when (answer) {
        in preQuestionAnswers -> preQuestionAnswers
        in q1Answers -> q1Answers
        in q2Answers -> q2Answers
        in q3Answers -> q3Answers
        else -> emptyMap()
    }

    private fun getQuestionStepForAnswerView(answer: AnswerView) =
        when (getCorrespondingAnswerMap(answer)) {
            preQuestionAnswers -> QuestionnaireStep.PRE_QUESTION
            q1Answers -> QuestionnaireStep.QUESTION_1
            q2Answers -> QuestionnaireStep.QUESTION_2
            q3Answers -> QuestionnaireStep.QUESTION_3
            else -> null
        }

    private fun createAccountEmailAnswer(email: String) = binding.q3A1.apply {
        findViewById<TextView>(R.id.title).text = email
    }

    private fun LottieAnimationView.changeAnimation(@RawRes resId: Int) {
        pauseAnimation()
        setAnimation(resId)
        playAnimation()
    }
}