package com.dashlane.autofill.api.pause.model

import com.dashlane.autofill.api.pause.services.PausedFormSourcesProvider
import com.dashlane.autofill.api.pause.services.PausedFormSourcesStringsRepository
import com.dashlane.autofill.api.pause.AskPauseContract
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import javax.inject.Inject

class AskPauseDataProvider @Inject constructor(
    private val pausedFormSourcesProvider: PausedFormSourcesProvider,
    private val pausedFormSourcesStringsRepository: PausedFormSourcesStringsRepository
) : AskPauseContract.DataProvider {

    override suspend fun getPauseFormSourceTitle(autoFillFormSource: AutoFillFormSource, openInDashlane: Boolean): String {
        return pausedFormSourcesStringsRepository.getPauseFormSourceTitle(autoFillFormSource, openInDashlane)
    }

    override suspend fun pauseItem(
        autoFillFormSource: AutoFillFormSource,
        pauseDurations: PauseDurations
    ) {
        pausedFormSourcesProvider.pauseUntil(autoFillFormSource, pauseDurations.getInstantForDuration())
    }

    override suspend fun getPauseMessageForDuration(
        autoFillFormSource: AutoFillFormSource,
        pauseDurations: PauseDurations
    ): String {
        return pausedFormSourcesStringsRepository.getPauseForDurationMessage(autoFillFormSource, pauseDurations)
    }
}
