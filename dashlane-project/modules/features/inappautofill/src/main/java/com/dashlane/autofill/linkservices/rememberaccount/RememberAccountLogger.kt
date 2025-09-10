package com.dashlane.autofill.linkservices.rememberaccount

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.hermes.generated.definitions.Space

interface RememberAccountLogger {
    fun logShowLinkPage()

    fun logLinkServiceAccepted(
        itemId: String,
        space: Space,
        itemUrl: String?,
        autoFillFormSource: AutoFillFormSource,
    )

    fun logLinkServiceRefused()

    fun logLinkServiceCancel()
}