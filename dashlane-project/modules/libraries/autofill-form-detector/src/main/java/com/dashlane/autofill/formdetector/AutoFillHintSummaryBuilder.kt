package com.dashlane.autofill.formdetector

import com.dashlane.autofill.formdetector.completer.HintCompleter
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.url.toUrlDomainOrNull

class AutoFillHintSummaryBuilder(
    val packageName: String,
    val manualRequest: Boolean,
) {
    var isRequestTimedOut: Boolean = true
    var webDomain: String? = null
    val focusEntry: AutoFillHintSummary.Entry?
        get() {
            return entries.firstOrNull { it.isFocused }
        }

    
    val entries = mutableListOf<AutoFillHintSummary.Entry>()

    fun build(hintCompleters: List<HintCompleter>): AutoFillHintSummary? {
        val formSource = getFormSource(this) ?: return null

        
        if (manualRequest) {
            entries.forEach { it.autoFillHints.addAccuracyToAll(50) }
        }

        
        hintCompleters.forEach { it.completeEntries(entries) }

        
        cleanNotUsedEntries()

        return AutoFillHintSummary(
            manualRequest = manualRequest,
            formSource = formSource,
            formType = getFormType(),
            focusIndex = entries.toList().indexOfFirst { it.isFocused },
            entries = entries.toList(),
            isRequestTimedOut = isRequestTimedOut,
        )
    }

    fun put(entry: AutoFillHintSummary.Entry) {
        entries.add(entry)
    }

    fun addWebsiteIfValid(domain: String?) {
        getUrlDomain(domain)?.let {
            webDomain = it
        }
    }

    private fun cleanNotUsedEntries() {
        entries.removeIf {
            it.autoFillHints.getHints().isEmpty()
        }
    }

    private fun getFormSource(summaryBuilder: AutoFillHintSummaryBuilder): AutoFillFormSource? {
        val packageName = summaryBuilder.packageName
        if (packageName.isEmpty()) {
            return null
        }
        val webDomain = summaryBuilder.webDomain

        return when {
            webDomain.isNullOrEmpty() -> ApplicationFormSource(packageName)
            else -> WebDomainFormSource(packageName, webDomain)
        }
    }

    @AutoFillFormType.FormType
    private fun getFormType(): Int {
        return AutoFillFormType.get(focusEntry, entries)
    }

    companion object {
        fun getUrlDomain(domain: String?): String? {
            if (domain.isNullOrBlank()) return null 
            if ("localhost" == domain || "127.0.0.1" == domain) return null 
            return domain.toUrlDomainOrNull()?.value
        }
    }
}