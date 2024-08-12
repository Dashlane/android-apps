package com.dashlane.autofill.fillresponse

import android.os.Build
import android.os.Bundle
import android.service.autofill.FillResponse
import android.service.autofill.SaveInfo
import android.view.autofill.AutofillId
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec
import java.util.LinkedList

internal class FillResponseWrapper(
    val saveInfo: SaveInfo?,
    var clientState: Bundle?,
    val dataSets: List<DatasetWrapper>,
    val hasViewAllAccount: Boolean,
    val scrollingRemoteViews: RemoteViews?,
    val ignoreIds: List<AutofillId>? = null
) {

    @Suppress("SpreadOperator")
    fun toAndroidFillResponse(): FillResponse {
        return FillResponse.Builder().apply {
            saveInfo?.let {
                setSaveInfo(it)
            }
            clientState?.let {
                setClientState(it)
            }
            dataSets.forEach {
                addDataset(it.toAndroidDataset())
            }
            scrollingRemoteViews?.let {
                setScrollingRemoteView(scrollingRemoteViews)
            }
            ignoreIds?.let {
                setIgnoredIds(*it.toTypedArray())
            }
        }.build()
    }

    private fun FillResponse.Builder.setScrollingRemoteView(remoteViews: RemoteViews) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setFooter(remoteViews)
        }
    }

    class Builder(
        internal val inlineSpecs: LinkedList<InlinePresentationSpec>?,
        internal val dataSetCreator: DataSetCreator,
        internal val remoteViewProvider: RemoteViewProvider,
        internal val inlinePresentationProvider: InlinePresentationProvider?,
    ) {

        private var saveInfo: SaveInfo? = null
        private var clientState: Bundle? = null
        private var scrollingRemoteView: RemoteViews? = null
        private var ignoreAutofillIds: List<AutofillId>? = null

        var itemDataSets = mutableListOf<DatasetWrapperBuilder>()
        var logoutDataSet: DatasetWrapperBuilder? = null
        var onBoardingDataSet: DatasetWrapperBuilder? = null
        var viewAllAccountsDataSet: DatasetWrapperBuilder? = null
        var pauseDataSet: DatasetWrapperBuilder? = null
        var createAccountDataSet: DatasetWrapperBuilder? = null
        var changePasswordDataSet: DatasetWrapperBuilder? = null
        var pinnedItemDataSet: DatasetWrapperBuilder? = null
        var phishingDataSet: DatasetWrapperBuilder? = null

        fun hasItemDataSet() = itemDataSets.isNotEmpty()

        fun setSaveInfo(it: SaveInfo) {
            saveInfo = it
        }

        fun setClientState(apply: Bundle) {
            clientState = apply
        }

        fun setIgnoreAutofillIds(autofillIds: List<AutofillId>) {
            ignoreAutofillIds = autofillIds
        }

        fun setScrollingRemoteView(remoteViews: RemoteViews?) {
            scrollingRemoteView = remoteViews
        }

        fun build(): FillResponseWrapper? {
            val datasets = mutableListOf<DatasetWrapper>()

            
            phishingDataSet?.build()?.let { datasets.add(it) }

            
            onBoardingDataSet?.build()?.let { datasets.add(it) }

            
            val itemDatasetWrapper = itemDataSets.mapNotNull { it.build() }
            datasets.addAll(itemDatasetWrapper)

            
            logoutDataSet?.build()?.let { datasets.add(it) }

            val autofillIdsWithDatasets = itemDatasetWrapper.map { it.autofillIdsValues.keys }.flatten()
            var hasViewAllAccount = false

            
            buildDatasetWrapperLimitedToAutofillIds(changePasswordDataSet?.clone(), autofillIdsWithDatasets)?.let {
                datasets.add(it)
            }

            
            buildDatasetWrapperLimitedToAutofillIds(viewAllAccountsDataSet?.clone(), autofillIdsWithDatasets)?.let {
                datasets.add(it)
                hasViewAllAccount = true
            }

            
            buildDatasetWrapperLimitedToAutofillIds(createAccountDataSet?.clone(), autofillIdsWithDatasets)?.let {
                datasets.add(it)
            }

            
            buildDatasetWrapperLimitedToAutofillIds(pinnedItemDataSet?.clone(), autofillIdsWithDatasets)?.let {
                datasets.add(it)
            }

            if (datasets.isEmpty() && saveInfo == null) {
                return null
            }

            
            val pauseDataSetWrapper = pauseDataSet?.clone()
                ?.apply {
                    val datasetAutofillIds = datasets.map { it.autofillIdsValues.keys }.flatten()
                    limitAutofillIdTo(datasetAutofillIds)
                }
                ?.build()

            pauseDataSetWrapper?.let { datasets.add(0, it) }

            return FillResponseWrapper(
                saveInfo = saveInfo,
                clientState = clientState,
                dataSets = datasets,
                hasViewAllAccount = hasViewAllAccount,
                scrollingRemoteViews = getScrollingRemoteView(datasets),
                ignoreIds = ignoreAutofillIds
            )
        }

        private fun buildDatasetWrapperLimitedToAutofillIds(
            dataSetWrapperBuilder: DatasetWrapperBuilder?,
            autofillIdsWithDatasets: List<AutofillId>
        ): DatasetWrapper? {
            return dataSetWrapperBuilder?.apply {
                if (autofillIdsWithDatasets.isNotEmpty()) {
                    limitAutofillIdTo(autofillIdsWithDatasets)
                }
            }?.build()
        }

        private fun getScrollingRemoteView(datasets: List<DatasetWrapper>): RemoteViews? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                return null
            }
            if (!datasets.showScrolling()) {
                return null
            }

            return scrollingRemoteView
        }

        private fun List<DatasetWrapper>.showScrolling(): Boolean {
            return this
                .asSequence()
                .map {
                    it.autofillIdsValues.keys
                }
                .flatten()
                .groupBy { it }
                .map { it.value.size }
                .any { it > FILL_RESPONSE_VISIBLE_CELLS }
        }
    }

    companion object {
        private const val FILL_RESPONSE_VISIBLE_CELLS = 3
    }
}

fun <E> LinkedList<E>.popOrLast(): E =
    if (this.size > 1) {
        pop()
    } else {
        last
    }
