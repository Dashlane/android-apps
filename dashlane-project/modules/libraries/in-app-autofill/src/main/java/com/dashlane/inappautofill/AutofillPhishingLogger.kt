package com.dashlane.inappautofill

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.hermes.generated.definitions.PhishingRisk

interface AutofillPhishingLogger {
    fun logSettingChanged(isEnabled: Boolean)

    fun onAcceptAutoFillRisk(
        autoFillFormSource: AutoFillFormSource,
        phishingRisk: PhishingRisk,
    )

    fun onDismissAutoFill(autoFillFormSource: AutoFillFormSource, trust: Boolean)
}