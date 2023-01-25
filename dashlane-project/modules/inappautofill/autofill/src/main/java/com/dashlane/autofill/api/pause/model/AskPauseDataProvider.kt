package com.dashlane.autofill.api.pause.model

import com.dashlane.autofill.api.pause.services.PausedFormSourcesProvider
import com.dashlane.autofill.api.pause.services.PausedFormSourcesStringsRepository
import com.dashlane.autofill.api.pause.AskPauseContract
import com.dashlane.autofill.formdetector.model.AutoFillFormSource



class AskPauseDataProvider(
    private val pausedFormSourcesProvider: PausedFormSourcesProvider,
    private val pausedFormSourcesStringsRepository: PausedFormSourcesStringsRepository,
    private val openInDashlane: Boolean
) : AskPauseContract.DataProvider {

    override suspend fun getPauseFormSourceTitle(autoFillFormSource: AutoFillFormSource): String {
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
