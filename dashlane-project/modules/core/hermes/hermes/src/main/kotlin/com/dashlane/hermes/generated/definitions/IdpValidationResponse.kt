package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class IdpValidationResponse(override val code: String) : TrackingLog.Enum {
    IDP_ENTRYPOINT_NOT_FOUND(
        code = "idp_entrypoint_not_found",
    ),
    INVALID_ENTRYPOINT(
        code = "invalid_entrypoint",
    ),
    INVALID_IDP_SSO_DESCRIPTOR(
        code = "invalid_idp_sso_descriptor",
    ),
    KEY_DESCRIPTOR_NOT_FOUND(
        code = "key_descriptor_not_found",
    ),
    MISSING_CERTIFICATE(
        code = "missing_certificate",
    ),
    MULTIPLE_CERTIFICATES(
        code = "multiple_certificates",
    ),
    SUCCESS(
        code = "success",
    ),
    XML_PARSE_FAILED(
        code = "xml_parse_failed",
    ),
}