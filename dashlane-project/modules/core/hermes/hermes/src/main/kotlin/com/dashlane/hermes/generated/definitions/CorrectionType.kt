package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class CorrectionType(override val code: String) : TrackingLog.Enum {
    CHANGE_CLASSIFICATION(
        code = "change_classification",
    ),
    EDIT(
        code = "edit",
    ),
    ERASE(
        code = "erase",
    ),
    OVERWRITE(
        code = "overwrite",
    ),
}