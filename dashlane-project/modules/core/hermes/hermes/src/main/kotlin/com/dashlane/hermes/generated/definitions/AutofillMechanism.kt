package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AutofillMechanism(override val code: String) : TrackingLog.Enum {
    ANDROID_ACCESSIBILITY(
        code = "android_accessibility",
    ),
    ANDROID_AUTOFILL_API(
        code = "android_autofill_api",
    ),
    IOS_TACHYON(
        code = "ios_tachyon",
    ),
    WEB(
        code = "web",
    ),
}