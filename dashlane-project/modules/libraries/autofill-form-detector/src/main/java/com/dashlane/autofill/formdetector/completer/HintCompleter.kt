package com.dashlane.autofill.formdetector.completer

import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

fun interface HintCompleter {
    fun completeEntries(entries: MutableList<AutoFillHintSummary.Entry>)
}