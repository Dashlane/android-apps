package com.dashlane.autofill.api.pause.services

import com.dashlane.autofill.api.pause.model.PausedFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import java.time.Instant



interface PausedFormSourcesProvider : RemovePauseContract {
    suspend fun isPaused(autoFillFormSource: AutoFillFormSource): Boolean
    suspend fun pauseUntil(autoFillFormSource: AutoFillFormSource, untilInstant: Instant)
    override suspend fun removePause(autoFillFormSource: AutoFillFormSource)
    override suspend fun removeAllPauses()
    override suspend fun getAllPausedFormSources(): List<PausedFormSource>
    override suspend fun getPausedFormSource(autoFillFormSource: AutoFillFormSource): PausedFormSource?
}
