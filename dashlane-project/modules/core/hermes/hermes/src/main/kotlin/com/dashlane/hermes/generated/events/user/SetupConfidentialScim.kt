package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ScimSetupStep
import javax.annotation.processing.Generated

@Generated
data class SetupConfidentialScim(
    val scimSetupStep: ScimSetupStep,
) : TrackingLog {
    override val name
        get() = "setup_confidential_scim"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["scim_setup_step"] = scimSetupStep
    }
}