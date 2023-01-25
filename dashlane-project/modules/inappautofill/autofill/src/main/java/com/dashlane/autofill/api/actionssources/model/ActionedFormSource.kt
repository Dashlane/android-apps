package com.dashlane.autofill.api.actionssources.model

import com.dashlane.autofill.formdetector.model.AutoFillFormSource



data class ActionedFormSource(
    val autoFillFormSource: AutoFillFormSource,
    val title: String,
    val type: String,
    val icon: ActionedFormSourceIcon = ActionedFormSourceIcon.IncorrectSignatureIcon
)
