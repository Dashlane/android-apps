package com.dashlane.autofill.fillresponse

import android.content.IntentSender
import android.service.autofill.Dataset
import android.service.autofill.InlinePresentation
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.dashlane.autofill.util.setFieldCompat

class DatasetWrapper(
    private var remoteViews: RemoteViews?,
    private var authentication: IntentSender?,
    val autofillIdsValues: Map<AutofillId, AutofillValue>,
    private var inlinePresentation: InlinePresentation? = null,
) {
    fun toAndroidDataset(): Dataset {
        val datasetBuilder = Dataset.Builder()
        datasetBuilder.setAuthentication(authentication)
        autofillIdsValues.forEach { datasetBuilder.setFieldCompat(it.key, it.value, remoteViews, inlinePresentation) }
        return datasetBuilder.build()
    }
}