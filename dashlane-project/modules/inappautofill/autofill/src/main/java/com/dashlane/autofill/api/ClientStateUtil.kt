package com.dashlane.autofill.api

import android.os.Bundle
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofillapi.AutoFillAPIService
import com.dashlane.util.getParcelableArrayListCompat

private const val PREVIOUS_ENTRIES_PARAM = "previousEntries"

internal fun Bundle.setClassLoaderInDashlane() {
    classLoader = AutoFillAPIService::class.java.classLoader
}

internal fun Bundle.addPreviousEntriesFrom(summary: AutoFillHintSummary) {
    putParcelableArrayList(PREVIOUS_ENTRIES_PARAM, ArrayList(summary.entries))
}

internal fun getPreviousEntriesFrom(bundle: Bundle?): List<AutoFillHintSummary.Entry> {
    return bundle?.getParcelableArrayListCompat(PREVIOUS_ENTRIES_PARAM) ?: listOf()
}
