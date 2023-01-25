package com.dashlane.csvimport.internal.onboardingchromeimport

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.transition.TransitionManager
import com.dashlane.csvimport.R
import com.dashlane.csvimport.internal.OnSwipeTouchListener
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.onClick
import com.rd.PageIndicatorView
import com.skocken.presentation.viewproxy.BaseViewProxy

internal class OnboardingChromeImportViewProxy(
    activity: Activity
) : BaseViewProxy<OnboardingChromeImportContract.Presenter>(activity),
    OnboardingChromeImportContract.ViewProxy {
    private val illustrationContent = findViewByIdEfficient<ViewGroup>(R.id.illustration_content)!!

    private val pageIndicator = findViewByIdEfficient<PageIndicatorView>(R.id.page_indicator)!!.apply {
        count = steps.size
    }

    private val swipeTouchListener = object : OnSwipeTouchListener(context) {
        override fun onSwipeRight(): Boolean {
            presenter.onSwipeRight()
            return true
        }

        override fun onSwipeLeft(): Boolean {
            presenter.onSwipeLeft()
            return true
        }
    }

    private val errorDialog = DialogHelper().builder(context)
        .setTitle(R.string.chrome_import_onboarding_error_title)
        .setMessage(R.string.chrome_import_onboarding_error_message)
        .setPositiveButton(R.string.chrome_import_onboarding_error_primary_cta) { _, _ ->
            presenter.onImportErrorSkipClicked()
        }
        .setNegativeButton(R.string.chrome_import_onboarding_error_secondary_cta) { dialog, _ -> dialog.cancel() }
        .setCancelable(true)
        .create()
        .apply {
            setOnCancelListener { presenter.onImportErrorCanceled() }
        }

    override var currentIllustration = 0
        set(value) {
            field = value
            updateUi()
        }

    override val illustrationCount
        get() = steps.size

    init {
        
        getRootView<View>().setOnTouchListener(swipeTouchListener)
        findViewByIdEfficient<View>(R.id.illustrations)!!.setOnTouchListener(swipeTouchListener)

        onClick(R.id.button_maybe_later) { presenter.onMayBeLaterClicked() }
        onClick(R.id.button_open_chrome) { presenter.onOpenChromeClicked() }

        forEachStepViewsIndexed { index, textView, _ ->
            textView.setOnClickListener {
                presenter.onStepClicked(index)
            }
        }
        ViewCompat.setAccessibilityHeading(findViewByIdEfficient<TextView>(R.id.title)!!, true)

        updateUi()
    }

    override fun showImportError() = errorDialog.show()

    private fun updateUi() {
        with(illustrationContent) {
            TransitionManager.beginDelayedTransition(this)
            removeAllViews()
            LayoutInflater.from(context).inflate(steps[currentIllustration].illustrationLayoutId, this, true)
        }

        forEachStepViewsIndexed { index, textView, iconView ->
            iconView.isEnabled = index == currentIllustration
            textView.isEnabled = index == currentIllustration
        }

        pageIndicator.setSelected(currentIllustration)
    }

    private inline fun forEachStepViewsIndexed(action: (index: Int, textView: TextView, iconView: ImageView) -> Unit) {
        steps.mapIndexed { index, step ->
            action(
                index,
                findViewByIdEfficient(step.textId)!!,
                findViewByIdEfficient(step.iconId)!!
            )
        }
    }

    private data class Step(
        @IdRes val textId: Int,
        @IdRes val iconId: Int,
        @LayoutRes val illustrationLayoutId: Int
    )

    companion object {
        private val steps = listOf(
            Step(
                textId = R.id.text_step1,
                iconId = R.id.icon_step1,
                illustrationLayoutId = R.layout.layout_onboarding_chrome_import_illustration_step1
            ),
            Step(
                textId = R.id.text_step2,
                iconId = R.id.icon_step2,
                illustrationLayoutId = R.layout.layout_onboarding_chrome_import_illustration_step2
            ),
            Step(
                textId = R.id.text_step3,
                iconId = R.id.icon_step3,
                illustrationLayoutId = R.layout.layout_onboarding_chrome_import_illustration_step3
            ),
            Step(
                textId = R.id.text_step4,
                iconId = R.id.icon_step4,
                illustrationLayoutId = R.layout.layout_onboarding_chrome_import_illustration_step4
            )
        )
    }
}