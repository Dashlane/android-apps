package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class VersionValidityStatus(override val code: String) : TrackingLog.Enum {
    EXPIRED_VERSION(
        code = "expired_version",
    ),
    UPDATE_RECOMMENDED(
        code = "update_recommended",
    ),
    UPDATE_REQUIRED(
        code = "update_required",
    ),
    UPDATE_STRONGLY_ENCOURAGED(
        code = "update_strongly_encouraged",
    ),
}