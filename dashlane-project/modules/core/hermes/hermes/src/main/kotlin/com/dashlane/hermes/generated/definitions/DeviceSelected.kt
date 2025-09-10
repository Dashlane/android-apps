package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class DeviceSelected(override val code: String) : TrackingLog.Enum {
    ANY(
        code = "any",
    ),
    COMPUTER(
        code = "computer",
    ),
    MOBILE(
        code = "mobile",
    ),
    NO_DEVICE_AVAILABLE(
        code = "no_device_available",
    ),
}