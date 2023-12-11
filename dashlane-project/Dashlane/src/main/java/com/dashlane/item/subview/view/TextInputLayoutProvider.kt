package com.dashlane.item.subview.view

import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.dashlane.R
import com.dashlane.listeners.edittext.NoLockEditTextWatcher
import com.dashlane.login.lock.LockManager
import com.dashlane.util.colorpassword.ColorTextWatcher
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.getThemeAttrResourceId
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE

object TextInputLayoutProvider {

    fun create(
        context: Context,
        lockManager: LockManager,
        header: String,
        value: String?,
        editable: Boolean = false,
        protected: Boolean = false,
        allowReveal: Boolean = true,
        suggestions: List<String>? = null,
        multiline: Boolean = false,
        listener: View.OnClickListener? = null,
        coloredCharacter: Boolean = false
    ): TextInputLayout {
        val textInputLayout = createTextInputLayout(context, header)

        val editText: EditText = createEditText(multiline, context, suggestions)

        editText.run {
            if (coloredCharacter) {
                addTextChangedListener(ColorTextWatcher(context))
            }
            if (!multiline) {
                setSingleLine()
            }
            minimumHeight = context.resources.getDimensionPixelSize(R.dimen.minimum_clickable_area_size)
            if (!editable) {
                isFocusable = false
                isFocusableInTouchMode = false
                isLongClickable = false
                setBackgroundResource(0)
                listener?.let { setOnClickListener(it) }
            } else {
                imeOptions = EditorInfo.IME_ACTION_NEXT
                
                addTextChangedListener(NoLockEditTextWatcher(lockManager))
            }
        }

        textInputLayout.addView(editText)

        if (protected) {
            editText.typeface = ResourcesCompat.getFont(context, R.font.roboto_mono_regular)
            editText.fontFeatureSettings = "font-variant-ligatures:none" 
            editText.transformationMethod = PasswordTransformationMethod()
            editText.imeOptions = editText.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI
            editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_PASSWORD
            if (allowReveal) {
                textInputLayout.endIconMode = END_ICON_PASSWORD_TOGGLE
            } else {
                textInputLayout.endIconMode = END_ICON_NONE
            }
        }
        editText.setText(value)
        return textInputLayout
    }

    private fun createEditText(multiline: Boolean, context: Context, suggestions: List<String>?): EditText =
        when {
            multiline -> LayoutInflater.from(context).inflate(R.layout.edittext_input_provider_item, null) as EditText
            suggestions == null -> AppCompatEditText(context)
            else -> AppCompatAutoCompleteTextView(context).apply {
                val adapter = ArrayAdapter<String>(
                    context,
                    R.layout.autocomplete_textview_adapter,
                    suggestions
                )
                setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceSubtitle1))
                setTextColor(context.getThemeAttrColor(R.attr.colorOnBackground))
                setAdapter(adapter)
                threshold = 1
            }
        }

    private fun createTextInputLayout(context: Context, header: String) = TextInputLayout(context).apply {
        setHintTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceBody2))
        defaultHintTextColor =
            ColorStateList.valueOf(context.getThemeAttrColor(R.attr.colorOnBackgroundMedium))
        hint = header
        contentDescription = header
    }
}