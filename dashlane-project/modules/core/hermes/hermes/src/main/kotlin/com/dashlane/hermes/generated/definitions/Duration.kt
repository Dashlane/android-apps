package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class Duration(
    val sharing: Int,
    val sync: Int,
    val treatProblem: Int,
    val chronological: Int,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["sharing"] = sharing
        collector["sync"] = sync
        collector["treat_problem"] = treatProblem
        collector["chronological"] = chronological
    }
}