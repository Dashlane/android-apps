package com.dashlane.autofill.util

import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

fun AutoFillHintSummary.getBestEntry(predicate: (AutoFillHintSummary.Entry) -> Boolean): AutoFillHintSummary.Entry? {
    
    focusEntry
        ?.takeIf { predicate.invoke(it) }
        ?.let { return it }

    
    return entries.firstOrNull(predicate)
}

fun AutoFillHintSummary.copyPlusEntries(entriesToAdd: List<AutoFillHintSummary.Entry>): AutoFillHintSummary {
    return AutoFillHintSummary(
        manualRequest,
        formSource,
        formType,
        focusIndex,
        entries + entriesToAdd
    )
}