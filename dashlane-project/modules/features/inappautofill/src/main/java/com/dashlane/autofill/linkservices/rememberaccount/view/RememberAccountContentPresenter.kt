package com.dashlane.autofill.linkservices.rememberaccount.view

import android.content.Context
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RememberAccountContentPresenter @Inject constructor(
    @ApplicationContext val context: Context,
) {
    fun getTitle(formSource: AutoFillFormSource): String = when (formSource) {
        is ApplicationFormSource -> context.getString(R.string.autofill_link_app_title)
        is WebDomainFormSource -> context.getString(R.string.autofill_link_website_title)
    }

    fun getDescription(
        formSource: AutoFillFormSource,
        itemName: String,
        linkServiceName: String,
    ): String = when (formSource) {
        is ApplicationFormSource -> context.getString(
            R.string.autofill_link_app_description,
            itemName,
            linkServiceName,
        )
        is WebDomainFormSource -> context.getString(R.string.autofill_link_website_description)
    }

    fun getServiceBadge(formSource: AutoFillFormSource): String = when (formSource) {
        is ApplicationFormSource -> context.getString(R.string.autofill_link_app)
        is WebDomainFormSource -> context.getString(R.string.autofill_link_website)
    }

    fun getPrimaryCtaText(formSource: AutoFillFormSource): String = when (formSource) {
        is ApplicationFormSource -> context.getString(R.string.autofill_link_app)
        is WebDomainFormSource -> context.getString(R.string.autofill_link_website)
    }
}