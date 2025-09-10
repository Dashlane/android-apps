package com.dashlane.autofill.util

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.R
import android.os.Build.VERSION_CODES.TIRAMISU
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi

internal fun Dataset.Builder.setFieldCompat(
    key: AutofillId,
    value: AutofillValue,
    remoteViews: RemoteViews? = null,
    inlinePresentation: InlinePresentation? = null,
): Dataset.Builder {
    when {
        SDK_INT >= TIRAMISU -> setFieldApi33(key, value, remoteViews, inlinePresentation)
        SDK_INT >= R -> setValueApi30(key, value, remoteViews, inlinePresentation)
        else -> setValueLegacy(key, value, remoteViews)
    }
    return this
}

@Suppress("DEPRECATION")
private fun Dataset.Builder.setValueLegacy(key: AutofillId, value: AutofillValue, remoteViews: RemoteViews?) = when {
    remoteViews != null -> setValue(key, value, remoteViews)
    else -> setValue(key, value)
}

@RequiresApi(R)
@Suppress("DEPRECATION")
private fun Dataset.Builder.setValueApi30(
    key: AutofillId,
    value: AutofillValue,
    remoteViews: RemoteViews?,
    inlinePresentation: InlinePresentation?,
) = when {
    inlinePresentation != null && remoteViews != null -> setValue(
        key,
        value,
        remoteViews,
        inlinePresentation,
    )
    remoteViews != null -> setValue(key, value, remoteViews)
    else -> setValue(key, value)
}

@RequiresApi(TIRAMISU)
private fun Dataset.Builder.setFieldApi33(
    key: AutofillId,
    value: AutofillValue,
    remoteViews: RemoteViews?,
    inlinePresentation: InlinePresentation?,
) = setField(
    key,
    Field
        .Builder()
        .apply {
            setValue(value)
            if (remoteViews == null && inlinePresentation == null) {
                null
            } else {
                Presentations
                    .Builder()
                    .apply {
                        remoteViews?.let {
                            
                            setMenuPresentation(it)
                            
                            
                            
                            
                            
                            setDialogPresentation(it)
                        }
                        inlinePresentation?.let { setInlinePresentation(it) }
                    }.build()
            }?.let {
                setPresentations(it)
            }
        }.build(),
)