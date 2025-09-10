package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class WebcardSaveOptions(override val code: String) : TrackingLog.Enum {
    REPLACE(
        code = "replace",
    ),
    SAVE(
        code = "save",
    ),
    SAVE_AS_NEW(
        code = "save_as_new",
    ),
    TRUST_AND_AUTOFILL(
        code = "trust_and_autofill",
    ),
    TRUST_AND_PASTE(
        code = "trust_and_paste",
    ),
}