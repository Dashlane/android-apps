package com.dashlane.autofill.pause.model

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import java.time.Instant

data class PausedFormSource(
    val autoFillFormSource: AutoFillFormSource,
    val pauseUntil: Instant
)