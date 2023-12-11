package com.dashlane.autofill.fillresponse

import android.content.Context
import android.widget.inline.InlinePresentationSpec
import com.dashlane.autofill.emptywebsitewarning.view.EmptyWebsiteWarningActivity
import com.dashlane.autofill.fillresponse.filler.AuthentifiantFiller
import com.dashlane.autofill.fillresponse.filler.CreditCardFiller
import com.dashlane.autofill.fillresponse.filler.EmailFiller
import com.dashlane.autofill.fillresponse.filler.OtpCodeFiller
import com.dashlane.autofill.fillresponse.filler.PauseFiller
import com.dashlane.autofill.fillresponse.filler.ViewAllAccountsFiller
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.model.EmailItemToFill
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.model.OtpItemToFill
import com.dashlane.autofill.ui.SmsOtpAutofillActivity
import com.dashlane.autofill.unlockfill.AutofillAuthActivity
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
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
        item: AuthentifiantItemToFill,
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
        itemToFill: ItemToFill,
        requireLock: Boolean,
        isGuidedChangePassword: Boolean,
        inlineSpec: InlinePresentationSpec?
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
                    applicationContext,
                    itemToFill.itemId,
                    summary,
                    itemToFill.matchType ?: MatchType.REGULAR
                )
            } else {
                AutofillAuthActivity.getAuthIntentSenderForDataset(
                    applicationContext,
                    itemToFill,
                    summary,
                    inlineSpec.isAvailable(),
                    isGuidedChangePassword,
                    itemToFill.matchType
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
            is AuthentifiantItemToFill -> AuthentifiantFiller(autofillValueFactory)
            is CreditCardItemToFill -> CreditCardFiller(autofillValueFactory)
            is EmailItemToFill -> EmailFiller(autofillValueFactory)
            is OtpItemToFill -> OtpCodeFiller(autofillValueFactory)
        }.fill(dataSetBuilder, summary, item, requireLock)
    }
}