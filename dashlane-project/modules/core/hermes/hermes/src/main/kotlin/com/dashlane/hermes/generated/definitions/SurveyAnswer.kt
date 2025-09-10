package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SurveyAnswer(override val code: String) : TrackingLog.Enum {
    AUTOFILL_DIDNT_WORK_AS_EXPECTED(
        code = "autofill_didnt_work_as_expected",
    ),
    DASHLANE_DOESNT_HAVE_THE_FEATURES_I_NEED(
        code = "dashlane_doesnt_have_the_features_i_need",
    ),
    DASHLANE_IS_TOO_EXPENSIVE(
        code = "dashlane_is_too_expensive",
    ),
    THERE_WERE_TOO_MANY_TECHNICAL_ISSUES(
        code = "there_were_too_many_technical_issues",
    ),
}