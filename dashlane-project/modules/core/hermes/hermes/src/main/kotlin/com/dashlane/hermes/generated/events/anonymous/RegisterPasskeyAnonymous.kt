package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AlgorithmsSupported
import com.dashlane.hermes.generated.definitions.AuthenticatorAttachment
import com.dashlane.hermes.generated.definitions.AuthenticatorResidentKey
import com.dashlane.hermes.generated.definitions.AuthenticatorUserVerification
import com.dashlane.hermes.generated.definitions.CeremonyStatus
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.PasskeyRegistrationErrorType
import com.dashlane.hermes.generated.definitions.PasskeyType
import com.dashlane.hermes.generated.definitions.WebauthnExtensionId
import javax.annotation.processing.Generated

@Generated
data class RegisterPasskeyAnonymous(
    val algorithmsSupportedList: List<AlgorithmsSupported>? = null,
    val authenticatorResidentKey: AuthenticatorResidentKey? = null,
    val msToCompleteRegistration: Int? = null,
    val passkeyRegistrationStatus: CeremonyStatus,
    val webauthnExtensionSupportedList: List<WebauthnExtensionId>? = null,
    val authenticatorAttachment: AuthenticatorAttachment? = null,
    val isRegisteredWithDashlane: Boolean? = null,
    val passkeyRegistrationErrorType: PasskeyRegistrationErrorType? = null,
    val passkeyType: PasskeyType? = null,
    val domain: Domain,
    val authenticatorUserVerification: AuthenticatorUserVerification? = null,
    val msToCompleteRegistrationTimeout: Int? = null,
) : TrackingLog {
    override val name
        get() = "register_passkey"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["algorithms_supported_list"] = algorithmsSupportedList
        collector["authenticator_resident_key"] = authenticatorResidentKey
        collector["ms_to_complete_registration"] = msToCompleteRegistration
        collector["passkey_registration_status"] = passkeyRegistrationStatus
        collector["webauthn_extension_supported_list"] = webauthnExtensionSupportedList
        collector["authenticator_attachment"] = authenticatorAttachment
        collector["is_registered_with_dashlane"] = isRegisteredWithDashlane
        collector["passkey_registration_error_type"] = passkeyRegistrationErrorType
        collector["passkey_type"] = passkeyType
        collector["domain"] = domain
        collector["authenticator_user_verification"] = authenticatorUserVerification
        collector["ms_to_complete_registration_timeout"] = msToCompleteRegistrationTimeout
    }
}