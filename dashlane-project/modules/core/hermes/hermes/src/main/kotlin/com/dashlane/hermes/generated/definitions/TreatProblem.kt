package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class TreatProblem(override val code: String) : TrackingLog.Enum {
    DOWNLOAD(
        code = "download",
    ),
    NOT_NEEDED(
        code = "not_needed",
    ),
    UPLOAD(
        code = "upload",
    ),
    UPLOAD_AND_DOWNLOAD(
        code = "upload_and_download",
    ),
}