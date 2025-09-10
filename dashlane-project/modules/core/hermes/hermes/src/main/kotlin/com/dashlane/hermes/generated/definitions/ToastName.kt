package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ToastName(override val code: String) : TrackingLog.Enum {
    INSUFFICIENT_SEATS_PROVISIONING_ERROR(
        code = "insufficient_seats_provisioning_error",
    ),
    NO_MORE_SEATS_AVAILABLE_TO_PROVISION(
        code = "no_more_seats_available_to_provision",
    ),
}