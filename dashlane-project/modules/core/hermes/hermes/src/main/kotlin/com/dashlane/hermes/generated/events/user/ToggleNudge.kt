package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.IntegrationPlatform
import com.dashlane.hermes.generated.definitions.NudgeType
import com.dashlane.hermes.generated.definitions.State
import javax.annotation.processing.Generated

@Generated
data class ToggleNudge(
    val integrationPlatform: IntegrationPlatform,
    val nudgeType: NudgeType,
    val nudgeState: State,
) : TrackingLog {
    override val name
        get() = "toggle_nudge"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["integration_platform"] = integrationPlatform
        collector["nudge_type"] = nudgeType
        collector["nudge_state"] = nudgeState
    }
}