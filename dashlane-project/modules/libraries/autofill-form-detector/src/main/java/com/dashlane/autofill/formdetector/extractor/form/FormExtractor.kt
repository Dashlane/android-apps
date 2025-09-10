package com.dashlane.autofill.formdetector.extractor.form

import android.text.InputType
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy

interface FormExtractor {
    fun extractor(
        view: AutoFillViewNode,
        hintsWithAccuracy: AutofillHintsWithAccuracy,
        isEditText: Boolean,
    ) {
        view.takeIf { it.isVisibleToUser } ?: return
    }

    fun isInputTypePassword(node: AutoFillViewNode): Boolean {
        val inputTypeVariation = getInputTypeVariation(node)
        return when (getInputTypeClass(node)) {
            InputType.TYPE_CLASS_TEXT -> (
                inputTypeVariation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    inputTypeVariation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            )

            InputType.TYPE_CLASS_NUMBER -> inputTypeVariation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
    }

    fun isInputTypeLogin(node: AutoFillViewNode): Boolean {
        val inputTypeVariation = getInputTypeVariation(node)
        return (getInputTypeClass(node) == InputType.TYPE_CLASS_TEXT) &&
            (
                inputTypeVariation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                    inputTypeVariation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ||
                    inputTypeVariation == InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            )
    }

    fun getInputTypeClass(node: AutoFillViewNode): Int {
        return node.inputType and InputType.TYPE_MASK_CLASS
    }

    fun getInputTypeVariation(node: AutoFillViewNode): Int {
        return node.inputType and InputType.TYPE_MASK_VARIATION
    }

    companion object {
        fun isEditText(root: AutoFillViewNode): Boolean {
            if (root.inputType != InputType.TYPE_NULL) {
                return true 
            }

            
            val isNativeEditText =
                root.className != null && (
                    root.className!!.endsWith("EditText", true) ||
                        root.className!!.endsWith("AutoCompleteTextView", true) ||
                        root.className!!.endsWith("MultiAutoCompleteTextView", true)
                )
            val isHtmlEditText = root.getHtmlAttribute("type") == "text" ||
                root.getHtmlAttribute("type") == "tel"
            return isNativeEditText || isHtmlEditText
        }

        fun isTextView(root: AutoFillViewNode): Boolean {
            return root.className != null && (
                root.className!!.endsWith("TextView", true) ||
                    (root.className!!.endsWith("view.View", true))
            )
        }
    }
}