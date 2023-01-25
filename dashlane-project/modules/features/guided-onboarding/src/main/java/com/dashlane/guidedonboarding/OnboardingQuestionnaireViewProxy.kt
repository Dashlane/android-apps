package com.dashlane.guidedonboarding

import android.animation.Animator
import android.animation.LayoutTransition.CHANGING
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.dashlane.design.component.compat.view.BaseButtonView
import com.dashlane.guidedonboarding.widgets.AnswerView
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.ACCOUNT_EMAIL
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.AUTOFILL
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.DIGITAL_TOOL
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.DWM
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.M2W
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.MEMORY
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.OTHER
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getThemeAttrColor
import com.skocken.presentation.viewproxy.BaseViewProxy

internal class OnboardingQuestionnaireViewProxy(private val activity: AppCompatActivity, accountEmail: String) :
    OnboardingQuestionnaireContract.View, BaseViewProxy<OnboardingQuestionnaireContract.Presenter>(activity) {

    private val questionNumber = findViewByIdEfficient<TextView>(R.id.onboarding_questionnaire_question_number)!!
    private val title = findViewByIdEfficient<TextView>(R.id.onboarding_questionnaire_title)!!
    private val animationView = findViewByIdEfficient<LottieAnimationView>(R.id.animationView)!!
    private val subtitle = findViewByIdEfficient<TextView>(R.id.question_subtitle)!!
    private val planReadyTitle = findViewByIdEfficient<TextView>(R.id.plan_ready_title)!!
    private val progressAnimation =
        findViewByIdEfficient<LottieAnimationView>(R.id.progress_animation)!!.apply {
            addLottieOnCompositionLoadedListener {
                addValueCallback(KeyPath("**", "Fill 1"), LottieProperty.COLOR) {
                    context.getThemeAttrColor(R.attr.colorOnBackground)
                }
            }
        }

    private val continueBtn = findViewByIdEfficient<BaseButtonView>(R.id.button_continue)!!
    private val backBtn = findViewByIdEfficient<BaseButtonView>(R.id.button_back)!!
    private val trustBtn = findViewByIdEfficient<BaseButtonView>(R.id.button_trust)!!
    private val skipBtn = findViewByIdEfficient<BaseButtonView>(R.id.button_skip)!!

    private val q1A1 = findViewByIdEfficient<AnswerView>(R.id.q1A1)!!
    private val q1A2 = findViewByIdEfficient<AnswerView>(R.id.q1A2)!!
    private val q1A3 = findViewByIdEfficient<AnswerView>(R.id.q1A3)!!

    private val q2A1 = findViewByIdEfficient<AnswerView>(R.id.q2A1)!!
    private val q2A2 = findViewByIdEfficient<AnswerView>(R.id.q2A2)!!
    private val q2A3 = findViewByIdEfficient<AnswerView>(R.id.q2A3)!!

    private val q3A1 = findViewByIdEfficient<AnswerView>(R.id.q3A1)!!

    private val q1Answers: Map<AnswerView, QuestionnaireAnswer> = mapOf(
        q1A1 to AUTOFILL,
        q1A2 to M2W,
        q1A3 to DWM
    )

    private val q2Answers: Map<AnswerView, QuestionnaireAnswer> = mapOf(
        q2A1 to MEMORY,
        q2A2 to DIGITAL_TOOL,
        q2A3 to OTHER
    )

    private val q3Answers: Map<AnswerView, QuestionnaireAnswer> = mapOf(
        createAccountEmailAnswer(accountEmail) to ACCOUNT_EMAIL
    )

    private val allAnswers = q1Answers.keys.union(q2Answers.keys).union(q3Answers.keys)

    init {
        continueBtn.onClick = {
            presenter.onClickContinue()
        }
        backBtn.onClick = {
            activity.onBackPressedDispatcher.onBackPressed()
        }

        trustBtn.onClick = {
            presenter.onClickTrustButton()
        }

        skipBtn.onClick = {
            presenter.onClickSkipButton()
        }
        ViewCompat.setAccessibilityHeading(title, true)
    }

    override fun showQuestion1(q1SelectedAnswer: QuestionnaireAnswer?) {
        showQuestion(
            q1SelectedAnswer,
            q1Answers,
            1,
            R.string.guided_onboarding_brings_title,
            R.raw.guided_onboarding_lottie_04_onlinelife
        )
        trustBtn.visibility = View.GONE
    }

    override fun showQuestion2(q2SelectedAnswer: QuestionnaireAnswer?) {
        showQuestion(
            q2SelectedAnswer,
            q2Answers,
            2,
            R.string.guided_onboarding_handle_title,
            R.raw.guided_onboarding_lottie_06_bouncing_logos
        )
        animationView.visibility = View.VISIBLE
        subtitle.visibility = View.GONE
        trustBtn.visibility = if (q2SelectedAnswer != null) View.VISIBLE else View.GONE
        skipBtn.visibility = View.GONE
    }

    override fun showQuestion3(q3SelectedAnswer: QuestionnaireAnswer) {
        showQuestion(q3SelectedAnswer, q3Answers, 3, R.string.guided_onboarding_dwm_title, null)
        planReadyTitle.apply {
            (parent as ViewGroup).layoutTransition.disableTransitionType(CHANGING)
            visibility = View.GONE
        }
        progressAnimation.visibility = View.GONE
        trustBtn.visibility = View.GONE
        animationView.visibility = View.GONE
        subtitle.visibility = View.VISIBLE
        skipBtn.visibility = View.VISIBLE
        questionNumber.visibility = View.VISIBLE
        backBtn.visibility = View.VISIBLE
        title.visibility = View.VISIBLE
    }

    private fun showQuestion(
        selectedAnswer: QuestionnaireAnswer?,
        answerMap: Map<AnswerView, QuestionnaireAnswer>,
        currentQuestionNumber: Int,
        @StringRes resTitle: Int,
        @RawRes defaultResAnimation: Int?
    ) {
        questionNumber.text = context.getString(R.string.guided_onboarding_question_number, currentQuestionNumber, 3)
        title.setText(resTitle)
        questionNumber.visibility = View.VISIBLE
        if (defaultResAnimation == null) {
            animationView.visibility = View.GONE
        } else {
            animationView.visibility = View.VISIBLE
            animationView.changeAnimation(selectedAnswer?.lottieRes ?: defaultResAnimation)
        }

        allAnswers.forEach {
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
            val positiveText = if (selectedAnswer == ACCOUNT_EMAIL) {
                R.string.guided_onboarding_dwm_positive_button
            } else {
                R.string.guided_onboarding_continue
            }
            continueBtn.text = continueBtn.context.getString(positiveText)
            continueBtn.visibility = View.VISIBLE
        } else {
            continueBtn.visibility = View.GONE
        }
    }

    override fun showPlanReady(animate: Boolean) {
        fun showAnimEnd() {
            planReadyTitle.setText(R.string.guided_onboarding_plan_ready_title)
            continueBtn.visibility = View.VISIBLE
            skipBtn.visibility = View.GONE
        }
        questionNumber.visibility = View.GONE
        animationView.visibility = View.GONE
        subtitle.visibility = View.GONE
        allAnswers.forEach {
            it.visibility = View.GONE
        }
        trustBtn.visibility = View.GONE
        backBtn.visibility = View.GONE
        title.visibility = View.GONE
        continueBtn.apply {
            visibility = View.GONE
            text = context.getString(R.string.guided_onboarding_continue)
        }
        skipBtn.visibility = View.VISIBLE
        planReadyTitle.apply {
            setText(R.string.guided_onboarding_building_plan_title)
            visibility = View.VISIBLE
            (parent as ViewGroup).layoutTransition.enableTransitionType(CHANGING)
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

    override fun showDarkWebRegistrationError() {
        SnackbarUtils.showSnackbar(activity, context.getString(R.string.darkweb_setup_mail_error))
    }

    private fun onAnswerClicked(view: View) {
        if (view !is AnswerView) return
        val map = getCorrespondingAnswerMap(view)
        map.keys.forEach {
            if (it != view) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
            }
        }
        continueBtn.visibility = View.VISIBLE
        if (map == q2Answers) {
            trustBtn.visibility = View.VISIBLE
        }
        view.showDetail = true
        val answer = map[view]
        answer?.let {
            if (it.lottieRes > 0) animationView.changeAnimation(it.lottieRes)
        }
        presenter.onAnswerSelected(getQuestionIndexForAnswerView(view), answer)
    }

    private fun getQuestionIndexForAnswerView(answer: AnswerView) = when (getCorrespondingAnswerMap(answer)) {
        q1Answers -> 0
        q2Answers -> 1
        q3Answers -> 2
        else -> -1
    }

    private fun getCorrespondingAnswerMap(answer: AnswerView) = when (answer) {
        in q1Answers -> q1Answers
        in q2Answers -> q2Answers
        in q3Answers -> q3Answers
        else -> emptyMap()
    }

    private fun LottieAnimationView.changeAnimation(@RawRes resId: Int) {
        pauseAnimation()
        setAnimation(resId)
        playAnimation()
    }

    private fun createAccountEmailAnswer(email: String) = q3A1.apply {
        findViewById<TextView>(R.id.title).text = email
    }
}