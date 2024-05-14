package com.dashlane.autofill.fillresponse

import android.content.IntentSender
import android.os.Bundle
import android.view.autofill.AutofillValue
import android.widget.inline.InlinePresentationSpec
import com.dashlane.autofill.addPreviousEntriesFrom
import com.dashlane.autofill.model.ItemToFill
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import java.util.LinkedList

internal fun FillResponseWrapper.Builder.buildCreateAccount(
    updatedEntriesSummary: AutoFillHintSummary,
    hasDataSet: Boolean
) {
    createAccountDataSet = dataSetCreator.createForCreateAccount(
        summary = updatedEntriesSummary,
        hasFillResponse = hasDataSet,
        inlineSpec = inlineSpecs?.popOrLast()
    )
}

internal fun FillResponseWrapper.Builder.buildPinnedItem(
    updatedEntriesSummary: AutoFillHintSummary,
    hasDataSet: Boolean
) {
    pinnedItemDataSet = dataSetCreator.createForPinnedItem(
        summary = updatedEntriesSummary,
        hasFillResponse = hasDataSet,
        inlineSpec = inlineSpecs?.popOrLast()
    )
}

internal fun FillResponseWrapper.Builder.buildViewAllAccount(
    updatedEntriesSummary: AutoFillHintSummary,
    hasDataSet: Boolean
) {
    viewAllAccountsDataSet = dataSetCreator.createViewAllItems(
        summary = updatedEntriesSummary,
        hasFillResponse = hasDataSet,
        inlineSpec = inlineSpecs?.popOrLast()
    )
}

internal fun FillResponseWrapper.Builder.buildViewChangePassword(
    updatedEntriesSummary: AutoFillHintSummary,
    hasDataSet: Boolean
) {
    changePasswordDataSet = dataSetCreator.createForChangePassword(
        summary = updatedEntriesSummary,
        hasFillResponse = hasDataSet,
        inlineSpec = inlineSpecs?.popOrLast()
    )
}

internal fun FillResponseWrapper.Builder.buildResults(
    updatedEntriesSummary: AutoFillHintSummary,
    isOtp: Boolean,
    isGuidedChangePassword: Boolean,
    result: List<ItemToFill>?
) {
    setClientState(Bundle().apply { addPreviousEntriesFrom(updatedEntriesSummary) })
    val otpItemDataSet = if (isOtp) {
        dataSetCreator.createForSmsOtp(
            summary = updatedEntriesSummary,
            inlineSpec = inlineSpecs?.popOrLast()
        )
    } else {
        null
    }

    val vaultItemDataSet = result?.map { itemToFill ->
        dataSetCreator.create(
            summary = updatedEntriesSummary,
            itemToFill = itemToFill,
            requireLock = true,
            isGuidedChangePassword = isGuidedChangePassword,
            inlineSpec = inlineSpecs?.popOrLast()
        )
    }
    vaultItemDataSet?.plus(otpItemDataSet)?.filterNotNull()?.let { itemDataSets.addAll(it) }
}

internal fun FillResponseWrapper.Builder.buildOnBoardingItem(
    summary: AutoFillHintSummary,
    sender: IntentSender
) {
    val autofillId = summary.focusEntry?.id ?: return
    onBoardingDataSet = dataSetCreator.createForOnBoarding(inlineSpecs?.popOrLast())?.apply {
        setValue(autofillId, AutofillValue.forText(""))
        setAuthentication(sender)
    }
}

internal fun FillResponseWrapper.Builder.createForLogout(
    summary: AutoFillHintSummary,
    sender: IntentSender
): FillResponseWrapper? {
    val inlineSpec = inlineSpecs?.popOrLast()
    val autofillId = summary.focusEntry?.id ?: return null
    val presentation = remoteViewProvider.forLogout()
    val presentationInline = inlinePresentationProvider?.forLogout(inlineSpec)

    val datasetBuilder = DatasetWrapperBuilder(presentation, presentationInline)
    datasetBuilder.setValue(autofillId, AutofillValue.forText(""))
    datasetBuilder.setAuthentication(sender)
    logoutDataSet = datasetBuilder
    pauseDataSet = dataSetCreator.createForPause(
        summary = summary,
        hasFillResponse = false,
        inlineSpec = inlineSpec
    )

    return build()
}

internal fun FillResponseWrapper.Builder.buildOptionsItems(
    updatedEntriesSummary: AutoFillHintSummary,
    isChangePassword: Boolean,
    inlines: LinkedList<InlinePresentationSpec>?,
    isOtp: Boolean,
    hasResult: Boolean,
    isViewAllAccountEnabled: Boolean
) {
    val hasItemsDataSet = hasItemDataSet()
    val hasChangePassword = hasItemsDataSet && isChangePassword

    if (hasChangePassword) {
        buildViewChangePassword(
            updatedEntriesSummary = updatedEntriesSummary,
            hasDataSet = hasItemsDataSet,
        )
    }
    if (isViewAllAccountEnabled) {
        buildViewAllAccount(
            updatedEntriesSummary = updatedEntriesSummary,
            hasDataSet = hasItemsDataSet
        )
        buildCreateAccount(
            updatedEntriesSummary = updatedEntriesSummary,
            hasDataSet = hasItemsDataSet
        )
    }
    buildPinnedItem(
        updatedEntriesSummary = updatedEntriesSummary,
        hasDataSet = hasItemsDataSet
    )

    
    if ((hasItemsDataSet || isViewAllAccountEnabled) && !isOtp) {
        pauseDataSet = dataSetCreator.createForPause(
            summary = updatedEntriesSummary,
            hasFillResponse = hasResult,
            inlineSpec = inlines?.popOrLast()
        )
    }
}