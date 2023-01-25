package com.dashlane.autofill.api

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.api.request.save.SaveAuthentifiantRequest
import com.dashlane.autofill.api.request.save.SaveCallback
import com.dashlane.autofill.api.request.save.SaveCreditCardRequest
import com.dashlane.autofill.api.util.copyPlusEntries
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

internal interface SaveRequestHandler {
    @SuppressLint("SwitchIntDef")
    fun onSaveRequest(
        context: Context,
        coroutineScope: CoroutineScope,
        clientState: Bundle?,
        summary: AutoFillHintSummary,
        saveCallback: SaveCallback,
        forKeyboard: Boolean
    )
}



internal class SaveRequestHandlerImpl @Inject constructor(
    private val databaseAccess: AutofillAnalyzerDef.DatabaseAccess,
    private val logger: AutofillAnalyzerDef.IAutofillUsageLog
) : SaveRequestHandler {

    @SuppressLint("SwitchIntDef")
    override fun onSaveRequest(
        context: Context,
        coroutineScope: CoroutineScope,
        clientState: Bundle?,
        summary: AutoFillHintSummary,
        saveCallback: SaveCallback,
        forKeyboard: Boolean
    ) {
        clientState?.apply {
            setClassLoaderInDashlane()
        }
        val updatedEntriesSummary = summary.copyPlusEntries(getPreviousEntriesFrom(clientState))

        val saveRequest = when (val formType = summary.formType) {
            AutoFillFormType.CREDENTIAL -> SaveAuthentifiantRequest(
                updatedEntriesSummary,
                logger,
                databaseAccess
            )
            AutoFillFormType.CREDIT_CARD -> SaveCreditCardRequest(
                updatedEntriesSummary,
                logger,
                databaseAccess
            )
            else -> {
                saveCallback.onFailure("FormType $formType not supported")
                null
            }
        } ?: return
        saveRequest.execute(context, coroutineScope, saveCallback, forKeyboard)
    }
}