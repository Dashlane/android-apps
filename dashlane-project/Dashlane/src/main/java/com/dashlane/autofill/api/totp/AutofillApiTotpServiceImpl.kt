package com.dashlane.autofill.api.totp

import com.dashlane.authenticator.Totp
import com.dashlane.autofill.api.totp.model.TotpResult
import com.dashlane.autofill.api.totp.services.AutofillApiTotpService
import javax.inject.Inject

internal class AutofillApiTotpServiceImpl @Inject constructor() : AutofillApiTotpService {

    override fun getTotp(totp: Totp): TotpResult? {
        return totp.getPin()?.let { TotpResult(it.code, it.timeRemaining.toMillis()) }
    }
}
