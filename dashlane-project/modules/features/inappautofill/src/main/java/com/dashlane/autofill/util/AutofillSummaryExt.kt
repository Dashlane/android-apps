package com.dashlane.autofill.util

import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

fun AutoFillHintSummary.getClosestEntryOrNull(
    referenceEntry: AutoFillHintSummary.Entry?,
    predicate: (AutoFillHintSummary.Entry) -> Boolean,
): AutoFillHintSummary.Entry? {
    
    if (referenceEntry == null) {
        return entries.firstOrNull(predicate)
    }

    
    val referenceEntryAncestry = referenceEntry.parentViewIds + referenceEntry.viewId
    val filteredEntries = entries.filter(predicate)

    return filteredEntries.minByOrNull { entry ->
        val candidateAncestry = entry.parentViewIds + entry.viewId
        calculateEntriesDistance(
            referenceTree = referenceEntryAncestry,
            candidateTree = candidateAncestry,
        ) ?: Int.MAX_VALUE
    }
}

fun AutoFillHintSummary.getBestEntry(predicate: (AutoFillHintSummary.Entry) -> Boolean): AutoFillHintSummary.Entry? {
    
    focusEntry
        ?.takeIf { predicate.invoke(it) }
        ?.let { return it }

    
    return entries.firstOrNull(predicate)
}

fun AutoFillHintSummary.copyPlusEntries(entriesToAdd: List<AutoFillHintSummary.Entry>): AutoFillHintSummary {
    return AutoFillHintSummary(
        manualRequest = manualRequest,
        formSource = formSource,
        formType = formType,
        focusIndex = focusIndex,
        entries = entries + entriesToAdd,
        isRequestTimedOut = isRequestTimedOut,
    )
}