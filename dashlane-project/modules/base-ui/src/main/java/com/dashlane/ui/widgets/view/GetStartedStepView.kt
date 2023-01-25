package com.dashlane.ui.widgets.view

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.res.getColorStateListOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.withStyledAttributes
import androidx.core.view.doOnPreDraw
import androidx.transition.AutoTransition
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable.INFINITE
import com.dashlane.ui.R
import com.dashlane.util.TextColorTransition
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.parcelize.Parcelize

class GetStartedStepView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.getStartedStepViewStyle
) : ConstraintLayout(
    ContextThemeWrapper(context, R.style.ThemeOverlay_Dashlane_Shape),
    attrs,
    defStyleAttr
) {

    val title: TextView
    val subtitle: TextView
    val completionDuration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
    private val check: ImageView
    private val number: TextView
    private val preview: LottieAnimationView
    private val previewForeground: LottieAnimationView
    private val cta: Button
    private val expandGroup: Group
    private val expandDuration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    private val expandTransition = TransitionSet().apply {
        addTransition(Fade())
        addTransition(ChangeBounds())
        ordering = TransitionSet.ORDERING_TOGETHER
    }
    private val completionStartDelay = resources.getInteger(android.R.integer.config_longAnimTime).toLong()

    @IdRes
    var sceneRootIdRes: Int? = null
    var onExpandedChangeListener: ChecklistGroup.OnExpandChangeListener? = null

    private lateinit var defaultTitleColor: ColorStateList
    private lateinit var completionColor: ColorStateList

    var completion: Boolean = false
        set(value) {
            field = value
            updateCompletionStatus()
        }

    var expanded: Boolean = false
        private set

    init {
        inflate(context, R.layout.getstarted_step, this)
        check = findViewById(R.id.checkStep)
        number = findViewById(R.id.numberStep)
        title = findViewById(R.id.titleStep)
        subtitle = findViewById(R.id.subtitleStep)
        preview = findViewById(R.id.previewStep)
        previewForeground = findViewById(R.id.previewForegroundStep)
        cta = findViewById(R.id.ctaStep)
        expandGroup = findViewById(R.id.expandGroupStep)
        init(attrs, defStyleAttr)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SaveState(
            superState = super.onSaveInstanceState(),
            expanded = expanded,
            completion = completion,
            sceneRootIdRes = sceneRootIdRes
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? SaveState)?.let { saveState ->
            super.onRestoreInstanceState(saveState.superState)
            setExpanded(saveState.expanded, false)
            completion = saveState.completion
            sceneRootIdRes = saveState.sceneRootIdRes
        }
    }

    fun setCtaOnClickListener(clickListener: OnClickListener?) {
        cta.setOnClickListener(clickListener)
    }

    fun setExpanded(value: Boolean, animate: Boolean = false, duration: Long? = null) {
        if (expanded != value) {
            expanded = value
            updateExpandState(value, animate, duration)
        }
    }

    fun animateCompletion(startListener: (() -> Unit)? = null, endListener: (() -> Unit)? = null) {
        if (!completion) {
            val completionTransition = buildCompletionTransition(startListener, endListener)
            expandGroup.visibility = View.VISIBLE
            completion = false
            
            doOnPreDraw {
                
                postDelayed(
                    {
                        TransitionManager.beginDelayedTransition(getSceneRoot(), completionTransition)
                        expandGroup.visibility = View.GONE
                        completion = true
                        requestLayout()
                    },
                    completionStartDelay
                )
            }
        }
    }

    private fun setNumber(value: Int) {
        number.text = String.format("%02d", value)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            attrs, R.styleable.GetStartedStepView, defStyleAttr, R.style.Widget_Dashlane_GetStartedStepView
        ) {
            completionColor = getColorStateListOrThrow(R.styleable.GetStartedStepView_stepCompletionColor)
            check.imageTintList = completionColor
            initNumber(this)
            title.text = getString(R.styleable.GetStartedStepView_stepTitle)
            defaultTitleColor = getColorStateListOrThrow(R.styleable.GetStartedStepView_stepTitleColor)
            title.setTextColor(defaultTitleColor)
            subtitle.text = getString(R.styleable.GetStartedStepView_stepSubtitle)
            initPreviews(this)
            cta.text = getString(R.styleable.GetStartedStepView_stepCta)
            completion = getBoolean(R.styleable.GetStartedStepView_completion, false)
            elevation = getDimensionOrThrow(R.styleable.GetStartedStepView_android_elevation)
            initBackground(this, attrs)
            addForegroundRipple()
            initExpand()
        }
    }

    private fun updateCompletionStatus() {
        if (completion) {
            number.visibility = View.GONE
            check.visibility = View.VISIBLE
            title.setTextColor(completionColor)
            isEnabled = false
        } else {
            number.visibility = View.VISIBLE
            check.visibility = View.GONE
            title.setTextColor(defaultTitleColor)
            isEnabled = true
        }
    }

    private fun setPreviewForegroundAnimation(rawRes: Int) {
        if (rawRes != 0) {
            previewForeground.apply {
                progress = 0.5f
                setAnimation(rawRes)
            }
        }
    }

    private fun setPreviewAnimation(rawRes: Int) {
        if (rawRes != 0) {
            preview.apply {
                repeatCount = INFINITE
                setAnimation(rawRes)
                playAnimation()
            }
        }
    }

    private fun addForegroundRipple() {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        val resId = outValue.resourceId
        if (resId != 0) {
            foreground = context.getDrawable(resId)
        }
    }

    private fun updateExpandState(value: Boolean, animate: Boolean = false, duration: Long? = null) {
        onExpandedChangeListener?.onExpandedChange(this, value)
        if (animate) {
            expandTransition.duration = duration ?: expandDuration
            TransitionManager.beginDelayedTransition(getSceneRoot(), expandTransition)
        }
        when (value) {
            true -> expandGroup.visibility = View.VISIBLE
            false -> expandGroup.visibility = View.GONE
        }
    }

    

    private fun getSceneRoot(): ViewGroup {
        return sceneRootIdRes?.let { rootView.findViewById(it) as? ViewGroup } ?: this
    }

    private fun initNumber(typedArray: TypedArray) {
        val numberColor =
            typedArray.getColorStateListOrThrow(R.styleable.GetStartedStepView_stepNumberColor)
        number.setTextColor(numberColor)
        val numberValue = typedArray.getInt(R.styleable.GetStartedStepView_stepNumber, 0)
        setNumber(numberValue)
    }

    private fun initPreviews(typedArray: TypedArray) {
        
        
        preview.id = View.generateViewId()
        previewForeground.id = View.generateViewId()
        val previewRes = typedArray.getResourceId(R.styleable.GetStartedStepView_stepPreview, 0)
        setPreviewAnimation(previewRes)
        val previewForegroundRes =
            typedArray.getResourceId(R.styleable.GetStartedStepView_stepPreviewForeground, 0)
        setPreviewForegroundAnimation(previewForegroundRes)
    }

    private fun initBackground(typedArray: TypedArray, attrs: AttributeSet?) {
        background =
            MaterialShapeDrawable(
                context,
                attrs,
                R.attr.getStartedStepViewStyle,
                R.style.Widget_Dashlane_GetStartedStepView
            )
        val backgroundTint =
            typedArray.getColorStateListOrThrow(R.styleable.GetStartedStepView_stepBackgroundTint)
        backgroundTintList = backgroundTint
    }

    private fun initExpand() {
        isClickable = true
        isFocusable = true
        expanded = false
        updateExpandState(expanded)
        setOnClickListener { if (!expanded) setExpanded(value = true, animate = true) }
    }

    private fun buildCompletionTransition(
        startListener: (() -> Unit)? = null,
        endListener: (() -> Unit)? = null
    ): Transition {
        return AutoTransition().apply {
            addTransition(TextColorTransition())
            ordering = TransitionSet.ORDERING_TOGETHER
            duration = completionDuration
            addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition) {
                    endListener?.invoke()
                }

                override fun onTransitionResume(transition: Transition) {
                    
                }

                override fun onTransitionPause(transition: Transition) {
                    
                }

                override fun onTransitionCancel(transition: Transition) {
                    
                }

                override fun onTransitionStart(transition: Transition) {
                    startListener?.invoke()
                }
            })
        }
    }

    @Parcelize
    private data class SaveState(
        val superState: Parcelable?,
        val expanded: Boolean,
        val completion: Boolean,
        @IdRes val sceneRootIdRes: Int?
    ) : Parcelable
}