package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import java.util.UUID
import javax.annotation.processing.Generated

@Generated
data class MassDeploymentStatusAnonymous(
    val massDeploymentAccessKey: String,
    val isProtectedByCredentialRiskDetection: Boolean,
    val massDeploymentDeviceId: UUID,
) : TrackingLog {
    override val name
        get() = "mass_deployment_status"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["mass_deployment_access_key"] = massDeploymentAccessKey
        collector["is_protected_by_credential_risk_detection"] = isProtectedByCredentialRiskDetection
        collector["mass_deployment_device_id"] = massDeploymentDeviceId
    }
}