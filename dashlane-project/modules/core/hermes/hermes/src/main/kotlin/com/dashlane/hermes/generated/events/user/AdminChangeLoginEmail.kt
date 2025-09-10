package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ChangeLoginEmailFlowStep
import com.dashlane.hermes.generated.definitions.ChangeLoginError
import javax.annotation.processing.Generated

@Generated
data class AdminChangeLoginEmail(
    val errorName: ChangeLoginError? = null,
    val changeLoginEmailFlowStep: ChangeLoginEmailFlowStep,
) : TrackingLog {
    override val name
        get() = "admin_change_login_email"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["error_name"] = errorName
        collector["change_login_email_flow_step"] = changeLoginEmailFlowStep
    }
}