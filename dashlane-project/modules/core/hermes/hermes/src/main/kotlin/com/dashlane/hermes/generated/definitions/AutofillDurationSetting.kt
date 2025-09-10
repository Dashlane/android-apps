package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class AutofillDurationSetting(override val code: String) : TrackingLog.Enum {
    ONCE_FOR_THIS_VISIT(
        code = "once_for_this_visit",
    ),
    UNTIL_TURNED_BACK_OFF(
        code = "until_turned_back_off",
    ),
    UNTIL_TURNED_BACK_ON(
        code = "until_turned_back_on",
    ),
}