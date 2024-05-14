package com.dashlane.autofill.totp

import com.dashlane.authenticator.Totp
import com.dashlane.authenticator.otp
import com.dashlane.autofill.api.R
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.xml.domain.SyncObject
import java.time.Clock
import javax.inject.Inject

interface AutofillTotpCopyService {
    fun copyTotpToClipboard(credential: SyncObject.Authentifiant)
}

class AutofillTotpCopyServiceImpl @Inject constructor(
    private val clipboardCopy: ClipboardCopy,
    private val clock: Clock
) : AutofillTotpCopyService {
    override fun copyTotpToClipboard(credential: SyncObject.Authentifiant) {
        val totpResult = credential.otp()?.let { it as? Totp }?.toTotpResult() ?: return

        clipboardCopy.copyToClipboard(
            data = totpResult.code,
            sensitiveData = true,
            autoClear = true,
            feedback = R.string.autofill_totp_automatically_copy
        )
    }

    private fun Totp.toTotpResult(): TotpResult? {
        return getPin(clock.millis())?.let { TotpResult(it.code, it.timeRemaining.toMillis()) }
    }
}
