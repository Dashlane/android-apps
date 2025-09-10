package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Button
import com.dashlane.hermes.generated.definitions.ClickOrigin
import javax.annotation.processing.Generated

@Generated
data class Click(
    val button: Button,
    val clickOrigin: ClickOrigin? = null,
) : TrackingLog {
    override val name
        get() = "click"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["button"] = button
        collector["click_origin"] = clickOrigin
    }
}