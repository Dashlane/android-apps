package com.dashlane.createaccount

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.dashlane.R
import com.dashlane.design.component.compat.view.ButtonMediumView
import com.dashlane.util.animation.fadeIn
import com.dashlane.util.animation.fadeOut
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.graphics.ViewAnimatorPager
import com.dashlane.util.onLifecycleEvent
import com.dashlane.util.showToaster
import com.skocken.presentation.viewproxy.BaseViewProxy
import kotlin.properties.Delegates

class CreateAccountViewProxy(rootView: View) :
    BaseViewProxy<CreateAccountContract.Presenter>(rootView),
    CreateAccountContract.ViewProxy {

    override val root: ConstraintLayout = findViewByIdEfficient(R.id.view_login_root)!!
    override val content: ViewAnimatorPager = findViewByIdEfficient(R.id.view_login_content)!!

    override var showProgress: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            showProgress(newValue)
        }
    }

    override var nextEnabled by Delegates.observable(true) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            nextButton.animate().cancel()
            if (newValue) {
                nextButton.animate()
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .alpha(1.0f)
                    .translationY(0.0f)
            } else {
                val bottomMargin =
                    (nextButton.layoutParams as ConstraintLayout.LayoutParams).bottomMargin
                nextButton.animate()
                    .setInterpolator(FastOutLinearInInterpolator())
                    .alpha(0.0f)
                    .translationY((nextButton.height + bottomMargin).toFloat())
            }
        }
    }
    override var mplessButtonVisible: Boolean
        get() = mplessButton.visibility == View.VISIBLE
        set(value) {
            if (value) mplessButton.fadeIn() else mplessButton.fadeOut()
        }

    private val logo: View = findViewByIdEfficient(R.id.logo)!!
    private val progressBar: ProgressBar = findViewByIdEfficient(R.id.view_login_progress)!!
    private val nextButton: Button = findViewByIdEfficient(R.id.view_next)!!
    private val mplessButton: ButtonMediumView = findViewByIdEfficient(R.id.view_passwordless_button)!!
    private val minContentHeight = resources.getDimensionPixelSize(R.dimen.login_content_min_height)

    private var showLogo by Delegates.observable(true) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            if (newValue) {
                ConstraintSet().run {
                    clone(root)
                    clear(R.id.view_login_content, ConstraintSet.TOP)
                    connect(
                        R.id.view_login_content,
                        ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.BOTTOM
                    )
                    applyTo(root)
                }
                logo.visibility = View.VISIBLE
            } else {
                logo.visibility = View.GONE
                ConstraintSet().run {
                    clone(root)
                    clear(R.id.view_login_content, ConstraintSet.TOP)
                    connect(
                        R.id.view_login_content,
                        ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP
                    )
                    applyTo(root)
                }
            }
        }
    }

    init {
        nextButton.setOnClickListener { presenter.onNextClicked() }
        mplessButton.onClick = { presenter.onMplessSetupClicked() }

        root.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val height = bottom - top
            showLogo = height > minContentHeight
        }
    }

    override fun navigateNext(completion: (() -> Unit)?) {
        completion?.let { content.outAnimation.onLifecycleEvent.end(it) }
        content.showNext()
    }

    override fun navigatePrevious() {
        content.showPrevious()
        showProgress(false)
    }

    override fun navigateLast() {
        
        val inAnimation = content.inAnimation
        val outAnimation = content.outAnimation
        content.inAnimation = null
        content.outAnimation = null
        content.displayedChild = content.childCount - 1
        content.inAnimation = inAnimation
        content.outAnimation = outAnimation
    }

    override fun showError(@StringRes errorResId: Int) {
        context.showToaster(errorResId, Toast.LENGTH_SHORT)
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            if (nextEnabled) {
                progressBar.indeterminateTintList =
                    ColorStateList.valueOf(context.getThemeAttrColor(R.attr.colorOnSecondary))
                nextButton.setTextColor(Color.TRANSPARENT)
                nextButton.isEnabled = false
            }
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
            if (nextEnabled) {
                nextButton.setTextColor(context.getThemeAttrColor(R.attr.colorOnSecondary))
                nextButton.isEnabled = true
            }
        }
    }
}