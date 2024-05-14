package com.dashlane.autofill.rememberaccount.view

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.hermes.generated.definitions.Space

interface AutofillLinkServiceLogger {
    fun logShowLinkPage()
    fun logLinkServiceAccepted(
        itemId: String,
        space: Space,
        itemUrl: String?,
        autoFillFormSource: AutoFillFormSource
    )

    fun logLinkServiceRefused()
    fun logLinkServiceCancel()
}