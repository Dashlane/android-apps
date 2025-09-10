package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.OptionType
import javax.annotation.processing.Generated

@Generated
data class SharingSelect(
    val optionType: OptionType,
) : TrackingLog {
    override val name
        get() = "sharing_select"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["option_type"] = optionType
    }
}