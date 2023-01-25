package com.dashlane.autofill.api.fillresponse

import android.content.Context
import android.widget.inline.InlinePresentationSpec
import com.dashlane.autofill.api.emptywebsitewarning.view.EmptyWebsiteWarningActivity
import com.dashlane.autofill.api.fillresponse.filler.AuthentifiantFiller
import com.dashlane.autofill.api.fillresponse.filler.AuthentifiantSyncObjectFiller
import com.dashlane.autofill.api.fillresponse.filler.CreditCardFiller
import com.dashlane.autofill.api.fillresponse.filler.CreditCardSyncObjectFiller
import com.dashlane.autofill.api.fillresponse.filler.EmailFiller
import com.dashlane.autofill.api.fillresponse.filler.OtpCodeFiller
import com.dashlane.autofill.api.fillresponse.filler.PauseFiller
import com.dashlane.autofill.api.fillresponse.filler.ViewAllAccountsFiller
import com.dashlane.autofill.api.model.AuthentifiantItemToFill
import com.dashlane.autofill.api.model.AuthentifiantSummaryItemToFill
import com.dashlane.autofill.api.model.CreditCardItemToFill
import com.dashlane.autofill.api.model.CreditCardSummaryItemToFill
import com.dashlane.autofill.api.model.EmailItemToFill
import com.dashlane.autofill.api.model.ItemToFill
import com.dashlane.autofill.api.model.TextItemToFill
import com.dashlane.autofill.api.ui.SmsOtpAutofillActivity
import com.dashlane.autofill.api.unlockfill.AutofillAuthActivity
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface DataSetCreator {
    fun create(
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean,
        isChangePassword: Boolean,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder?

    fun createForOnBoarding(inlineSpec: InlinePresentationSpec?): DatasetWrapperBuilder?

    fun createForSmsOtp(
        summary: AutoFillHintSummary,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder?

    fun createViewAllItems(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
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
        item: AuthentifiantSummaryItemToFill,
        packageName: String,
        summary: AutoFillHintSummary,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder?

    fun createForPinnedItem(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
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
    private val emptyWebsiteWarningIntentProvider: EmptyWebsiteWarningIntentProvider
) : DataSetCreator {

    override fun create(
        summary: AutoFillHintSummary,
        item: ItemToFill,
        requireLock: Boolean,
        isChangePassword: Boolean,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder? {
        val remoteView = remoteViewProvider.forItem(item, summary) ?: return null
        val remoteViewInline = inlinePresentationProvider?.forItem(item, inlineSpec)
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)
        val hasValue = fill(dataSetBuilder, summary, item, requireLock)
        if (!hasValue) {
            return null
        }
        val primaryItem = when (item) {
            is AuthentifiantItemToFill -> item.primaryItem.toSummary()
            is AuthentifiantSummaryItemToFill -> item.primaryItem
            is CreditCardItemToFill -> item.primaryItem.toSummary()
            is CreditCardSummaryItemToFill -> item.primaryItem
            else -> null
        }
        if (primaryItem != null && requireLock) {

            val intentSender = if (showEmptyWebsiteWarning(summary.webDomain, primaryItem)) {
                emptyWebsiteWarningIntentProvider.getEmptyWebsiteWarningSender(
                    applicationContext,
                    primaryItem.id,
                    summary,
                    item.matchType ?: MatchType.REGULAR
                )
            } else {
                AutofillAuthActivity.getAuthIntentSenderForDataset(
                    applicationContext,
                    primaryItem.id,
                    primaryItem.syncObjectType,
                    summary,
                    inlineSpec.isAvailable(),
                    isChangePassword,
                    item.matchType
                )
            }
            dataSetBuilder.setAuthentication(intentSender)
        }
        return dataSetBuilder
    }

    

    private fun showEmptyWebsiteWarning(webDomain: String?, primaryItem: SummaryObject) =
        webDomain.isNotSemanticallyNull() &&
                (primaryItem is SummaryObject.Authentifiant) &&
                primaryItem.url.isSemanticallyNull()

    override fun createForOnBoarding(inlineSpec: InlinePresentationSpec?): DatasetWrapperBuilder {
        val remoteView = remoteViewProvider.emptyView() 
        val remoteViewInline = inlinePresentationProvider?.forOnBoarding(inlineSpec)

        return DatasetWrapperBuilder(remoteView, remoteViewInline)
    }

    override fun createForEmptyWebsite(
        item: AuthentifiantSummaryItemToFill,
        packageName: String,
        summary: AutoFillHintSummary,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder {
        val remoteView = remoteViewProvider.forEmptyWebsite(item, packageName)
        val remoteViewInline = inlinePresentationProvider?.forSmsOtp(inlineSpec)
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)

        dataSetBuilder.setAuthentication(
            EmptyWebsiteWarningActivity.getAuthIntentSenderForEmptyWebsiteWarning(
                applicationContext,
                item.getItemId(),
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
        val remoteViewInline = inlinePresentationProvider?.forSmsOtp(inlineSpec)
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, remoteViewInline)
        if (!OtpCodeFiller(autofillValueFactory).fillNoValue(dataSetBuilder, summary)) {
            return null
        }
        dataSetBuilder.setAuthentication(
            SmsOtpAutofillActivity.getIntentSenderForDataset(
                applicationContext,
                summary,
                forKeyboard = inlineSpec.isAvailable()
            )
        )
        return dataSetBuilder
    }

    override fun createViewAllItems(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
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
                applicationContext,
                summary,
                hasFillResponse,
                forKeyboard = inlineSpec.isAvailable()
            )
        )

        return dataSetBuilder
    }

    override fun createForPinnedItem(
        summary: AutoFillHintSummary,
        hasFillResponse: Boolean,
        inlineSpec: InlinePresentationSpec?
    ): DatasetWrapperBuilder? {
        val remoteView = remoteViewProvider.emptyView()
        val dataSetBuilder = DatasetWrapperBuilder(remoteView, inlinePresentationProvider?.forPinnedItem(inlineSpec))

        val hasValue = fillForViewAllItems(dataSetBuilder, summary)
        if (!hasValue) {
            return null
        }

        dataSetBuilder.setAuthentication(
            viewAllAccountsActionIntentProvider.getViewAllAccountsIntentSender(
                applicationContext,
                summary,
                hasFillResponse,
                forKeyboard = inlineSpec.isAvailable()
            )
        )
        return dataSetBuilder
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
                applicationContext,
                summary,
                hasFillResponse,
                forKeyboard = inlineSpec.isAvailable()
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
                applicationContext,
                summary,
                forKeyboard = inlineSpec.isAvailable()
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
            is AuthentifiantItemToFill -> AuthentifiantSyncObjectFiller(autofillValueFactory)
            is AuthentifiantSummaryItemToFill -> AuthentifiantFiller(autofillValueFactory)
            is CreditCardItemToFill -> CreditCardSyncObjectFiller(autofillValueFactory)
            is CreditCardSummaryItemToFill -> CreditCardFiller(autofillValueFactory)
            is EmailItemToFill -> EmailFiller(autofillValueFactory)
            is TextItemToFill -> if (summary.formType == AutoFillFormType.OTP) {
                OtpCodeFiller(autofillValueFactory)
            } else {
                null
            }
        }?.fill(dataSetBuilder, summary, item, requireLock)
            ?: false 
    }
}