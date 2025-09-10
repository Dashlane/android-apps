package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class TestSsoResponse(override val code: String) : TrackingLog.Enum {
    FAILURE(
        code = "failure",
    ),
    NOT_TESTED(
        code = "not_tested",
    ),
    SUCCESS(
        code = "success",
    ),
    TIMEOUT(
        code = "timeout",
    ),
}