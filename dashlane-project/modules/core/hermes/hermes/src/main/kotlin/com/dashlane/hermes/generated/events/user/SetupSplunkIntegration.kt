package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.SplunkSetupStep
import javax.annotation.processing.Generated

@Generated
data class SetupSplunkIntegration(
    val splunkSetupStep: SplunkSetupStep,
) : TrackingLog {
    override val name
        get() = "setup_splunk_integration"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["splunk_setup_step"] = splunkSetupStep
    }
}