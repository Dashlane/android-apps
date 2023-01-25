package com.dashlane.guidedpasswordchange.internal

import android.app.Activity
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import com.airbnb.lottie.LottieAnimationView
import com.dashlane.guidedpasswordchange.R
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.onClick
import com.skocken.presentation.viewproxy.BaseViewProxy

internal class OnboardingGuidedPasswordChangeViewProxy(
    private val activity: Activity,
    domain: String,
    hasInlineAutofill: Boolean
) : BaseViewProxy<OnboardingGuidedPasswordChangeContract.Presenter>(activity),
    OnboardingGuidedPasswordChangeContract.ViewProxy {

    private val illustrations = findViewByIdEfficient<LottieAnimationView>(R.id.illustrations)!!.apply {
        addAnimatorUpdateListener {
            steps.lastOrNull { it.frameNumber <= frame }?.let {
                val index = it.index
                if (currentIllustration != index) currentIllustration = index
            }
        }
    }

    override var currentIllustration = 0
        set(value) {
            field = value
            updateUi()
        }

    init {
        onClick(R.id.button_change_password) { presenter.onChangePasswordClicked() }
        findViewByIdEfficient<TextView>(R.id.title)!!.text =
            activity.getString(R.string.guided_password_change_title, domain)
        forEachStepViews { step, textView, _ ->
            if (step.index == 0) {
                textView.text = activity.getString(R.string.guided_password_change_step_1, domain)
            }
            textView.setOnClickListener {
                illustrations.frame = step.frameNumber
                presenter.onStepClicked(step.index)
            }
        }
        
        if (!hasInlineAutofill) illustrations.setAnimation(R.raw.lottie_guided_change_webcard)
        updateUi()
    }

    override fun showEnableAutofillApiDialog() {
        DialogHelper().builder(context, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
            .setTitle(context.getString(R.string.guided_password_change_enable_autofill_dialog_title))
            .setMessage(context.getString(R.string.guided_password_change_enable_autofill_dialog_message))
            .setPositiveButton(context.getString(R.string.guided_password_change_enable_autofill_dialog_positive_button)) { _, _ -> presenter.onEnableAutofillApiClicked() }
            .setNegativeButton(context.getString(R.string.guided_password_change_enable_autofill_dialog_negative_button)) { _, _ -> activity.finish() }
            .setCancelable(false)
            .show()
    }

    private fun updateUi() {
        forEachStepViews { step, textView, iconView ->
            if (step.index == currentIllustration) {
                iconView.isEnabled = true
                textView.isEnabled = true
            } else {
                iconView.isEnabled = false
                textView.isEnabled = false
            }
        }
    }

    private inline fun forEachStepViews(action: (step: Step, textView: TextView, iconView: ImageView) -> Unit) {
        steps.map { step ->
            action(
                step,
                findViewByIdEfficient(step.textId)!!,
                findViewByIdEfficient(step.iconId)!!
            )
        }
    }

    private data class Step(
        val index: Int,
        @IdRes val textId: Int,
        @IdRes val iconId: Int,
        val frameNumber: Int
    )

    companion object {
        private val steps = listOf(
            Step(
                index = 0,
                textId = R.id.text_step1,
                iconId = R.id.icon_step1,
                frameNumber = 0
            ),
            Step(
                index = 1,
                textId = R.id.text_step2,
                iconId = R.id.icon_step2,
                frameNumber = 100
            ),
            Step(
                index = 2,
                textId = R.id.text_step3,
                iconId = R.id.icon_step3,
                frameNumber = 150
            ),
            Step(
                index = 3,
                textId = R.id.text_step4,
                iconId = R.id.icon_step4,
                frameNumber = 300
            )
        )
    }
}