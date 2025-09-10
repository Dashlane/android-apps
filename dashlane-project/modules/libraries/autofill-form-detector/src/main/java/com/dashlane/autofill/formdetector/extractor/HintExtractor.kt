package com.dashlane.autofill.formdetector.extractor

import com.dashlane.autofill.api.model.AutoFillApiViewNode
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.field.enums.ChromeAutofillFieldType
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy
import java.util.Collections

class HintExtractor(private val autoFillViewNode: AutoFillViewNode) {
    fun getHint(hintsWithAccuracy: AutofillHintsWithAccuracy): Boolean {
        var hasFoundHint = false
        (getAutoFillHints() + getHintsFromWebViewTags()).distinct().forEach {
            hasFoundHint = true
            hintsWithAccuracy.add(it, 90)
        }
        return hasFoundHint
    }

    private fun getAutoFillHints(): List<String> {
        if (autoFillViewNode is AutoFillApiViewNode) {
            return autoFillViewNode.viewNode.autofillHints
                ?.filterNotNull()
                ?.filter { it.isNotEmpty() && !IGNORED_AUTOFILL_HINTS.contains(it.lowercase()) }
                ?.map { it.split("|") } 
                ?.flatten()
                ?.takeIf { it.isNotEmpty() }
                ?.replaceSynonymHints()
                ?.mapNotNull { convertToExistingHint(it) } ?: listOf()
        }
        return listOf()
    }

    private fun getHintsFromWebViewTags(): List<String> {
        if (autoFillViewNode.htmlTag != "input") {
            return listOf() 
        }

        val webViewHints = arrayListOf<String?>()
        webViewHints.add(autoFillViewNode.getHtmlAttribute("crowdsourcing-autofill-hints"))
        webViewHints.add(autoFillViewNode.getHtmlAttribute("computed-autofill-hints"))
        autoFillViewNode.getHtmlAttribute("crowdsourcing-predictions-autofill-hints")
            ?.split(",")
            ?.forEach { webViewHints.add(it) }
        autoFillViewNode.getHtmlAttribute("ua-autofill-hints")?.let { hint ->
            webViewHints.add(
                ChromeAutofillFieldType.values().firstOrNull { it.value == hint }?.androidHint,
            )
        }
        return webViewHints.filterNotNull()
            .mapNotNull { convertToExistingHint(it) }
    }

    private fun convertToExistingHint(value: String): String? {
        return AutoFillHint.all.firstOrNull {
            it == value
        } ?: AutoFillHint.all.firstOrNull {
            it == value.lowercase().camelToSnakeCase()
        } ?: AutoFillHint.all.firstOrNull {
            it == value.lowercase().snakeToLowerCamelCase()
        }
    }

    private fun List<String>.replaceSynonymHints(): List<String> {
        Collections.replaceAll(this, "email", AutoFillHint.EMAIL_ADDRESS)
        Collections.replaceAll(this, "cc-number", AutoFillHint.CREDIT_CARD_NUMBER)
        return this
    }

    private fun String.camelToSnakeCase(): String {
        return CAMEL_REGEX.replace(this) {
            "_${it.value}"
        }.lowercase()
    }

    private fun String.snakeToLowerCamelCase(): String {
        return SNAKE_REGEX.replace(this) {
            it.value.replace("_", "").uppercase()
        }
    }

    companion object {
        private val IGNORED_AUTOFILL_HINTS = listOf("on", "off")
        val CAMEL_REGEX = "(?<=[a-zA-Z])[A-Z]".toRegex()
        val SNAKE_REGEX = "_[a-zA-Z]".toRegex()
    }
}