package com.dashlane.autofill.api.pause.services

import com.dashlane.autofill.api.pause.model.PauseDurations
import com.dashlane.autofill.formdetector.model.AutoFillFormSource



interface PausedFormSourcesStringsRepository {
    suspend fun getFormSourceName(autoFillFormSource: AutoFillFormSource): String
    suspend fun getFormSourceTypeName(autoFillFormSource: AutoFillFormSource): String
    suspend fun getPauseFormSourceTitle(autoFillFormSource: AutoFillFormSource, showDashlane: Boolean = false): String
    suspend fun getPauseForDurationMessage(autoFillFormSource: AutoFillFormSource, pauseDurations: PauseDurations): String
}
