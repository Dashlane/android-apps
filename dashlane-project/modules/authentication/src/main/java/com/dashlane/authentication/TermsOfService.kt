package com.dashlane.authentication

import com.dashlane.server.api.endpoints.account.Consent

data class TermsOfService(
    val conditions: Boolean?,
    val offers: Boolean?
)

internal fun TermsOfService.toConsentsList() = listOfNotNull(
    conditions?.toConsent(Consent.ConsentType.PRIVACYPOLICYANDTOS),
    offers?.toConsent(Consent.ConsentType.EMAILSOFFERSANDTIPS)
)

private fun Boolean.toConsent(type: Consent.ConsentType) = Consent(
    consentType = type,
    status = this
)