package com.dashlane.autofill.fillresponse

import android.content.IntentSender
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.InlinePresentation
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews

class DatasetWrapper(
    private var remoteViews: RemoteViews?,
    private var authentication: IntentSender?,
    val autofillIdsValues: Map<AutofillId, AutofillValue>,
    private var inlinePresentation: InlinePresentation? = null
) {
    fun toAndroidDataset(): Dataset {
        val datasetBuilder = Dataset.Builder()
        datasetBuilder.setAuthentication(authentication)
        autofillIdsValues.forEach {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && inlinePresentation != null) {
                datasetBuilder.setValue(it.key, it.value, remoteViews!!, inlinePresentation!!)
            } else {
                datasetBuilder.setValue(it.key, it.value, remoteViews!!)
            }
        }
        return datasetBuilder.build()
    }
}