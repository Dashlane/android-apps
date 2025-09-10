package com.dashlane.autofill.formdetector.field

import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.scoring.AutofillHintsWithAccuracy

open class LabelAcceptor(private val keywords: List<String>) {
    fun labelsAccuracy(node: AutoFillViewNode): Int {
        return matchingAccuracy(
            Pair(node.contentDescription, 80),
            Pair(node.hint, 80),
            Pair(node.text, 60),
            Pair(getViewIdWithoutPackage(node.viewIdResourceName), 50),
            Pair(node.getHtmlAttribute("id"), 50),
            Pair(node.getHtmlAttribute("name"), 50),
            Pair(node.getHtmlAttribute("label"), 50),
        )
    }

    private fun matchingAccuracy(vararg labels: Pair<CharSequence?, Int>): Int {
        var resultAccuracy = 0
        labels.forEach { label ->
            findLabelAccuracy(label.first)?.let {
                resultAccuracy = AutofillHintsWithAccuracy.addPercent(
                    resultAccuracy,
                    (it * (label.second.toFloat() / 100)).toInt(),
                )
            }
        }
        return resultAccuracy
    }

    private fun findLabelAccuracy(label: CharSequence?): Int? {
        if (label == null || label.length > 55) {
            
            return null
        }

        
        keywords.find {
            label.toString().trim().equals(it, ignoreCase = true) ||
                label.toString().trim().equals(it.withoutSpace(), ignoreCase = true)
        }?.let {
            return 100
        }

        
        keywords.find {
            label.toString().trim().lowercase().contains(it) ||
                label.toString().trim().lowercase().contains(it.withoutSpace())
        }?.let {
            return 50
        }
        return null
    }

    private fun getViewIdWithoutPackage(viewIdResourceName: String?): String? {
        viewIdResourceName ?: return null
        val indexOf = viewIdResourceName.indexOf(SEPARATOR_PACKAGE_VIEW_ID)
        if (indexOf < 0) return viewIdResourceName
        return viewIdResourceName.substring(indexOf + SEPARATOR_PACKAGE_VIEW_ID.length)
    }

    private fun String.withoutSpace(): String {
        return this.replace(" ", "")
    }

    companion object {
        private const val SEPARATOR_PACKAGE_VIEW_ID = ":id/"
    }
}