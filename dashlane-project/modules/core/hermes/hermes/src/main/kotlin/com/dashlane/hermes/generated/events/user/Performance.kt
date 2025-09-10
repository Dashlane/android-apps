package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.MeasureName
import com.dashlane.hermes.generated.definitions.MeasureType
import com.dashlane.hermes.generated.definitions.Unit
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class Performance(
    val measureType: MeasureType? = null,
    val unit: Unit? = null,
    val measureName: MeasureName,
    val value: Double,
) : TrackingLog {
    override val name
        get() = "performance"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["measure_type"] = measureType
        collector["unit"] = unit
        collector["measure_name"] = measureName
        collector["value"] = value
    }
}