package com.dashlane.activatetotp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.dashlane.activatetotp.databinding.ActivateTotpErrorBinding
import com.dashlane.activatetotp.databinding.ActivateTotpLoadingBinding

internal fun ActivateTotpErrorBinding.setup(
    titleResId: Int,
    descriptionResId: Int,
    positiveButtonResId: Int,
    negativeButtonResId: Int? = null,
    onClickPositiveButton: () -> Unit,
    onClickNegativeButton: (() -> Unit)? = null
) {
    val minContentHeight = root.context.resources.getDimensionPixelSize(R.dimen.size_480dp)

    
    root.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
        val height = bottom - top
        val hasEnoughHeight = height > minContentHeight
        root.post { space.isVisible = hasEnoughHeight }
    }

    title.setText(titleResId)

    description.setText(descriptionResId)

    buttonPositive.run {
        setText(positiveButtonResId)
        setOnClickListener { onClickPositiveButton() }
    }

    negativeButtonResId?.let { resId ->
        buttonNegative.run {
            setText(resId)
            isVisible = true
            setOnClickListener { onClickNegativeButton?.invoke() }
        }
    }
}

internal fun ActivateTotpLoadingBinding.setup(
    messageResId: Int,
    isSuccess: Boolean = false
) {
    lottie.run {
        removeAllAnimatorListeners()

        addLottieOnCompositionLoadedListener {
            val color = context.getColor(R.color.text_brand_quiet)

            addValueCallback(KeyPath("load", "**"), LottieProperty.STROKE_COLOR) { color }
            addValueCallback(KeyPath("load 2", "**"), LottieProperty.STROKE_COLOR) { color }
            addValueCallback(KeyPath("load 3", "**"), LottieProperty.STROKE_COLOR) { color }
            addValueCallback(KeyPath("Layer 1 copy Outlines", "**"), LottieProperty.COLOR) { color }
        }

        if (isSuccess) {
            addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setAnimation(R.raw.lottie_loading_success)
                    repeatCount = 1
                }
            })
            repeatCount = 0
        } else {
            setAnimation(R.raw.lottie_loading_indeterminate)
            repeatCount = LottieDrawable.INFINITE
        }

        playAnimation()
    }

    message.setText(messageResId)
}