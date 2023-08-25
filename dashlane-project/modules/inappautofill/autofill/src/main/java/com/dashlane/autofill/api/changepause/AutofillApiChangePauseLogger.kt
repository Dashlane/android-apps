package com.dashlane.autofill.api.changepause

import com.dashlane.autofill.api.pause.model.PauseDurations
import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface AutofillApiChangePauseLogger {
    fun resumeFormSource(autoFillFormSource: AutoFillFormSource)

    fun openPauseForFormSource(autoFillFormSource: AutoFillFormSource)

    fun pauseFormSource(autoFillFormSource: AutoFillFormSource, pauseDurations: PauseDurations)
}