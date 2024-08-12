package com.dashlane.autofill.fillresponse

import android.content.Context
import android.widget.inline.InlinePresentationSpec
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.emptywebsitewarning.view.EmptyWebsiteWarningActivity
import com.dashlane.autofill.fillresponse.filler.AuthentifiantFiller
import com.dashlane.autofill.fillresponse.filler.CreditCardFiller
import com.dashlane.autofill.fillresponse.filler.EmailFiller
import com.dashlane.autofill.fillresponse.filler.OtpCodeFiller
import com.dashlane.autofill.fillresponse.filler.PauseFiller
import com.dashlane.autofill.fillresponse.filler.PhishingWarningFiller
import com.dashlane.autofill.fillresponse.filler.ViewAllAccountsFiller
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.EmailItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.model.OtpItemToFill
import com.dashlane.autofill.phishing.PhishingAttemptLevel
import com.dashlane.autofill.ui.SmsOtpAutofillActivity
import com.dashlane.autofill.unlockfill.AutofillAuthActivity
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface DataSetCreator {
    fun create(
        summary: AutoFillHintSummary,
        itemToFill: ItemToFill,
        requireLock: Boolean,
        isGuidedChangePassword: Boolean,
        inlineSpec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): DatasetWrapperBuilder?

    fun createForOnBoarding(inlineSpec: InlinePresentationSpec?): DatasetWrapperBuilder?

    fun createForSmsOtp(
        summary: AutoFillHintSummary,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder?

    fun createViewAllItems(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): DatasetWrapperBuilder?

    fun createForPause(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder?

    fun createForCreateAccount(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder?

    fun createForChangePassword(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder?

    fun createForEmptyWebsite(
        item: AuthentifiantItemToFill,
        packageName: String,
        summary: AutoFillHintSummary,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder?

    fun createForPinnedItem(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): DatasetWrapperBuilder?

    fun createForPhishingWarning(
        summary: AutoFillHintSummary,
        inlineSpec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): DatasetWrapperBuilder?

    fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean
}

internal class DataSetCreatorImpl @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context,
    private val remoteViewProvider: RemoteViewProvider,
    private val inlinePresentationProvider: InlinePresentationProvider?,
    private val autofillValueFactory: AutofillValueFactory,
    private val viewAllAccountsActionIntentProvider: ViewAllAccountsActionIntentProvider,
    private val pauseActionIntentProvider: PauseActionIntentProvider,
    private val createAccountActionIntentProvider: CreateAccountActionIntentProvider,
    private val changePasswordActionIntentProvider: ChangePasswordActionIntentProvider,
    private val emptyWebsiteWarningIntentProvider: EmptyWebsiteWarningIntentProvider,
    private val phishingWarningIntentProvider: PhishingWarningIntentProvider,
    private val frozenStateManager: FrozenStateManager,
) : DataSetCreator {

    private val isAccountFrozen: Boolean
        get() = frozenStateManager.isAccountFrozen

    override fun create(
        summary: AutoFillHintSummary,
        itemToFill: ItemToFill,
        requireLock: Boolean,
        isGuidedChangePassword: Boolean,
        inlineSpec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): DatasetWrapperBuilder? {
        val remoteView = remoteViewProvider.forItem(itemToFill) ?: return null
        val remoteViewInline = inlinePresentationProvider?.forItem(itemToFill, inlineSpec)
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)
        val hasValue = fill(dataSetBuilder, summary, itemToFill, requireLock)
        if (!hasValue) {
            return null
        }
        if (requireLock) {
            val intentSender = if (showEmptyWebsiteWarning(summary.webDomain, itemToFill)) {
                emptyWebsiteWarningIntentProvider.getEmptyWebsiteWarningSender(
                    context = applicationContext,
                    itemId = itemToFill.itemId,
                    summary = summary,
                    matchType = itemToFill.matchType ?: MatchType.REGULAR,
                    isAccountFrozen = isAccountFrozen
                )
            } else {
                AutofillAuthActivity.getAuthIntentSenderForDataset(
                    context = applicationContext,
                    itemToFill = itemToFill,
                    summary = summary,
                    forKeyboard = inlineSpec.isAvailable(),
                    guidedChangePasswordFlow = isGuidedChangePassword,
                    matchType = itemToFill.matchType,
                    phishingAttemptLevel = phishingAttemptLevel,
                    isAccountFrozen = isAccountFrozen
                )
            }
            dataSetBuilder.setAuthentication(intentSender)
        }
        return dataSetBuilder
    }

    private fun showEmptyWebsiteWarning(webDomain: String?, item: ItemToFill) =
        webDomain.isNotSemanticallyNull() && (item is AuthentifiantItemToFill) && item.url.isSemanticallyNull()

    override fun createForOnBoarding(inlineSpec: InlinePresentationSpec?): DatasetWrapperBuilder {
        val remoteView = remoteViewProvider.emptyView() 
        val remoteViewInline = inlinePresentationProvider?.forOnBoarding(inlineSpec)

        return DatasetWrapperBuilder(remoteView, remoteViewInline)
    }

    override fun createForEmptyWebsite(
        item: AuthentifiantItemToFill,
        packageName: String,
        summary: AutoFillHintSummary,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder {
        val remoteView = remoteViewProvider.forEmptyWebsite(item)
        val remoteViewInline = inlinePresentationProvider?.forOtp(inlineSpec)
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)

        dataSetBuilder.setAuthentication(
            EmptyWebsiteWarningActivity.getAuthIntentSenderForEmptyWebsiteWarning(
                applicationContext,
                item.itemId,
                summary,
                item.matchType ?: MatchType.REGULAR
            )
        )

        return dataSetBuilder
    }

    override fun createForSmsOtp(
        summary: AutoFillHintSummary,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder? {
        val remoteView = remoteViewProvider.forOtp()
        val remoteViewInline = inlinePresentationProvider?.forOtp(inlineSpec)
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)
        if (!OtpCodeFiller(autofillValueFactory).fillNoValue(dataSetBuilder, summary)) {
            return null
        }
        dataSetBuilder.setAuthentication(
            SmsOtpAutofillActivity.getIntentSenderForDataset(
                context = applicationContext,
                summary = summary,
                forKeyboard = inlineSpec.isAvailable(),
                isAccountFrozen = isAccountFrozen
            )
        )
        return dataSetBuilder
    }

    override fun createViewAllItems(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): DatasetWrapperBuilder? {
        val remoteView = if (hasFillResponse) {
            remoteViewProvider.forViewAllItems()
        } else {
            remoteViewProvider.forViewAllItemsOnEmptyResults()
        }
        val remoteViewInline = if (hasFillResponse) {
            inlinePresentationProvider?.forViewAllItems(inlineSpec)
        } else {
            inlinePresentationProvider?.forViewAllItemsOnEmptyResults(inlineSpec)
        }
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)

        val hasValue = fillForViewAllItems(dataSetBuilder, summary)
        if (!hasValue) {
            return null
        }

        dataSetBuilder.setAuthentication(
            viewAllAccountsActionIntentProvider.getViewAllAccountsIntentSender(
                context = applicationContext,
                summary = summary,
                hadCredentials = hasFillResponse,
                forKeyboard = inlineSpec.isAvailable(),
                phishingAttemptLevel = phishingAttemptLevel,
                isAccountFrozen = isAccountFrozen
            )
        )

        return dataSetBuilder
    }

    override fun createForPinnedItem(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): DatasetWrapperBuilder? {
        val dataSetBuilder = DatasetWrapperBuilder(
            remoteViewProvider.emptyView(),
            inlinePresentationProvider?.forPinnedItem(inlineSpec, phishingAttemptLevel)
        )

        val hasValue = fillForViewAllItems(dataSetBuilder, summary)
        if (!hasValue) {
            return null
        }

        dataSetBuilder.setAuthentication(
            viewAllAccountsActionIntentProvider.getViewAllAccountsIntentSender(
                context = applicationContext,
                summary = summary,
                hadCredentials = hasFillResponse,
                forKeyboard = inlineSpec.isAvailable(),
                phishingAttemptLevel = phishingAttemptLevel,
                isAccountFrozen = isAccountFrozen
            )
        )
        return dataSetBuilder
    }

    override fun createForPhishingWarning(
        summary: AutoFillHintSummary,
        inlineSpec: InlinePresentationSpec?,
        phishingAttemptLevel: PhishingAttemptLevel
    ): DatasetWrapperBuilder? {
        val datasetBuilder = DatasetWrapperBuilder(
            remoteViewProvider.emptyView(),
            inlinePresentationProvider?.forPhishingWarning(inlineSpec, phishingAttemptLevel)
        )

        
        val hasValue = PhishingWarningFiller(autofillValueFactory).fill(datasetBuilder, summary)
        if (!hasValue) {
            return null
        }

        datasetBuilder.setAuthentication(
            phishingWarningIntentProvider.getPhishingIntentAction(
                context = applicationContext,
                summary = summary,
                phishingAttemptLevel = phishingAttemptLevel,
                isAccountFrozen = isAccountFrozen
            )
        )
        return datasetBuilder
    }

    override fun createForPause(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder? {
        val remoteView = remoteViewProvider.forPause()
        val remoteViewInline = inlinePresentationProvider?.forPause(inlineSpec)
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)

        val hasValue = fillForPause(dataSetBuilder, summary)
        if (!hasValue) {
            return null
        }

        dataSetBuilder.setAuthentication(
            pauseActionIntentProvider.getPauseIntentSender(
                applicationContext,
                summary,
                hasFillResponse
            )
        )

        return dataSetBuilder
    }

    override fun createForCreateAccount(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder? {
        val remoteView = remoteViewProvider.forCreateAccount()
        val remoteViewInline = inlinePresentationProvider?.forAddAccount(inlineSpec)
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)

        
        val hasValue = fillForViewAllItems(dataSetBuilder, summary)
        if (!hasValue) {
            return null
        }

        dataSetBuilder.setAuthentication(
            createAccountActionIntentProvider.getCreateAccountIntentSender(
                context = applicationContext,
                summary = summary,
                hadCredentials = hasFillResponse,
                forKeyboard = inlineSpec.isAvailable(),
                isAccountFrozen = isAccountFrozen
            )
        )

        return dataSetBuilder
    }

    override fun createForChangePassword(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder? {
        val remoteView = remoteViewProvider.forChangePassword()
        val remoteViewInline = inlinePresentationProvider?.forChangePassword(inlineSpec)
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)

        
        val hasValue = fillForViewAllItems(dataSetBuilder, summary)
        if (!hasValue) {
            return null
        }
        dataSetBuilder.setAuthentication(
            changePasswordActionIntentProvider.getChangePasswordIntentSender(
                context = applicationContext,
                summary = summary,
                forKeyboard = inlineSpec.isAvailable(),
                isAccountFrozen = isAccountFrozen
            )
        )

        return dataSetBuilder
    }

    private fun fillForViewAllItems(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary
    ): Boolean {
        return ViewAllAccountsFiller(autofillValueFactory).fill(dataSetBuilder, summary)
    }

    private fun fillForPause(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary
    ): Boolean {
        return PauseFiller(autofillValueFactory).fill(dataSetBuilder, summary)
    }

    override fun fill(
        dataSetBuilder: DatasetWrapperBuilder,
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean
    ): Boolean {
        return when (item) {
            is AuthentifiantItemToFill -> AuthentifiantFiller(autofillValueFactory)
            is CreditCardItemToFill -> CreditCardFiller(autofillValueFactory)
            is EmailItemToFill -> EmailFiller(autofillValueFactory)
            is OtpItemToFill -> OtpCodeFiller(autofillValueFactory)
        }.fill(dataSetBuilder, summary, item, requireLock)
    }
}