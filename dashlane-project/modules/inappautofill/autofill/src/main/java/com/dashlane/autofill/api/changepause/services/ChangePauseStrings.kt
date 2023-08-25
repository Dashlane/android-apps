package com.dashlane.autofill.api.changepause.services

import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface ChangePauseStrings {
    fun getAutofillFromSourceTitle(autoFillFormSource: AutoFillFormSource): String
    fun getPauseTitle(autoFillFormSource: AutoFillFormSource): String
    fun getPausePermanentMessage(autoFillFormSource: AutoFillFormSource): String
    fun getPauseForHoursMessage(autoFillFormSource: AutoFillFormSource, hours: Int): String
    fun getPauseForMinutesMessage(autoFillFormSource: AutoFillFormSource, minutes: Int): String
    fun getNotPausedMessage(autoFillFormSource: AutoFillFormSource): String
}
