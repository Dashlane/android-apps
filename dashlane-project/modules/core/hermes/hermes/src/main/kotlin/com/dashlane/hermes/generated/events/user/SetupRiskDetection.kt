package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.RiskDetectionSetupStep
import javax.annotation.processing.Generated

@Generated
data class SetupRiskDetection(
    val riskDetectionSetupStep: RiskDetectionSetupStep,
) : TrackingLog {
    override val name
        get() = "setup_risk_detection"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["risk_detection_setup_step"] = riskDetectionSetupStep
    }
}