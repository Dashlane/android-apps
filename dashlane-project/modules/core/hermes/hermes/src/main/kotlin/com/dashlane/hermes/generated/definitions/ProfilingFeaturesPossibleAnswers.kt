package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ProfilingFeaturesPossibleAnswers(override val code: String) : TrackingLog.Enum {
    JUST_IN_TIME_PROVISIONING(
        code = "just_in_time_provisioning",
    ),
    MASS_DEPLOYMENT(
        code = "mass_deployment",
    ),
    NOT_SURE(
        code = "not_sure",
    ),
    SCIM(
        code = "scim",
    ),
    SIEM(
        code = "siem",
    ),
    SSO(
        code = "sso",
    ),
}