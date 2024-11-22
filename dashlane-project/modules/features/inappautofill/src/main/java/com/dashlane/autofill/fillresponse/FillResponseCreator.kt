package com.dashlane.autofill.fillresponse

import android.content.Context
import android.os.Bundle
import android.view.autofill.AutofillId
import android.widget.inline.InlinePresentationSpec
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.announcement.KeyboardAutofillService
import com.dashlane.autofill.changepassword.AutoFillChangePasswordConfiguration
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.field.AutoFillHint
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.getPreviousEntriesFrom
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.phishing.PhishingAttemptLevel
import com.dashlane.autofill.phishing.PhishingWarningDataProvider
import com.dashlane.autofill.phishing.UrlPhishingClassificationHelper
import com.dashlane.autofill.request.autofill.logger.logShowList
import com.dashlane.autofill.setClassLoaderInDashlane
import com.dashlane.autofill.unlockfill.AutofillAuthActivity
import com.dashlane.autofill.util.AutofillNavigationService
import com.dashlane.autofill.util.SaveInfoWrapper
import com.dashlane.autofill.util.copyPlusEntries
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.canUseAntiPhishing
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.LinkedList
import javax.inject.Inject

internal interface FillResponseCreator {
    suspend fun createFor(
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
    private val keyboardAutofillService: KeyboardAutofillService,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val urlPhishingClassificationHelper: UrlPhishingClassificationHelper,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val phishingWarningDataProvider: PhishingWarningDataProvider
) : FillResponseCreator {

    @Suppress("LongMethod")
    override suspend fun createFor(
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

        
        val needChangePassword = needChangePassword(summary)
        val isGuidedChangePassword = !changePasswordConfiguration.isEmpty && needChangePassword

        val updatedEntriesSummary =
            
            if (isGuidedChangePassword) summary else summary.copyPlusEntries(getPreviousEntriesFrom(clientState))

        val hasSaveInfo = saveInfoWrapper.hasSaveInfo()

        val hasResult = !result.isNullOrEmpty()

        val phishingAttemptLevel =
            if (userFeaturesChecker.canUseAntiPhishing() &&
                preferencesManager[sessionManager.session?.username].isAntiPhishingEnable &&
                !phishingWarningDataProvider.isWebsiteIgnored(summary.webDomain)
            ) {
                getPhishingLevel(summary.webDomain)
            } else {
                PhishingAttemptLevel.NONE
            }

        
        if (keyboardAutofillService.canDisplayOnBoardingSuggestion() && inlineSpecs != null) {
            val onboardingIntentSender = navigationService.getOnBoardingIntentSender()
            responseBuilder.buildOnBoardingItem(summary, onboardingIntentSender)
            keyboardAutofillService.setOnBoardingSuggestionDisplayed()
        }

        
        if (canPopulateResponse(isOtp, hasResult, hasSaveInfo)) {
            responseBuilder.buildResults(
                updatedEntriesSummary,
                isOtp,
                isGuidedChangePassword,
                result,
                phishingAttemptLevel
            )
        }

        
        val isViewAllAccountEnabled = needsViewAllAccounts(
            summary = updatedEntriesSummary,
            hasDataSet = hasResult,
            isKeyboardAutofill = hasInlineSpecs
        )

        
        responseBuilder.buildOptionsItems(
            updatedEntriesSummary = updatedEntriesSummary,
            isChangePassword = needChangePassword,
            inlines = inlines,
            isOtp = isOtp,
            hasResult = hasResult,
            isViewAllAccountEnabled = isViewAllAccountEnabled,
            phishingAttemptLevel = phishingAttemptLevel
        )

        
        if (phishingAttemptLevel != PhishingAttemptLevel.NONE) {
            responseBuilder.buildPhishingAttempt(summary, phishingAttemptLevel)
        }

        
        responseBuilder.setScrollingRemoteView(remoteViewProvider.forScrolling())
        val response = responseBuilder.build()
        if (response != null && response.dataSets.isNotEmpty()) {
            logger.logShowList(
                packageName = updatedEntriesSummary.packageName,
                formType = summary.formType,
                forKeyboard = hasInlineSpecs,
                isNativeApp = summary.formSource is ApplicationFormSource,
                totalCount = response.dataSets.count(),
                phishingAttemptLevel = phishingAttemptLevel,
            )
        }
        return response
    }

    private suspend fun getPhishingLevel(url: String?): PhishingAttemptLevel =
        url?.let { urlPhishingClassificationHelper.classifyWebsitePhishingLevel(url) } ?: PhishingAttemptLevel.NONE

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

    private fun needChangePassword(summary: AutoFillHintSummary): Boolean {
        
        if (summary.entries.firstOrNull { it.hasHint(AutoFillHint.NEW_PASSWORD) } != null) {
            return true
        }
        
        val passwordFieldCount = summary.entries.count {
            it.hasOneOfHints(arrayOf(AutoFillHint.PASSWORD, AutoFillHint.CURRENT_PASSWORD, AutoFillHint.NEW_PASSWORD))
        }
        return passwordFieldCount > 1
    }
}