package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class DomainVerificationStep(override val code: String) : TrackingLog.Enum {
    DOMAIN_VERIFICATION_COMPLETED(
        code = "domain_verification_completed",
    ),
    DOMAIN_VERIFICATION_ERROR(
        code = "domain_verification_error",
    ),
    TAP_CONTINUE_CTA_WITH_DOMAIN_URL(
        code = "tap_continue_cta_with_domain_url",
    ),
    TAP_VERIFY_DOMAIN_CTA_WITH_DNS_INFORMATION(
        code = "tap_verify_domain_cta_with_dns_information",
    ),
}