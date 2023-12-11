package com.dashlane.login.pages.authenticator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.dashlane.R
import com.dashlane.login.pages.LoginBaseSubViewProxy
import kotlin.properties.Delegates

class LoginDashlaneAuthenticatorViewProxy(view: View) :
    LoginBaseSubViewProxy<LoginDashlaneAuthenticatorContract.Presenter>(view),
    LoginDashlaneAuthenticatorContract.ViewProxy {

    private val loaderView get() = findViewByIdEfficient<LottieAnimationView>(R.id.loader)!!
    private val messageView get() = findViewByIdEfficient<TextView>(R.id.message)!!
    private val useAlternativeView get() = findViewByIdEfficient<TextView>(R.id.use_alternative)!!
    private val resendRequest get() = findViewByIdEfficient<View>(R.id.resend_request)!!

    override var showTotpAvailable by Delegates.observable(false) { _, _, totp ->
        useAlternativeView.setText(
            if (totp) {
                R.string.login_dashlane_authenticator_use_totp
            } else {
                R.string.login_dashlane_authenticator_use_email_token
            }
        )
    }

    init {
        useAlternativeView.setOnClickListener { presenter.onUseAlternativeClicked() }
        resendRequest.setOnClickListener { presenter.onResendRequestClicked() }

        loaderView.addLottieOnCompositionLoadedListener {
            loaderView.addValueCallback(
                KeyPath("load", "**"),
                LottieProperty.STROKE_COLOR
            ) { context.getColor(R.color.border_neutral_quiet_idle) }
            loaderView.addValueCallback(
                KeyPath("load 2", "**"),
                LottieProperty.STROKE_COLOR
            ) { context.getColor(R.color.text_brand_standard) }
            loaderView.addValueCallback(
                KeyPath("load 3", "**"),
                LottieProperty.STROKE_COLOR
            ) { context.getColor(R.color.text_positive_quiet) }
            loaderView.addValueCallback(
                KeyPath("load 4", "**"),
                LottieProperty.STROKE_COLOR
            ) { context.getColor(R.color.text_danger_quiet) }
            loaderView.addValueCallback(
                KeyPath("Layer 1 copy Outlines", "**"),
                LottieProperty.COLOR
            ) { context.getColor(R.color.text_positive_quiet) }
            loaderView.addValueCallback(
                KeyPath("Layer 1 Outlines", "**"),
                LottieProperty.COLOR
            ) { context.getColor(R.color.text_danger_quiet) }
        }
    }

    override fun showLoading() {
        messageView.text = context.getText(R.string.login_dashlane_authenticator_message_in_progress)
        resendRequest.isInvisible = true
        useAlternativeView.isVisible = true

        updateLoader(
            resId = R.raw.lottie_loading_indeterminate,
            repeatCount = LottieDrawable.INFINITE
        )
    }

    override fun showSuccess(onAnimEnd: () -> Unit) {
        messageView.text = context.getText(R.string.login_dashlane_authenticator_request_approved)
        resendRequest.isInvisible = true
        useAlternativeView.isInvisible = true

        updateLoader(
            resId = R.raw.lottie_loading_success,
            repeatCount = 0,
            onAnimEnd = onAnimEnd
        )
    }

    override fun showError(error: CharSequence?, onClick: () -> Unit) {
        messageView.text = error
        resendRequest.isVisible = true
        useAlternativeView.isVisible = true

        updateLoader(
            resId = R.raw.lottie_loading_fail,
            repeatCount = 0
        )
    }

    override fun requestFocus() = Unit

    private fun updateLoader(
        @RawRes resId: Int,
        repeatCount: Int,
        onAnimEnd: (() -> Unit)? = null
    ) {
        loaderView.removeAllAnimatorListeners()
        loaderView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onAnimEnd?.invoke()
            }
        })
        loaderView.setAnimation(resId)
        loaderView.repeatCount = repeatCount
        loaderView.playAnimation()
    }
}