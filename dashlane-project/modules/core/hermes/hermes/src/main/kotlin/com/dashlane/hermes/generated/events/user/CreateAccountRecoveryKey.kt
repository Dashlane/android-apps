package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CreateKeyErrorName
import com.dashlane.hermes.generated.definitions.FlowStep
import javax.annotation.processing.Generated

@Generated
data class CreateAccountRecoveryKey(
    val createKeyErrorName: CreateKeyErrorName? = null,
    val flowStep: FlowStep,
) : TrackingLog {
    override val name
        get() = "create_account_recovery_key"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["create_key_error_name"] = createKeyErrorName
        collector["flow_step"] = flowStep
    }
}