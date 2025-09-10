package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class BuildType(override val code: String) : TrackingLog.Enum {
    ALPHA(
        code = "alpha",
    ),
    BETA(
        code = "beta",
    ),
    DEV(
        code = "dev",
    ),
    NIGHTLY(
        code = "nightly",
    ),
    PRODUCTION(
        code = "production",
    ),
    QA(
        code = "qa",
    ),
}