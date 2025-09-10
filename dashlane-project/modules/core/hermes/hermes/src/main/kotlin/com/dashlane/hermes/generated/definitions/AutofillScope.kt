package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AutofillScope(override val code: String) : TrackingLog.Enum {
    FIELD(
        code = "field",
    ),
    GLOBAL(
        code = "global",
    ),
    PAGE(
        code = "page",
    ),
    SITE(
        code = "site",
    ),
}