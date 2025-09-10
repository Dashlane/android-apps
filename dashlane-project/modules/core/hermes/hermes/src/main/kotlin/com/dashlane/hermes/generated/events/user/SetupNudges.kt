package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.IntegrationPlatform
import com.dashlane.hermes.generated.definitions.NudgeAction
import javax.annotation.processing.Generated

@Generated
data class SetupNudges(
    val integrationPlatform: IntegrationPlatform,
    val action: NudgeAction,
) : TrackingLog {
    override val name
        get() = "setup_nudges"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["integration_platform"] = integrationPlatform
        collector["action"] = action
    }
}