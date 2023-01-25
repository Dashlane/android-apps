package com.dashlane.autofill.api

import android.content.Context
import android.os.Bundle
import android.service.autofill.FillResponse
import android.view.autofill.AutofillId
import android.widget.inline.InlinePresentationSpec
import com.dashlane.autofill.api.changepassword.AutoFillChangePasswordConfiguration
import com.dashlane.autofill.api.fillresponse.FillResponseCreator
import com.dashlane.autofill.api.fillresponse.RememberedAccountsService
import com.dashlane.autofill.api.model.AuthentifiantSummaryItemToFill
import com.dashlane.autofill.api.request.autofill.database.ItemLoader
import com.dashlane.autofill.api.util.SmsOtpPossibleChecker
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.performancelogger.TimeToAutofillLogger
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal interface FillRequestHandler {
    suspend fun getFillResponse(
        context: Context,
        clientState: Bundle?,
        summary: AutoFillHintSummary,
        inlineSpecs: List<InlinePresentationSpec>?,
        timeToAutofillLogger: TimeToAutofillLogger?,
        focusedAutofillId: AutofillId? = null
    ): FillResponse?
}



internal class FillRequestHandlerImpl @Inject constructor(
    private val fillResponseCreator: FillResponseCreator,
    private val itemLoader: ItemLoader,
    private val rememberedAccountsService: RememberedAccountsService,
    private val changePasswordConfiguration: AutoFillChangePasswordConfiguration
) : FillRequestHandler {

    override suspend fun getFillResponse(
        context: Context,
        clientState: Bundle?,
        summary: AutoFillHintSummary,
        inlineSpecs: List<InlinePresentationSpec>?,
        timeToAutofillLogger: TimeToAutofillLogger?,
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

            val matching = itemLoader.load(
                formType = summary.formType,
                packageName = summary.packageName,
                url = summary.webDomain,
                username = userName
            )
            
            
            
            if (matching != null) {
                val remembered =
                    rememberedAccountsService.fetchRememberedAccounts(summary.formSource)
                        ?.map {
                            AuthentifiantSummaryItemToFill(it, matchType = MatchType.REMEMBERED)
                        } ?: listOf()
                val rememberedIds = remembered.map { it.primaryItem.id }
                val matchingWithoutRemembered = matching.filter {
                    !rememberedIds.contains(it.getItemId())
                }

                remembered + matchingWithoutRemembered
            } else {
                null
            }?.sortedByDescending {
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

        
        timeToAutofillLogger?.hasDataSet =
            fillResponseWrapper?.dataSets?.isNotEmpty() == true || fillResponseWrapper?.hasViewAllAccount == true

        
        return fillResponseWrapper?.toAndroidFillResponse()
    }

    private fun isInChangePasswordMode(
        summary: AutoFillHintSummary,
        configuration: AutoFillChangePasswordConfiguration
    ) = !configuration.isEmpty &&
            summary.webDomain?.contains(configuration.filterOnDomain ?: "", true) == true
}