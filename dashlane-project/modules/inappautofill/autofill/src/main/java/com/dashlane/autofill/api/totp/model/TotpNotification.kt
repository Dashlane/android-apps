package com.dashlane.autofill.api.totp.model

import com.dashlane.xml.domain.SyncObject

class TotpNotification(
    val id: String,
    val credential: SyncObject.Authentifiant
) {
    var totalCodeUpdates: Int = 0
        private set
    var totalCodeCopies: Int = 0
        private set
    val hasCopies: Boolean
        get() = totalCodeCopies > 0
    var isCodeUpdatedCopied: Boolean = false
        private set

    fun totpCodeUpdated() {
        totalCodeUpdates = totalCodeUpdates.inc()
        isCodeUpdatedCopied = false
    }

    fun totpCodeCopied() {
        totalCodeCopies = totalCodeCopies.inc()
        isCodeUpdatedCopied = true
    }
}
