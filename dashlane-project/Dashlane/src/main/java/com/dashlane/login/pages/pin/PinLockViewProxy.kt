package com.dashlane.login.pages.pin

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import com.dashlane.R
import com.dashlane.ui.widgets.PinCodeKeyboardView
import com.dashlane.ui.widgets.PinCodeView
import com.dashlane.util.getThemeAttrColor
import com.skocken.presentation.viewproxy.BaseViewProxy

class PinLockViewProxy(view: View) :
    BaseViewProxy<PinLockContract.Presenter>(view),
    PinLockContract.ViewProxy {

    override var showProgress: Boolean = false
    override var email: String? = null

    val rootView = getRootView<ViewGroup>()
    val pinEnterArea: PinCodeView = findViewByIdEfficient(R.id.pincode_enter_area)!!
    private val pinKeyboard: PinCodeKeyboardView? = findViewByIdEfficient(R.id.pincode_keyboard)
    private val topicView: TextView = findViewByIdEfficient(R.id.topic)!!
    private val questionView: TextView = findViewByIdEfficient(R.id.question)!!
    private val logoutButton: Button = findViewByIdEfficient(R.id.lock_pincode_logout)!!

    private val errorAnimation = AnimationUtils.loadAnimation(context, R.anim.shake)

    private val mErrorColor = context.getColor(R.color.text_warning_standard)
    private val mDefaultColor = context.getColor(R.color.border_neutral_standard_idle)
    private val mUnderLineFocusedColor = context.getThemeAttrColor(R.attr.colorControlActivated)
    private val mUnderLineDefaultColor = mDefaultColor

    override fun setupKeyboard(pinLockKeyboardListener: PinCodeKeyboardView.PinCodeKeyboardListener) {
        if (pinKeyboard == null) {
            PinCodeKeyboardView.setupSoftKeyboard(pinEnterArea, pinLockKeyboardListener)
        } else {
            pinKeyboard.setListener(pinLockKeyboardListener)
        }
    }

    override fun setPinsVisible(quantity: Int) {
        pinEnterArea.setPinsVisible(quantity, mUnderLineFocusedColor, mUnderLineDefaultColor)
    }

    override fun enableAllKeyboardButtons(enabled: Boolean) {
        pinKeyboard?.setEnableButtons(enabled)
    }

    override fun prepareForTransitionStart() {
    }

    override fun prepareForTransitionEnd() {
    }

    override fun init(savedInstanceState: Bundle?) {
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }

    override fun requestFocus() {
        
    }

    override fun showError(errorResId: Int, onClick: () -> Unit) {
        
    }

    override fun showError(error: CharSequence?, onClick: () -> Unit) {
        
    }

    override fun animateSuccess(disableAnimationEffect: Boolean): Int {
        disableChildrenClipping()
        hideAllElementsExceptDots()
        return pinEnterArea.startAnimationDots(rootView, disableAnimationEffect)
    }

    override fun animateError() {
        errorAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                pinEnterArea.setUnderlinesColor(mErrorColor)
            }

            override fun onAnimationEnd(animation: Animation) {
                pinEnterArea.setUnderlinesColor(mDefaultColor)
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        pinEnterArea.startAnimation(errorAnimation)
    }

    override fun setTextError(error: String) {
        questionView.text = error
        questionView.visibility = View.VISIBLE
    }

    override fun setTopic(topic: String) {
        topicView.text = topic
    }

    override fun setQuestion(question: String) {
        questionView.text = question
    }

    override fun initLogoutButton(text: String?, listener: View.OnClickListener) {
        logoutButton.text = text
        logoutButton.setOnClickListener(listener)
    }

    private fun hideAllElementsExceptDots() {
        val anim = AnimationUtils.loadAnimation(context, R.anim.fadeout)
        if (pinKeyboard != null) {
            pinKeyboard.visibility = View.INVISIBLE
            pinKeyboard.startAnimation(anim)
        }
        questionView.visibility = View.INVISIBLE
        questionView.startAnimation(anim)
        topicView.visibility = View.INVISIBLE
        topicView.startAnimation(anim)
        logoutButton.visibility = View.INVISIBLE
        pinEnterArea.setUnderlinesInvisible()
    }

    private fun disableChildrenClipping() {
        rootView.clipChildren = false
        pinEnterArea.clipChildren = false
    }
}