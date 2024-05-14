package com.dashlane.autofill.generatepassword

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.databinding.IncludeGeneratePasswordBinding
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.passwordstrength.borderColorRes
import com.dashlane.passwordstrength.getShortTitle
import com.dashlane.passwordstrength.isSafeEnoughForSpecialMode
import com.dashlane.ui.PasswordGeneratorConfigurationView
import com.dashlane.ui.widgets.view.tintProgressDrawable
import com.dashlane.util.animation.fadeIn
import com.dashlane.util.animation.fadeOut
import com.dashlane.util.colorpassword.ColorTextWatcher
import com.dashlane.util.setProgressDrawablePrimaryTrack
import com.dashlane.utils.PasswordScrambler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GeneratePasswordViewProxy(
    private val binding: IncludeGeneratePasswordBinding,
    defaultCriteria: PasswordGeneratorCriteria,
    private val viewModel: GeneratePasswordViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
) : GeneratePasswordContract.ViewProxy,
    PasswordGeneratorConfigurationView.ConfigurationChangeListener {
    private val context: Context
        get() = binding.root.context

    private val passwordScrambler = PasswordScrambler()
    private var passwordScrambleJob: Job? = null
    private val passwordTextWatcher = object : ColorTextWatcher(context) {
        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            try {
                super.onTextChanged(charSequence, start, before, count)
            } catch (e: IndexOutOfBoundsException) {
                
                
            }
            viewModel.evaluatePassword(charSequence.toString())
        }
    }

    init {
        binding.password.addTextChangedListener(passwordTextWatcher)
        binding.passwordLayout.setEndIconOnClickListener {
            binding.generatorConfiguration.let {
                viewModel.onGenerateButtonClicked(it.getConfiguration())
            }
        }

        initGeneratorConfiguration(defaultCriteria)

        binding.showOptionButton.setOnClickListener { toggleGeneratorConfiguration() }
        binding.collapseArrow.setOnClickListener { toggleGeneratorConfiguration() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is GeneratePasswordState.Initial -> {
                            updateSpecialMode(state.data.specialMode)
                        }
                        is GeneratePasswordState.PasswordGenerated -> {
                            setPasswordField(state.password)
                        }
                        is GeneratePasswordState.PasswordSavedToHistory -> Unit
                    }
                    updatePasswordStrengthAndColors(state.data.strengthScore)
                }
            }
        }
    }

    private fun updatePasswordStrengthAndColors(strengthScore: PasswordStrengthScore?) {
        val title = strengthScore?.getShortTitle(context)
        val borderColor = context.getColor(strengthScore?.borderColorRes ?: R.color.border_brand_standard_idle)
        val strength = strengthScore?.percentValue ?: 0
        val safeEnoughForSpecialMode = strengthScore?.isSafeEnoughForSpecialMode ?: false
        setPasswordStrength(
            title = title,
            color = borderColor,
            strength = strength,
            safeEnoughForSpecialMode = safeEnoughForSpecialMode
        )
    }

    override fun setPasswordField(newPassword: String) {
        revealPassword()
        passwordScrambleJob?.cancel()
        passwordScrambleJob = viewLifecycleOwner.lifecycleScope.launch {
            binding.password.removeTextChangedListener(passwordTextWatcher)
            passwordScrambler.runScramble(newPassword) {
                binding.password.setText(it)
            }
            viewModel.evaluatePassword(newPassword)
            binding.password.addTextChangedListener(passwordTextWatcher)
        }
    }

    override fun setPasswordStrength(
        title: String?,
        color: Int,
        strength: Int,
        safeEnoughForSpecialMode: Boolean,
    ) {
        if (title == null) {
            binding.strengthBar.fadeOut()
            binding.strengthTitle.fadeOut()
            binding.strengthTitle.text = null
        } else {
            binding.strengthTitle.text = title
            binding.strengthTitle.setTextColor(color)
            binding.strengthTitle.fadeIn()

            val progress = if (strength == 0) 2 else strength
            binding.strengthBar.apply {
                this.progress = progress
                tintProgressDrawable(color)
                fadeIn()
            }
            val alpha = 1f.takeIf { safeEnoughForSpecialMode } ?: 0f
            val duration = 400L.takeIf { safeEnoughForSpecialMode } ?: 250L
            val startDelay = 300L.takeIf { safeEnoughForSpecialMode } ?: 0L
            binding.strengthBarSpecialMode.animate()
                ?.alpha(alpha)
                ?.setDuration(duration)
                ?.setStartDelay(startDelay)
                ?.start()
        }
    }

    override fun onLengthUpdated(criteria: PasswordGeneratorCriteria, fromUser: Boolean) {
        revealPassword()
        viewModel.onGeneratorConfigurationChanged(criteria)
    }

    override fun onDigitSwitched(criteria: PasswordGeneratorCriteria) {
        viewModel.onGeneratorConfigurationChanged(criteria)
    }

    override fun onLetterSwitched(criteria: PasswordGeneratorCriteria) {
        viewModel.onGeneratorConfigurationChanged(criteria)
    }

    override fun onSymbolSwitched(criteria: PasswordGeneratorCriteria) {
        viewModel.onGeneratorConfigurationChanged(criteria)
    }

    override fun onAmbiguousSwitched(criteria: PasswordGeneratorCriteria) {
        viewModel.onGeneratorConfigurationChanged(criteria)
    }

    override fun updateSpecialMode(specialMode: GeneratePasswordSpecialMode?) {
        if (specialMode == null) {
            binding.strengthBarSpecialMode.isVisible = false
        } else {
            val success = when (specialMode) {
                GeneratePasswordSpecialMode.PRIDE ->
                    binding.strengthBarSpecialMode.setProgressDrawablePrimaryTrack(R.drawable.password_strength_pride_flag_bar)
            }
            binding.strengthBarSpecialMode.isVisible = success
        }
    }

    private fun initGeneratorConfiguration(defaultCriteria: PasswordGeneratorCriteria) {
        val min = context.resources.getInteger(R.integer.password_generator_min_length_generated_password)
        val max = context.resources.getInteger(R.integer.password_generator_max_length_generated_password)
        binding.generatorConfiguration.setNewConfiguration(min, max, defaultCriteria)
        binding.generatorConfiguration.listener = this
    }

    private fun revealPassword() {
        binding.password.transformationMethod = null
    }

    private fun toggleGeneratorConfiguration() {
        if (binding.generatorConfiguration.visibility == View.VISIBLE) {
            binding.generatorConfiguration.fadeOut()
            binding.optionsSeparator.root.fadeOut()
            binding.showOptionButton.setText(R.string.autofill_generate_password_generator_options_show)
            binding.collapseArrow.animate()?.rotation(0f)
        } else {
            binding.generatorConfiguration.fadeIn()
            binding.optionsSeparator.root.fadeIn()
            binding.showOptionButton.setText(R.string.autofill_generate_password_generator_options_hide)
            binding.collapseArrow.animate()?.rotation(-180f)
        }
    }
}