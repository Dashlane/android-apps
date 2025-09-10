package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.InvitationLinkClickOrigin
import com.dashlane.hermes.generated.definitions.SignupFlowStep
import javax.annotation.processing.Generated

@Generated
data class SignupToDashlane(
    val signupFlowStep: SignupFlowStep,
    val invitationLinkClickOrigin: InvitationLinkClickOrigin,
) : TrackingLog {
    override val name
        get() = "signup_to_dashlane"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["signup_flow_step"] = signupFlowStep
        collector["invitation_link_click_origin"] = invitationLinkClickOrigin
    }
}