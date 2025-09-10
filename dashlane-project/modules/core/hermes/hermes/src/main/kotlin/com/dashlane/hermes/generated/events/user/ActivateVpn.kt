package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ActivateVpnError
import com.dashlane.hermes.generated.definitions.FlowStep
import javax.annotation.processing.Generated

@Generated
data class ActivateVpn(
    val errorName: ActivateVpnError? = null,
    val flowStep: FlowStep,
) : TrackingLog {
    override val name
        get() = "activate_vpn"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["error_name"] = errorName
        collector["flow_step"] = flowStep
    }
}