package com.dashlane.autofill.changepause.model

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import java.time.Instant

data class ChangePauseModel(
    val processing: Boolean = false,
    val autoFillFormSource: AutoFillFormSource,
    val pauseUntil: Instant? = null,
    val autoFillFormSourceTitle: String,
    val title: String,
    val subtitle: String
) {
    val isPaused = pauseUntil != null
}