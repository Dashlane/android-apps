package com.dashlane.autofill.pause.services

import com.dashlane.autofill.pause.model.PausedFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface RemovePauseContract {
    suspend fun removePause(autoFillFormSource: AutoFillFormSource)
    suspend fun removeAllPauses()
    suspend fun getAllPausedFormSources(): List<PausedFormSource>
    suspend fun getPausedFormSource(autoFillFormSource: AutoFillFormSource): PausedFormSource?
}