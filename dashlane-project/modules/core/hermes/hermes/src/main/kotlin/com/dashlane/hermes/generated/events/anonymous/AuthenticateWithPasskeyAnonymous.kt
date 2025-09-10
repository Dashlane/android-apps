package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AuthenticationMediationType
import com.dashlane.hermes.generated.definitions.AuthenticatorUserVerification
import com.dashlane.hermes.generated.definitions.CeremonyStatus
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.PasskeyAuthenticationErrorType
import com.dashlane.hermes.generated.definitions.PasskeyType
import com.dashlane.hermes.generated.definitions.WebauthnExtensionId
import javax.annotation.processing.Generated

@Generated
data class AuthenticateWithPasskeyAnonymous(
    val passkeyType: PasskeyType? = null,
    val msToCompleteAuthentication: Int? = null,
    val passkeyAuthenticationErrorType: PasskeyAuthenticationErrorType? = null,
    val domain: Domain,
    val authenticatorUserVerification: AuthenticatorUserVerification? = null,
    val hasCredentialsAllowed: Boolean? = null,
    val authenticationMediationType: AuthenticationMediationType? = null,
    val passkeyAuthenticationStatus: CeremonyStatus,
    val msToCompleteAuthenticationTimeout: Int? = null,
    val webauthnExtensionSupportedList: List<WebauthnExtensionId>? = null,
    val isAuthenticatedWithDashlane: Boolean? = null,
) : TrackingLog {
    override val name
        get() = "authenticate_with_passkey"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["passkey_type"] = passkeyType
        collector["ms_to_complete_authentication"] = msToCompleteAuthentication
        collector["passkey_authentication_error_type"] = passkeyAuthenticationErrorType
        collector["domain"] = domain
        collector["authenticator_user_verification"] = authenticatorUserVerification
        collector["has_credentials_allowed"] = hasCredentialsAllowed
        collector["authentication_mediation_type"] = authenticationMediationType
        collector["passkey_authentication_status"] = passkeyAuthenticationStatus
        collector["ms_to_complete_authentication_timeout"] = msToCompleteAuthenticationTimeout
        collector["webauthn_extension_supported_list"] = webauthnExtensionSupportedList
        collector["is_authenticated_with_dashlane"] = isAuthenticatedWithDashlane
    }
}