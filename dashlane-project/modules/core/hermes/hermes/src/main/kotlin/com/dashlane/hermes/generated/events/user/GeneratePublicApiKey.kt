package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.PublicApiKeyStep
import javax.annotation.processing.Generated

@Generated
data class GeneratePublicApiKey(
    val publicApiKeyStep: PublicApiKeyStep,
) : TrackingLog {
    override val name
        get() = "generate_public_api_key"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["public_api_key_step"] = publicApiKeyStep
    }
}