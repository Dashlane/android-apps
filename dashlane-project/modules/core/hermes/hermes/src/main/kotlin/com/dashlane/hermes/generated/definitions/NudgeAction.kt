package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class NudgeAction(override val code: String) : TrackingLog.Enum {
    COMPLETED_SETUP(
        code = "completed_setup",
    ),
    SEE_IN_CONTEXT_NUDGE_EXAMPLE(
        code = "see_in_context_nudge_example",
    ),
    SENT_TEST_NUDGE(
        code = "sent_test_nudge",
    ),
    STARTED_SETUP(
        code = "started_setup",
    ),
    UNINSTALLED_INTEGRATION(
        code = "uninstalled_integration",
    ),
}