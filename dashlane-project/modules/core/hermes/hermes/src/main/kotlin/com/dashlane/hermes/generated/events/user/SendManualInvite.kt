package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import javax.annotation.processing.Generated

@Generated
data class SendManualInvite(
    val isImport: Boolean,
    val flowStep: FlowStep,
    val importSize: Int? = null,
    val initialSeatCount: Int? = null,
    val inviteFailedCount: Int,
    val isResend: Boolean,
    val inviteCount: Int,
    val inviteResentCount: Int,
    val inviteSuccessfulCount: Int,
    val seatAddedCount: Int? = null,
) : TrackingLog {
    override val name
        get() = "send_manual_invite"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_import"] = isImport
        collector["flow_step"] = flowStep
        collector["import_size"] = importSize
        collector["initial_seat_count"] = initialSeatCount
        collector["invite_failed_count"] = inviteFailedCount
        collector["is_resend"] = isResend
        collector["invite_count"] = inviteCount
        collector["invite_resent_count"] = inviteResentCount
        collector["invite_successful_count"] = inviteSuccessfulCount
        collector["seat_added_count"] = seatAddedCount
    }
}