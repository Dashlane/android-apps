package com.dashlane.guidedonboarding

import android.animation.Animator
import android.animation.LayoutTransition
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.dashlane.darkweb.ui.result.DarkWebSetupResultActivity
import com.dashlane.darkweb.ui.setup.DarkWebSetupMailActivity
import com.dashlane.guidedonboarding.darkwebmonitoring.OnboardingDarkWebMonitoringActivity
import com.dashlane.guidedonboarding.databinding.ActivityOnboardingQuestionnaireBinding
import com.dashlane.guidedonboarding.widgets.AnswerView
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer
import com.dashlane.guidedonboarding.widgets.QuestionnaireStep
import com.dashlane.navigation.NavigationConstants
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.startActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingQuestionnaireActivity : DashlaneActivity() {

    override var requireUserUnlock: Boolean = false
    private lateinit var binding: ActivityOnboardingQuestionnaireBinding

    private lateinit var progressAnimation: LottieAnimationView
    private lateinit var q1Answers: Map<AnswerView, QuestionnaireAnswer>
    private lateinit var q2Answers: Map<AnswerView, QuestionnaireAnswer>
    private lateinit var q3Answers: Map<AnswerView, QuestionnaireAnswer>
    private lateinit var allAnswers: Set<AnswerView>

    private val viewModel by viewModels<OnboardingQuestionnaireViewModel>()
    private val onboardingDWMLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.onEmailConfirmationResult(it)
    }
    private val darkWebSetupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.onDwmResult(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingQuestionnaireBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressed()
            }
        })

        binding.buttonContinue.onClick = {
            viewModel.onClickContinue()
        }
        binding.buttonBack.onClick = {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonTrust.onClick = {
            startActivity<TrustFAQActivity>(R.anim.slide_in_bottom, R.anim.no_animation) {}
        }

        binding.buttonSkip.onClick = {
            viewModel.onClickSkipButton()
        }

        progressAnimation = binding.progressAnimation.apply {
            addLottieOnCompositionLoadedListener {
                addValueCallback(KeyPath("**", "Fill 1"), LottieProperty.COLOR) {
                    getThemeAttrColor(R.attr.colorOnBackground)
                }
            }
        }

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                    q3Answers = mapOf(createAccountEmailAnswer(email) to QuestionnaireAnswer.ACCOUNT_EMAIL)
                    allAnswers = q1Answers.keys.union(q2Answers.keys).union(q3Answers.keys)
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
                        showDarkWebRegistrationError()
                    }
                }
                is OnboardingQuestionnaireState.End -> {
                    val intent = viewState.intent.putExtra(
                        NavigationConstants.STARTED_WITH_INTENT,
                        this.intent.getParcelableExtra<Parcelable?>(NavigationConstants.STARTED_WITH_INTENT)
                    )
                    startActivity(intent)
                    finish()
                }
                is OnboardingQuestionnaireState.GoToDWM -> {
                    viewModel.hasNavigated()
                    onboardingDWMLauncher.launch(Intent(this@OnboardingQuestionnaireActivity, OnboardingDarkWebMonitoringActivity::class.java))
                }
                is OnboardingQuestionnaireState.GoToEmailConfirmation -> {
                    viewModel.hasNavigated()
                    val intent = Intent(this@OnboardingQuestionnaireActivity, DarkWebSetupResultActivity::class.java).apply {
                        putExtra(DarkWebSetupMailActivity.INTENT_SIGN_UP_MAIL, viewState.viewData.email)
                    }
                    darkWebSetupLauncher.launch(intent)
                }
                is OnboardingQuestionnaireState.Cancel -> finish()
                is OnboardingQuestionnaireState.HasNavigated -> Unit
            }
        }
    }

    private fun showQuestion(viewData: OnboardingQuestionnaireData) {
        when (viewData.step) {
            QuestionnaireStep.QUESTION_1 -> showQuestion1(viewData.answers[QuestionnaireStep.QUESTION_1])
            QuestionnaireStep.QUESTION_2 -> showQuestion2(viewData.answers[QuestionnaireStep.QUESTION_2])
            QuestionnaireStep.QUESTION_3 -> showQuestion3()
            else -> {
                
            }
        }
    }

    private fun showQuestion1(q1SelectedAnswer: QuestionnaireAnswer?) {
        showQuestion(
            selectedAnswer = q1SelectedAnswer,
            answerMap = q1Answers,
            currentQuestionNumber = 1,
            resTitle = R.string.guided_onboarding_brings_title,
            defaultResAnimation = R.raw.guided_onboarding_lottie_04_onlinelife
        )
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
        binding.buttonSkip.visibility = View.GONE
    }

    private fun showQuestion3() {
        showQuestion(
            selectedAnswer = QuestionnaireAnswer.ACCOUNT_EMAIL,
            answerMap = q3Answers,
            currentQuestionNumber = 3,
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
        currentQuestionNumber: Int,
        @StringRes resTitle: Int,
        @RawRes defaultResAnimation: Int?
    ) {
        binding.onboardingQuestionnaireQuestionNumber.text = getString(R.string.guided_onboarding_question_number, currentQuestionNumber, 3)
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
            binding.buttonContinue.text = getString(positiveText)
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
            text = getString(R.string.guided_onboarding_continue)
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

    private fun showDarkWebRegistrationError() {
        SnackbarUtils.showSnackbar(this, getString(R.string.darkweb_setup_mail_error))
    }

    private fun onAnswerClicked(view: View) {
        if (view !is AnswerView) return
        val map = getCorrespondingAnswerMap(view)
        val answer = map[view]
        viewModel.onAnswerSelected(getQuestionStepForAnswerView(view), answer)
    }

    private fun getCorrespondingAnswerMap(answer: AnswerView) = when (answer) {
        in q1Answers -> q1Answers
        in q2Answers -> q2Answers
        in q3Answers -> q3Answers
        else -> emptyMap()
    }

    private fun getQuestionStepForAnswerView(answer: AnswerView) = when (getCorrespondingAnswerMap(answer)) {
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