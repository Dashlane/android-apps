package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class FieldsFilledByCount(
    val autologinCount: Int,
    val manuallyTypedCount: Int,
    val autofillCount: Int,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["autologin_count"] = autologinCount
        collector["manually_typed_count"] = manuallyTypedCount
        collector["autofill_count"] = autofillCount
    }
}