package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SsoSolutionChosen(override val code: String) : TrackingLog.Enum {
    NITRO_SSO(
        code = "nitro_sso",
    ),
    SELF_HOSTED_SSO(
        code = "self_hosted_sso",
    ),
}