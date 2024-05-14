package com.dashlane.autofill.fillresponse

import android.content.IntentSender
import android.service.autofill.InlinePresentation
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews

class DatasetWrapperBuilder private constructor(
    private var removeViews: RemoteViews? = null,
    private var authenticationIntentSender: IntentSender?,
    private val autofillIdsValues: MutableMap<AutofillId, AutofillValue>,
    private var inlinePresentation: InlinePresentation?
) : Cloneable {

    constructor(removeViews: RemoteViews?, inlinePresentation: InlinePresentation?) :
            this(removeViews, null, mutableMapOf(), inlinePresentation)

    fun setValue(id: AutofillId?, value: AutofillValue) {
        id?.let {
            autofillIdsValues[id] = value
        }
    }

    fun setAuthentication(intentSender: IntentSender) {
        authenticationIntentSender = intentSender
    }

    fun limitAutofillIdTo(autofillIdsToKeep: List<AutofillId>) {
        autofillIdsValues.keysToKeep {
            autofillIdsToKeep.contains(it)
        }
    }

    fun build() = if (autofillIdsValues.isNotEmpty()) {
        DatasetWrapper(
            removeViews,
            authenticationIntentSender,
            autofillIdsValues.toMutableMap(),
            inlinePresentation
        )
    } else {
        null
    }

    public override fun clone(): DatasetWrapperBuilder {
        return DatasetWrapperBuilder(removeViews, authenticationIntentSender, autofillIdsValues, inlinePresentation)
    }

    private fun MutableMap<AutofillId, AutofillValue>.keysToKeep(predicate: (AutofillId) -> Boolean) {
        filter {
            !predicate(it.key)
        }.map {
            it.key
        }.forEach {
            remove(it)
        }
    }
}