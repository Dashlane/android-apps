package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.UseKeyErrorName
import javax.annotation.processing.Generated

@Generated
data class UseAccountRecoveryKey(
    val flowStep: FlowStep,
    val useKeyErrorName: UseKeyErrorName? = null,
) : TrackingLog {
    override val name
        get() = "use_account_recovery_key"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["flow_step"] = flowStep
        collector["use_key_error_name"] = useKeyErrorName
    }
}