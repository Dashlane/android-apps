package com.dashlane.autofill.formdetector.completer

import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

class CandidateHintCompleter : HintCompleter {
    override fun completeEntries(entries: MutableList<AutoFillHintSummary.Entry>) {
        entries
            .filter { it.autoFillHints.isCandidate() }
            .filter { it.autoFillHints.getHints().isEmpty() }
            .maxByOrNull { it.autoFillHints.getHigherPriority() }
            ?.let {
                it.autoFillHints.add(
                    AutoFillHint.USERNAME,
                    it.autoFillHints.getHigherPriority(),
                )
            }
        entries
            .filter { it.autoFillHints.isPasswordCandidate() }
            .filter { it.autoFillHints.getHints().isEmpty() }
            .maxByOrNull { it.autoFillHints.getHigherPriority() }
            ?.let {
                it.autoFillHints.add(
                    AutoFillHint.PASSWORD,
                    it.autoFillHints.getHigherPriority(),
                )
            }
    }
}