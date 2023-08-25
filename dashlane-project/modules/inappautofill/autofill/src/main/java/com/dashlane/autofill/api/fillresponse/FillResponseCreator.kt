package com.dashlane.autofill.api.fillresponse

import android.content.Context
import android.os.Bundle
import android.view.autofill.AutofillId
import android.widget.inline.InlinePresentationSpec
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.announcement.KeyboardAutofillService
import com.dashlane.autofill.api.changepassword.AutoFillChangePasswordConfiguration
import com.dashlane.autofill.api.getPreviousEntriesFrom
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.request.autofill.logger.logShowList
import com.dashlane.autofill.api.setClassLoaderInDashlane
import com.dashlane.autofill.api.unlockfill.AutofillAuthActivity
import com.dashlane.autofill.api.util.AutofillNavigationService
import com.dashlane.autofill.api.util.SaveInfoWrapper
import com.dashlane.autofill.api.util.copyPlusEntries
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.LinkedList
import javax.inject.Inject

internal interface FillResponseCreator {
    fun createFor(
        clientState: Bundle?,
        summary: AutoFillHintSummary,
        result: List<ItemToFill>?,
        inlineSpecs: List<InlinePresentationSpec>?,
        focusedAutofillId: AutofillId?,
        changePasswordConfiguration: AutoFillChangePasswordConfiguration
    ): FillResponseWrapper?
}

internal class FillResponseCreatorImpl @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context,
    private val remoteViewProvider: RemoteViewProvider,
    private val inlinePresentationProvider: InlinePresentationProvider?,
    private val dataSetCreator: DataSetCreator,
    private val logger: AutofillAnalyzerDef.IAutofillUsageLog,
    private val navigationService: AutofillNavigationService,
    private val keyboardAutofillService: KeyboardAutofillService
) : FillResponseCreator {

    @Suppress("LongMethod")
    override fun createFor(
        clientState: Bundle?,
        summary: AutoFillHintSummary,
        result: List<ItemToFill>?,
        inlineSpecs: List<InlinePresentationSpec>?,
        focusedAutofillId: AutofillId?,
        changePasswordConfiguration: AutoFillChangePasswordConfiguration
    ): FillResponseWrapper? {
        
        val inlines = inlineSpecs?.let { LinkedList(inlineSpecs) }
        val hasInlineSpecs = inlines?.isNotEmpty() ?: false

        val saveInfoWrapper = SaveInfoWrapper(summary)
        val responseBuilder = FillResponseWrapper.Builder(
            inlineSpecs = inlines,
            dataSetCreator = dataSetCreator,
            remoteViewProvider = remoteViewProvider,
            inlinePresentationProvider = inlinePresentationProvider
        )
        saveInfoWrapper.saveInfo?.let { responseBuilder.setSaveInfo(it) }

        
        focusedAutofillId?.takeUnless { summary.entries.any { entry -> entry.id == it } }?.let {
            responseBuilder.setIgnoreAutofillIds(listOf(it))
        }

        val isOtp = summary.formType == AutoFillFormType.OTP
        if (!isOtp && result == null) {
            val logOutIntentSender =
                AutofillAuthActivity.getAuthIntentSenderForLoggedOutDataset(
                    context = applicationContext,
                    summary = summary,
                    forKeyboard = hasInlineSpecs
                )
            return responseBuilder.createForLogout(summary, logOutIntentSender)
        }

        clientState?.apply {
            setClassLoaderInDashlane()
        }
        
        val isChangePassword = needChangePassword(summary, changePasswordConfiguration)

        val updatedEntriesSummary =
            
            if (isChangePassword) summary else summary.copyPlusEntries(getPreviousEntriesFrom(clientState))

        val hasSaveInfo = saveInfoWrapper.hasSaveInfo()

        val hasResult = !result.isNullOrEmpty()

        
        if (keyboardAutofillService.canDisplayOnBoardingSuggestion() && inlineSpecs != null) {
            val onboardingIntentSender = navigationService.getOnBoardingIntentSender()
            responseBuilder.buildOnBoardingItem(summary, onboardingIntentSender)
            keyboardAutofillService.setOnBoardingSuggestionDisplayed()
        }

        
        if (canPopulateResponse(isOtp, hasResult, hasSaveInfo)) {
            responseBuilder.buildResults(updatedEntriesSummary, isOtp, isChangePassword, result)
        }

        
        val isViewAllAccountEnabled = needsViewAllAccounts(
            summary = updatedEntriesSummary,
            hasDataSet = hasResult,
            isKeyboardAutofill = hasInlineSpecs
        )

        
        responseBuilder.buildOptionsItems(
            updatedEntriesSummary = updatedEntriesSummary,
            isChangePassword = isChangePassword,
            inlines = inlines,
            isOtp = isOtp,
            hasResult = hasResult,
            isViewAllAccountEnabled = isViewAllAccountEnabled
        )

        
        responseBuilder.setScrollingRemoteView(remoteViewProvider.forScrolling())
        val response = responseBuilder.build()
        if (response != null && response.dataSets.isNotEmpty()) {
            logger.logShowList(
                packageName = updatedEntriesSummary.packageName,
                formType = summary.formType,
                forKeyboard = hasInlineSpecs,
                isNativeApp = summary.formSource is ApplicationFormSource,
                totalCount = response.dataSets.count()
            )
        }
        return response
    }

    private fun canPopulateResponse(
        isOtp: Boolean,
        hasResult: Boolean,
        hasSaveInfo: Boolean
    ) = isOtp || hasResult || hasSaveInfo

    private fun needsViewAllAccounts(
        summary: AutoFillHintSummary,
        hasDataSet: Boolean,
        isKeyboardAutofill: Boolean
    ) = hasDataSet || summary.manualRequest || summary.isNotGuessingCredential() || isKeyboardAutofill

    private fun needChangePassword(
        summary: AutoFillHintSummary,
        config: AutoFillChangePasswordConfiguration
    ): Boolean {
        
        
        if (config.isEmpty) {
            return false
        }
        
        if (summary.entries.firstOrNull { it.hasHint(AutoFillHint.NEW_PASSWORD) } != null) {
            return true
        }
        
        val passwordFieldCount = summary.entries.count {
            it.hasOneOfHints(arrayOf(AutoFillHint.PASSWORD, AutoFillHint.CURRENT_PASSWORD))
        }
        val hasLoginField =
            summary.entries.count { it.hasOneOfHints(arrayOf(AutoFillHint.EMAIL_ADDRESS, AutoFillHint.USERNAME)) } != 0
        return !hasLoginField && passwordFieldCount > 0 || passwordFieldCount > 1
    }
}