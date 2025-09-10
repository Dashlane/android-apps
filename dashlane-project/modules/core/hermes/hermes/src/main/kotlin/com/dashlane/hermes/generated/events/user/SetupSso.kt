package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.B2bPlanTier
import com.dashlane.hermes.generated.definitions.EmailDomainError
import com.dashlane.hermes.generated.definitions.EncryptionServicePlatformSelected
import com.dashlane.hermes.generated.definitions.IdpValidationResponse
import com.dashlane.hermes.generated.definitions.SsoSetupStep
import com.dashlane.hermes.generated.definitions.SsoSolutionChosen
import com.dashlane.hermes.generated.definitions.TestSsoResponse
import javax.annotation.processing.Generated

@Generated
data class SetupSso(
    val ssoSolutionChosen: SsoSolutionChosen,
    val emailDomainError: EmailDomainError,
    val emailDomainValidatedCount: Int,
    val currentBillingPlanTier: B2bPlanTier? = null,
    val idpValidationResponse: IdpValidationResponse,
    val emailDomainSubmittedCount: Int,
    val testSsoResponse: TestSsoResponse,
    val encryptionServicePlatformSelected: EncryptionServicePlatformSelected,
    val ssoSetupStep: SsoSetupStep,
) : TrackingLog {
    override val name
        get() = "setup_sso"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["sso_solution_chosen"] = ssoSolutionChosen
        collector["email_domain_error"] = emailDomainError
        collector["email_domain_validated_count"] = emailDomainValidatedCount
        collector["current_billing_plan_tier"] = currentBillingPlanTier
        collector["idp_validation_response"] = idpValidationResponse
        collector["email_domain_submitted_count"] = emailDomainSubmittedCount
        collector["test_sso_response"] = testSsoResponse
        collector["encryption_service_platform_selected"] = encryptionServicePlatformSelected
        collector["sso_setup_step"] = ssoSetupStep
    }
}