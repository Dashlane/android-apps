package com.dashlane.autofill.actionssources.model

import android.content.Context
import com.dashlane.autofill.actionssources.view.PausedItemData
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.autofill.pause.presenter.AutofillFormSourceProvider
import com.dashlane.autofill.pause.services.RemovePauseContract
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Locale
import javax.inject.Inject

class ActionsSourcesRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val removePauseContract: RemovePauseContract,
    private val autofillFormSourceProvider: AutofillFormSourceProvider,
    private val clock: Clock,
) {
    suspend fun loadFormSources(): Result<Pair<List<PausedItemData>, List<PausedItemData>>> =
        runCatching {
            getAllFormSource()
        }

    private suspend fun getAllFormSource(): Pair<List<PausedItemData>, List<PausedItemData>> {
        val pausedFormSources = removePauseContract.getAllPausedFormSources()
        val allItemsWithPausedUntil = pausedFormSources.map { pausedFormSource ->
            val form = pausedFormSource.autoFillFormSource
            PausedItemData(
                form = form,
                icon = autofillFormSourceProvider.getIcon(form),
                title = autofillFormSourceProvider.getServiceName(form),
                description = getDescription(pausedFormSource.pauseUntil),
            ) to pausedFormSource.pauseUntil
        }.filterNot {
            it.first.icon is AutofillFormSourceIcon.NotInstalledApplication
        }

        
        val (temporary, permanent) = allItemsWithPausedUntil.partition { it.second != Instant.MAX }
        
        val temporarilyPausedItems = temporary
            .sortedBy { it.second }
            .map { it.first }
        
        val permanentlyPausedItems = permanent
            .map { it.first }
            .sortedBy { it.title.lowercase(Locale.getDefault()) }
        return temporarilyPausedItems to permanentlyPausedItems
    }

    private fun getDescription(pauseUntil: Instant): String? {
        if (pauseUntil == Instant.MAX) {
            return null
        }

        val duration: Duration = Duration.between(Instant.now(clock), pauseUntil)
        val hour = Duration.ofHours(1L)

        return if (duration <= hour) {
            val minutes = duration.toMinutes().toInt()
            context.resources.getQuantityString(
                R.plurals.paused_autofill_setting_paused_until_by_minutes,
                minutes,
                minutes,
            )
        } else {
            val hours = duration.toHours().toInt()
            context.resources.getQuantityString(
                R.plurals.paused_autofill_setting_paused_until_by_hours,
                hours,
                hours,
            )
        }
    }

    fun getResumedMessageFor(form: AutoFillFormSource): String =
        context.getString(
            R.string.paused_autofill_setting_resumed_success_feedback,
            autofillFormSourceProvider.getServiceName(form),
        )
}