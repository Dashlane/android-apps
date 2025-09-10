package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class DisableSetting(
    val durationSetting: AutofillDurationSetting? = null,
    val configuration: AutofillConfiguration? = null,
    val scope: AutofillScope? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["duration_setting"] = durationSetting
        collector["configuration"] = configuration
        collector["scope"] = scope
    }
}