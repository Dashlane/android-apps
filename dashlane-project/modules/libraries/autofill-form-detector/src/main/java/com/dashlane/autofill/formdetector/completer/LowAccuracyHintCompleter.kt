package com.dashlane.autofill.formdetector.completer

import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

class LowAccuracyHintCompleter : HintCompleter {
    override fun completeEntries(entries: MutableList<AutoFillHintSummary.Entry>) {
        val hasEmail = entries.any {
            it.hasAccurateHint(AutoFillHint.EMAIL_ADDRESS) || it.hasAccurateHint(AutoFillHint.USERNAME)
        }
        val hasPassword = entries.any { it.hasAccurateHint(AutoFillHint.PASSWORD) }
        if (hasEmail && !hasPassword) {
            entries
                .filter { it.hasHint(AutoFillHint.PASSWORD) }
                .filter { it.autoFillHints.getHints().isEmpty() }
                .maxByOrNull { it.autoFillHints.getHigherPriority() }
                ?.autoFillHints?.add(AutoFillHint.PASSWORD, 20)
        } else if (hasPassword && !hasEmail) {
            entries
                .filter { it.hasHint(AutoFillHint.USERNAME) }
                .filter { it.autoFillHints.getHints().isEmpty() }
                .maxByOrNull { it.autoFillHints.getHigherPriority() }
                ?.autoFillHints?.add(AutoFillHint.USERNAME, 30)
        }
    }
}