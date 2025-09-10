package com.dashlane.autofill.pause

import android.content.Context
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.autofill.pause.presenter.AutofillFormSourceProvider
import com.dashlane.autofill.pause.services.PausedFormSourcesProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AskPauseRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val autofillFormSourceProvider: AutofillFormSourceProvider,
    private val pausedFormSourcesProvider: PausedFormSourcesProvider,
) {
    fun getTitle(): String = context.getString(R.string.autofill_ask_pause_title)

    fun getIcon(autoFillFormSource: AutoFillFormSource): AutofillFormSourceIcon =
        autofillFormSourceProvider.getIcon(autoFillFormSource)

    fun getServiceName(autoFillFormSource: AutoFillFormSource): String =
        autofillFormSourceProvider.getServiceName(autoFillFormSource)

    suspend fun pauseItem(
        autoFillFormSource: AutoFillFormSource,
        pauseDurations: PauseDurations,
    ) {
        pausedFormSourcesProvider.pauseUntil(autoFillFormSource, pauseDurations.getInstantForDuration())
    }

    fun getPauseMessageForDuration(
        autoFillFormSource: AutoFillFormSource,
        pauseDurations: PauseDurations,
    ): String = when (autoFillFormSource) {
        is ApplicationFormSource -> getApplicationPauseForDurationMessage(pauseDurations)
        is WebDomainFormSource -> getWebsitePauseForDurationMessage(pauseDurations)
    }

    private fun getApplicationPauseForDurationMessage(pauseDurations: PauseDurations): String =
        when (pauseDurations) {
            PauseDurations.ONE_HOUR ->
                context.getString(R.string.autofill_do_not_show_again_paused_for_one_hour_application_message)
            PauseDurations.ONE_DAY ->
                context.getString(R.string.autofill_do_not_show_again_paused_for_one_day_application_message)
            PauseDurations.PERMANENT ->
                context.getString(R.string.autofill_do_not_show_again_paused_permanent_application_message)
        }

    private fun getWebsitePauseForDurationMessage(pauseDurations: PauseDurations): String =
        when (pauseDurations) {
            PauseDurations.ONE_HOUR ->
                context.getString(R.string.autofill_do_not_show_again_paused_for_one_hour_website_message)
            PauseDurations.ONE_DAY ->
                context.getString(R.string.autofill_do_not_show_again_paused_for_one_day_website_message)
            PauseDurations.PERMANENT ->
                context.getString(R.string.autofill_do_not_show_again_paused_permanent_website_message)
        }

    fun getPauseMessageForError(): String =
        context.getString(R.string.autofill_do_not_show_again_pause_error_message)
}