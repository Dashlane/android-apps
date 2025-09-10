package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ChangeMasterPasswordError
import com.dashlane.hermes.generated.definitions.FlowStep
import javax.annotation.processing.Generated

@Generated
data class ChangeMasterPassword(
    val isWeak: Boolean? = null,
    val errorName: ChangeMasterPasswordError? = null,
    val flowStep: FlowStep,
    val isLeaked: Boolean? = null,
) : TrackingLog {
    override val name
        get() = "change_master_password"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_weak"] = isWeak
        collector["error_name"] = errorName
        collector["flow_step"] = flowStep
        collector["is_leaked"] = isLeaked
    }
}