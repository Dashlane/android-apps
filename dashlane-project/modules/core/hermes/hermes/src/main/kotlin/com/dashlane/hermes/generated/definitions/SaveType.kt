package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SaveType(override val code: String) : TrackingLog.Enum {
    REPLACE(
        code = "replace",
    ),
    SAVE(
        code = "save",
    ),
    SAVE_AS_NEW(
        code = "save_as_new",
    ),
}