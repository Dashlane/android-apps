package com.dashlane.autofill.api.totp.services

import com.dashlane.authenticator.Totp
import com.dashlane.autofill.api.totp.model.TotpResult



interface AutofillApiTotpService {
    fun getTotp(totp: Totp): TotpResult?
}
