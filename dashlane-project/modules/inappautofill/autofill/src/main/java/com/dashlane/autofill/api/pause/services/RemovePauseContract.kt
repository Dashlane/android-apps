package com.dashlane.autofill.api.pause.services

import com.dashlane.autofill.api.pause.model.PausedFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource



interface RemovePauseContract {
    suspend fun removePause(autoFillFormSource: AutoFillFormSource)
    suspend fun removeAllPauses()
    suspend fun getAllPausedFormSources(): List<PausedFormSource>
    suspend fun getPausedFormSource(autoFillFormSource: AutoFillFormSource): PausedFormSource?
}