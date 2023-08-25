package com.dashlane.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.dashlane.design.component.compat.view.ToggleView
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordgenerator.R
import com.google.android.material.slider.Slider
import kotlin.math.roundToInt

class PasswordGeneratorConfigurationView(context: Context, attrSet: AttributeSet?) : FrameLayout(context, attrSet) {
    private val slider: Slider
    private val digit: ToggleView
    private val letters: ToggleView
    private val symbols: ToggleView
    private val ambiguous: ToggleView

    var listener: ConfigurationChangeListener? = null

    private val sliderChangeListener = Slider.OnChangeListener { slider, value, fromUser ->
        if (value > 0) {
            slider.contentDescription = context.getString(
                R.string.and_accessibility_generator_password_length,
                value.toString()
            )
        } else {
            slider.contentDescription = null
        }
        listener?.onLengthUpdated(
            getConfiguration(value.roundToInt()),
            fromUser = fromUser
        )
    }

    init {
        val view = inflate(context, R.layout.generator_options_layout, this)
        slider = view.findViewById<Slider>(R.id.password_generator_option_length_seek).apply {
            value = DEFAULT_LENGTH.toFloat()
            addOnChangeListener(sliderChangeListener)
        }
        setPasswordLengthMaxMin(
            resources.getInteger(R.integer.password_generator_min_length_generated_password),
            resources.getInteger(R.integer.password_generator_max_length_generated_password)
        )

        digit = initDigits(view)
        letters = initLetter(view)
        symbols = initSymbols(view)
        ambiguous = initAmbiguous(view)

        updateSwitches()
    }

    private fun setPasswordLengthMaxMin(min: Int, max: Int) {
        slider.valueFrom = min.toFloat()
        slider.valueTo = max.toFloat()
        findViewById<TextView>(R.id.password_generator_option_length_min_title).text = min.toString()
        findViewById<TextView>(R.id.password_generator_option_length_max_title).text = max.toString()
    }

    private fun initAmbiguous(view: View): ToggleView {
        val layout = view.findViewById<RelativeLayout>(R.id.password_generator_option_ambiguous)
        return layout.findViewById<ToggleView>(R.id.option_switch).apply {
            text = context.getString(R.string.password_generator_ambiguous)
            onCheckedChange = { _ ->
                updateSwitches()
                listener?.onAmbiguousSwitched(getConfiguration())
            }
        }
    }

    private fun initSymbols(view: View): ToggleView {
        val layout = view.findViewById<RelativeLayout>(R.id.password_generator_option_symbols)
        return layout.findViewById<ToggleView>(R.id.option_switch).apply {
            text = context.getString(R.string.password_generator_symbols)
            onCheckedChange = { _ ->
                updateSwitches()
                listener?.onSymbolSwitched(getConfiguration())
            }
        }
    }

    private fun initLetter(view: View): ToggleView {
        val layout = view.findViewById<RelativeLayout>(R.id.password_generator_option_letters)
        return layout.findViewById<ToggleView>(R.id.option_switch).apply {
            text = context.getString(R.string.password_generator_letters)
            onCheckedChange = { _ ->
                updateSwitches()
                listener?.onLetterSwitched(getConfiguration())
            }
        }
    }

    private fun initDigits(view: View): ToggleView {
        val layout = view.findViewById<RelativeLayout>(R.id.password_generator_option_digits)

        return layout.findViewById<ToggleView>(R.id.option_switch).apply {
            text = context.getString(R.string.password_generator_digits)
            onCheckedChange = { _ ->
                updateSwitches()
                listener?.onDigitSwitched(getConfiguration())
            }
        }
    }

    private fun updateSwitches() {
        enableAllEssentialSwitches()
        preventDisablingEssentialOption()
    }

    private fun enableAllEssentialSwitches() {
        digit.readOnly = false
        letters.readOnly = false
        symbols.readOnly = false
    }

    private fun preventDisablingEssentialOption() {
        when {
            
            digit.checked && !letters.checked && !symbols.checked ->
                digit.readOnly = true
            
            !digit.checked && letters.checked && !symbols.checked ->
                letters.readOnly = true
            
            digit.checked && !letters.checked && symbols.checked ->
                digit.readOnly = true
            !digit.checked && letters.checked && symbols.checked ->
                letters.readOnly = true
            
            !digit.checked && !letters.checked && !symbols.checked -> {
                enableAllEssentialSwitches()
                digit.checked = true
                digit.readOnly = true
            }
        }
    }

    fun getConfiguration(updatedLength: Int? = null) =
        PasswordGeneratorCriteria(
            length = updatedLength ?: slider.value.roundToInt(),
            digits = digit.checked,
            letters = letters.checked,
            symbols = symbols.checked,
            ambiguousChars = ambiguous.checked
        )

    fun setNewConfiguration(minLength: Int, maxLength: Int, defaultCriteria: PasswordGeneratorCriteria) {
        
        digit.checked = true
        letters.checked = true
        symbols.checked = true
        ambiguous.checked = true

        setPasswordLengthMaxMin(minLength, maxLength)
        slider.value = defaultCriteria.length.coerceIn(minLength, maxLength).toFloat()
        digit.checked = defaultCriteria.digits
        letters.checked = defaultCriteria.letters
        symbols.checked = defaultCriteria.symbols
        ambiguous.checked = defaultCriteria.ambiguousChars
        
        
        updateSwitches()
    }

    interface ConfigurationChangeListener {
        fun onLengthUpdated(criteria: PasswordGeneratorCriteria, fromUser: Boolean)

        fun onDigitSwitched(criteria: PasswordGeneratorCriteria)

        fun onLetterSwitched(criteria: PasswordGeneratorCriteria)

        fun onSymbolSwitched(criteria: PasswordGeneratorCriteria)

        fun onAmbiguousSwitched(criteria: PasswordGeneratorCriteria)
    }

    companion object {
        const val DEFAULT_LENGTH = 12
    }
}