package com.dashlane.autofill.pause.view

import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface AskPauseDialogContract {
    fun getPausedFormSource(): AutoFillFormSource
    fun onPauseFormSourceDialogResponse(pauseDurations: PauseDurations?)
}
