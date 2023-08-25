package com.dashlane.autofill.api.pause.view

import com.dashlane.autofill.api.pause.model.PauseDurations
import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface AskPauseDialogContract {
    fun getPausedFormSource(): AutoFillFormSource
    fun onPauseFormSourceDialogResponse(pauseDurations: PauseDurations?)
}
