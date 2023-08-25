package com.dashlane.autofill.api.request.save

import android.content.Context
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import kotlinx.coroutines.CoroutineScope

internal abstract class SaveRequest(
    val summary: AutoFillHintSummary,
    val usageLog: AutofillAnalyzerDef.IAutofillUsageLog,
    val databaseAccess: AutofillAnalyzerDef.DatabaseAccess
) {

    abstract fun execute(
        context: Context,
        coroutineScope: CoroutineScope,
        saveCallback: SaveCallback,
        hasInline: Boolean
    )

    open fun notifyLogout(callback: SaveCallback, forKeyboard: Boolean) {
        notifyLogout(callback)
    }

    internal fun notifyLogout(callback: SaveCallback) {
        callback.onFailure("User is logged out")
    }

    sealed class SaveResult {
        object Success : SaveResult()
        data class Failure(val error: String) : SaveResult()
    }
}