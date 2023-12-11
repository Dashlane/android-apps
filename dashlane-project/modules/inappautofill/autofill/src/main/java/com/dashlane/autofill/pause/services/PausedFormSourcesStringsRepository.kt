package com.dashlane.autofill.pause.services

import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface PausedFormSourcesStringsRepository {
    suspend fun getFormSourceName(autoFillFormSource: AutoFillFormSource): String
    suspend fun getFormSourceTypeName(autoFillFormSource: AutoFillFormSource): String
    suspend fun getPauseFormSourceTitle(autoFillFormSource: AutoFillFormSource, showDashlane: Boolean = false): String
    suspend fun getPauseForDurationMessage(autoFillFormSource: AutoFillFormSource, pauseDurations: PauseDurations): String
}
