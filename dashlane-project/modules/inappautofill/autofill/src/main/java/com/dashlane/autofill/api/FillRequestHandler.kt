package com.dashlane.autofill.api

import android.content.Context
import android.os.Bundle
import android.service.autofill.FillResponse
import android.view.autofill.AutofillId
import android.widget.inline.InlinePresentationSpec
import com.dashlane.autofill.api.changepassword.AutoFillChangePasswordConfiguration
import com.dashlane.autofill.api.fillresponse.FillResponseCreator
import com.dashlane.autofill.api.request.autofill.database.ItemLoader
import com.dashlane.autofill.api.util.SmsOtpPossibleChecker
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal interface FillRequestHandler {
    suspend fun getFillResponse(
        context: Context,
        clientState: Bundle?,
        summary: AutoFillHintSummary,
        inlineSpecs: List<InlinePresentationSpec>?,
        focusedAutofillId: AutofillId? = null
    ): FillResponse?
}

internal class FillRequestHandlerImpl @Inject constructor(
    private val fillResponseCreator: FillResponseCreator,
    private val itemLoader: ItemLoader,
    private val changePasswordConfiguration: AutoFillChangePasswordConfiguration
) : FillRequestHandler {

    override suspend fun getFillResponse(
        context: Context,
        clientState: Bundle?,
        summary: AutoFillHintSummary,
        inlineSpecs: List<InlinePresentationSpec>?,
        focusedAutofillId: AutofillId?
    ): FillResponse? {
        
        if (summary.formType == AutoFillFormType.OTP &&
            !SmsOtpPossibleChecker.isSmsOtpAutofillPossible(context, summary.packageName)
        ) {
            return null
        }

        
        var changePasswordConfig = changePasswordConfiguration
        val items = withContext(Default) {
            val userName = if (isInChangePasswordMode(summary, changePasswordConfiguration)) {
                changePasswordConfiguration.filterOnUsername
            } else {
                
                changePasswordConfiguration.onDomainChanged.invoke()
                changePasswordConfig = AutoFillChangePasswordConfiguration()
                null
            }

            return@withContext itemLoader.loadSuggestions(
                formType = summary.formType,
                packageName = summary.packageName,
                url = summary.webDomain,
                username = userName
            )?.sortedByDescending {
                it.lastUsedDate
            }
        }

        val fillResponseWrapper = fillResponseCreator.createFor(
            clientState = clientState,
            summary = summary,
            result = items,
            inlineSpecs = inlineSpecs,
            focusedAutofillId = focusedAutofillId,
            changePasswordConfiguration = changePasswordConfig
        )

        
        return fillResponseWrapper?.toAndroidFillResponse()
    }

    private fun isInChangePasswordMode(
        summary: AutoFillHintSummary,
        configuration: AutoFillChangePasswordConfiguration
    ) = !configuration.isEmpty &&
            summary.webDomain?.contains(configuration.filterOnDomain ?: "", true) == true
}