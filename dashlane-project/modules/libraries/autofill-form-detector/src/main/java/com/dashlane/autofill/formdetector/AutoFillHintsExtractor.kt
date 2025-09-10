package com.dashlane.autofill.formdetector

import android.app.assist.AssistStructure
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillId
import com.dashlane.autofill.api.model.AutoFillApiViewNode
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.formdetector.completer.CandidateHintCompleter
import com.dashlane.autofill.formdetector.completer.LowAccuracyHintCompleter
import com.dashlane.autofill.formdetector.extractor.Extractor
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.autofill.formdetector.model.children
import com.dashlane.autofill.util.IdGenerator
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.coroutineContext

class AutoFillHintsExtractor(
    private val autofillValueFactory: AutofillValueFactory,
    private val packageNameAcceptor: AutofillPackageNameAcceptor,
    private val idGenerator: IdGenerator,
) {
    suspend fun extract(request: FillRequest): AutoFillHintSummary? {
        val manualRequest = (request.flags or FillRequest.FLAG_MANUAL_REQUEST) == request.flags
        return extract(request.fillContexts, manualRequest)
    }

    suspend fun extractOnlyRequested(request: FillRequest): AutoFillHintSummary? {
        val manualRequest = (request.flags or FillRequest.FLAG_MANUAL_REQUEST) == request.flags
        val requestedFillContext = request.getRequestedFillContext()

        return extract(
            listOf(requestedFillContext),
            manualRequest,
        )
    }

    private fun FillRequest.getRequestedFillContext(): FillContext {
        return this.fillContexts.filter {
            it.requestId == this.id
        }.takeUnless {
            it.isEmpty()
        }?.last() ?: getLastFillContext()
    }

    private fun FillRequest.getLastFillContext(): FillContext {
        return this.fillContexts.last()
    }

    suspend fun extract(request: SaveRequest): AutoFillHintSummary? {
        return extract(request.fillContexts, false)
    }

    private suspend fun extract(
        fillContexts: List<FillContext>,
        manualRequest: Boolean,
    ): AutoFillHintSummary? {
        val lastStructure = fillContexts.lastOrNull()?.structure ?: return null
        val packageName = lastStructure.activityComponent.packageName
        val completers = listOf(CandidateHintCompleter(), LowAccuracyHintCompleter())

        if (!packageNameAcceptor.canFillRequestForApp(packageName)) {
            
            return null
        }

        val summary = AutoFillHintSummaryBuilder(packageName, manualRequest)

        
        withTimeout(DETECTION_TIMEOUT) {
            fillSummary(fillContexts, summary, false)

            
            if (summary.build(completers)?.takeUnless { it.isEmpty() } == null) {
                fillSummary(fillContexts, summary, true)
            }

            
            summary.isRequestTimedOut = false
        }
        if (summary.focusEntry == null) {
            
            fillContexts.lastOrNull()?.focusedId
                ?.let { focusedAutofillId: AutofillId ->
                    summary.entries.firstOrNull { entry: AutoFillHintSummary.Entry ->
                        entry.id == focusedAutofillId
                    }?.let {
                        it.isFocused = true
                    }
                }
        }
        
        
        if (summary.focusEntry?.isHtmlEntry == true) {
            val htmlEntries = summary.entries.filter { it.isHtmlEntry }
            summary.entries.apply {
                clear()
                addAll(htmlEntries)
            }
        }
        return summary.build(completers)
            ?.takeUnless { it.isEmpty() }
            ?.takeUnless {
                
                BrowserDetectionHelper.isBrowserSupported(it.packageName) && it.webDomain == null
            }
    }

    private suspend fun fillSummary(
        fillContexts: List<FillContext>,
        summary: AutoFillHintSummaryBuilder,
        needBrotherExtractor: Boolean,
    ) {
        fillContexts.forEach {
            if (!coroutineContext.isActive) return
            try {
                fillSummary(summary, it.structure, needBrotherExtractor)
            } catch (t: IllegalStateException) {
                if (t.message?.contains("DeadObjectException") == true) {
                    
                } else {
                    throw t 
                }
            }
        }
    }

    private suspend fun fillSummary(
        summary: AutoFillHintSummaryBuilder,
        structure: AssistStructure,
        needBrotherExtractor: Boolean,
    ) {
        for (i in 0 until structure.windowNodeCount) {
            if (!coroutineContext.isActive) return
            val parent = if (i > 0 && i < structure.windowNodeCount) {
                structure.getWindowNodeAt(i - 1).rootViewNode
            } else {
                null
            }
            fillSummary(
                summary,
                AutoFillApiViewNode.newInstance(
                    structure.getWindowNodeAt(i).rootViewNode,
                    parent,
                ),
                viewId = idGenerator.nextId(),
                parentViewIds = emptyList(),
                needBrotherExtractor,
            )
        }
    }

    private suspend fun fillSummary(
        summary: AutoFillHintSummaryBuilder,
        viewNode: AutoFillViewNode,
        viewId: String,
        parentViewIds: List<String>,
        needBrotherExtractor: Boolean,
    ) {
        Extractor(viewNode, summary.packageName, needBrotherExtractor).apply {
            summary.addWebsiteIfValid(website)
            build(
                autofillValueFactory = autofillValueFactory,
                viewId = viewId,
                parentsIds = parentViewIds,
            )
                ?.let { summary.put(it) }
        }

        
        summary.addWebsiteIfValid(viewNode.url)
        viewNode.children.forEach { childViewNode ->
            if (!coroutineContext.isActive) return
            fillSummary(
                summary = summary,
                viewNode = childViewNode,
                viewId = idGenerator.nextId(),
                parentViewIds = parentViewIds + viewId,
                needBrotherExtractor = needBrotherExtractor,
            )
        }
    }

    companion object {
        private const val DETECTION_TIMEOUT = 4000L
    }
}