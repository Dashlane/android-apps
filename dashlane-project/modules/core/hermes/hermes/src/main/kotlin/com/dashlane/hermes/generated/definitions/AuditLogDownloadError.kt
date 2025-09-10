package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AuditLogDownloadError(override val code: String) : TrackingLog.Enum {
    NO_CSV(
        code = "no_csv",
    ),
    NO_LOG_DATA(
        code = "no_log_data",
    ),
    UNEXPECTED_UNKNOWN(
        code = "unexpected_unknown",
    ),
}