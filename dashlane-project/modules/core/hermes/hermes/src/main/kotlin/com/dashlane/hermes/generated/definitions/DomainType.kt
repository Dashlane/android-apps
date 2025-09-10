package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class DomainType(override val code: String) : TrackingLog.Enum {
    APP(
        code = "app",
    ),
    WEB(
        code = "web",
    ),
}