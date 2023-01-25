package com.dashlane.autofill.api.pause.services

import android.content.Context
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.api.pause.model.PauseDurations
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject



class PausedFormSourcesStrings @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val autofillFormSourcesStrings: AutofillFormSourcesStrings
) : PausedFormSourcesStringsRepository {

    override suspend fun getFormSourceName(autoFillFormSource: AutoFillFormSource): String {
        return autofillFormSourcesStrings.getAutoFillFormSourceString(autoFillFormSource)
    }

    override suspend fun getFormSourceTypeName(autoFillFormSource: AutoFillFormSource): String {
        return autofillFormSourcesStrings.getAutoFillFormSourceTypeString(autoFillFormSource)
    }

    override suspend fun getPauseFormSourceTitle(
        autoFillFormSource: AutoFillFormSource,
        showDashlane: Boolean
    ): String {
        val autofillFormSourceTitle = autofillFormSourcesStrings.getAutoFillFormSourceString(autoFillFormSource)

        return if (showDashlane) {
            context.getString(
                R.string.autofill_do_not_show_again_choose_pause_inside_dashlane_title,
                autofillFormSourceTitle
            )
        } else {
            context.getString(R.string.autofill_do_not_show_again_choose_pause_title, autofillFormSourceTitle)
        }
    }

    override suspend fun getPauseForDurationMessage(
        autoFillFormSource: AutoFillFormSource,
        pauseDurations: PauseDurations
    ): String {
        return when (autoFillFormSource) {
            is ApplicationFormSource -> getApplicationPauseForDurationMessage(pauseDurations)
            is WebDomainFormSource -> getWebsitePauseForDurationMessage(pauseDurations)
        }
    }

    private fun getApplicationPauseForDurationMessage(pauseDurations: PauseDurations): String {
        return when (pauseDurations) {
            PauseDurations.ONE_HOUR ->
                context.getString(R.string.autofill_do_not_show_again_paused_for_one_hour_application_message)
            PauseDurations.ONE_DAY ->
                context.getString(R.string.autofill_do_not_show_again_paused_for_one_day_application_message)
            PauseDurations.PERMANENT ->
                context.getString(R.string.autofill_do_not_show_again_paused_permanent_application_message)
        }
    }

    private fun getWebsitePauseForDurationMessage(pauseDurations: PauseDurations): String {
        return when (pauseDurations) {
            PauseDurations.ONE_HOUR ->
                context.getString(R.string.autofill_do_not_show_again_paused_for_one_hour_website_message)
            PauseDurations.ONE_DAY ->
                context.getString(R.string.autofill_do_not_show_again_paused_for_one_day_website_message)
            PauseDurations.PERMANENT ->
                context.getString(R.string.autofill_do_not_show_again_paused_permanent_website_message)
        }
    }
}
