package com.dashlane.autofill.api.common

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.dashlane.autofill.api.R
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.ui.PasswordGeneratorConfigurationView
import com.dashlane.ui.widgets.view.tintProgressDrawable
import com.dashlane.util.animation.fadeIn
import com.dashlane.util.animation.fadeOut
import com.dashlane.util.colorpassword.ColorTextWatcher
import com.dashlane.util.setProgressDrawablePrimaryTrack
import com.google.android.material.textfield.TextInputEditText
import com.skocken.presentation.viewproxy.BaseViewProxy



abstract class GeneratePasswordViewProxy<T : GeneratePasswordContract.Presenter>(
    dialogView: View,
    defaultCriteria: PasswordGeneratorCriteria,
    private val logger: AutofillGeneratePasswordLogger,
) : GeneratePasswordContract.ViewProxy, BaseViewProxy<T>(dialogView),
    PasswordGeneratorConfigurationView.ConfigurationChangeListener {
    private var password: TextInputEditText? = null
    private var generateButton: Button? = null
    var generatorConfiguration: PasswordGeneratorConfigurationView? = null
    private var showOptionButton: Button? = null
    private var collapseArrow: ImageView? = null
    private var optionsSeparator: View? = null
    private var strengthBar: ProgressBar? = null
    private var strengthBarSpecialMode: ProgressBar? = null
    private var strengthTitle: TextView? = null

    private val passwordTextWatcher = object : ColorTextWatcher(context) {
        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            try {
                super.onTextChanged(charSequence, start, before, count)
            } catch (e: IndexOutOfBoundsException) {
                
                
            }
            presenter.onPasswordUpdated(charSequence.toString())
        }
    }

    init {
        password = dialogView.findViewById(R.id.password)
        generateButton = dialogView.findViewById(R.id.generate_button)
        generatorConfiguration = dialogView.findViewById(R.id.generator_configuration)
        showOptionButton = dialogView.findViewById(R.id.show_option_button)
        collapseArrow = dialogView.findViewById(R.id.collapse_arrow)
        optionsSeparator = dialogView.findViewById(R.id.options_separator)
        strengthBar = dialogView.findViewById(R.id.strength_bar)
        strengthBarSpecialMode = dialogView.findViewById(R.id.strength_bar_special_mode)
        strengthTitle = dialogView.findViewById(R.id.strength_title)

        password?.addTextChangedListener(passwordTextWatcher)
        generateButton?.setOnClickListener {
            generatorConfiguration?.let { presenter.onGenerateButtonClicked(it.getConfiguration()) }
        }

        initGeneratorConfiguration(defaultCriteria)

        showOptionButton?.setOnClickListener { toggleGeneratorConfiguration() }
        collapseArrow?.setOnClickListener { toggleGeneratorConfiguration() }
    }

    override fun setPasswordField(newPassword: String?) {
        revealPassword()
        password?.setText(newPassword)
    }

    override fun setPasswordStrength(
        title: String?,
        color: Int,
        strength: Int,
        safeEnoughForSpecialMode: Boolean,
    ) {
        if (title == null) {
            strengthBar?.fadeOut()
            strengthTitle?.fadeOut()
            strengthTitle?.text = null
        } else {
            strengthTitle?.text = title
            strengthTitle?.setTextColor(color)
            strengthTitle?.fadeIn()

            val progress = if (strength == 0) 2 else strength
            strengthBar?.apply {
                this.progress = progress
                tintProgressDrawable(color)
                fadeIn()
            }
            val alpha = 1f.takeIf { safeEnoughForSpecialMode } ?: 0f
            val duration = 400L.takeIf { safeEnoughForSpecialMode } ?: 250L
            val startDelay = 300L.takeIf { safeEnoughForSpecialMode } ?: 0L
            strengthBarSpecialMode?.animate()
                ?.alpha(alpha)
                ?.setDuration(duration)
                ?.setStartDelay(startDelay)
                ?.start()
        }
    }

    override fun onLengthUpdated(criteria: PasswordGeneratorCriteria, fromUser: Boolean) {
        revealPassword()
        logger.logChangeLength(criteria.length)
        presenter.onGeneratorConfigurationChanged(criteria)
    }

    override fun onDigitSwitched(criteria: PasswordGeneratorCriteria) {
        logger.logChangeDigit(criteria.digits)
        presenter.onGeneratorConfigurationChanged(criteria)
    }

    override fun onLetterSwitched(criteria: PasswordGeneratorCriteria) {
        logger.logChangeLetters(criteria.letters)
        presenter.onGeneratorConfigurationChanged(criteria)
    }

    override fun onSymbolSwitched(criteria: PasswordGeneratorCriteria) {
        logger.logChangeSymbols(criteria.symbols)
        presenter.onGeneratorConfigurationChanged(criteria)
    }

    override fun onAmbiguousSwitched(criteria: PasswordGeneratorCriteria) {
        logger.logChangeAmbiguousChar(criteria.ambiguousChars)
        presenter.onGeneratorConfigurationChanged(criteria)
    }

    override fun initSpecialMode(eligible: Boolean) {
        if (!eligible) {
            strengthBarSpecialMode?.isVisible = false
        } else {
            val success =
                strengthBarSpecialMode?.setProgressDrawablePrimaryTrack(R.drawable.password_strength_pride_flag_bar)
            strengthBarSpecialMode?.isVisible = success ?: false
        }
    }

    

    private fun initGeneratorConfiguration(defaultCriteria: PasswordGeneratorCriteria) {
        val min = resources.getInteger(R.integer.password_generator_min_length_generated_password)
        val max = resources.getInteger(R.integer.password_generator_max_length_generated_password)
        generatorConfiguration?.setNewConfiguration(min, max, defaultCriteria)
        generatorConfiguration?.listener = this
    }

    

    private fun revealPassword() {
        password?.transformationMethod = null
    }

    

    private fun toggleGeneratorConfiguration() {
        if (generatorConfiguration?.visibility == View.VISIBLE) {
            generatorConfiguration?.fadeOut()
            optionsSeparator?.fadeOut()
            showOptionButton?.setText(R.string.autofill_generate_password_generator_options_show)
            collapseArrow?.animate()?.rotation(0f)
        } else {
            generatorConfiguration?.fadeIn()
            optionsSeparator?.fadeIn()
            showOptionButton?.setText(R.string.autofill_generate_password_generator_options_hide)
            collapseArrow?.animate()?.rotation(-180f)
        }
    }
}