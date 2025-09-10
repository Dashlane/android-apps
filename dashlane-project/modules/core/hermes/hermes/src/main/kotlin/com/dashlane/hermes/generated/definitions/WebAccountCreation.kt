package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class WebAccountCreation(
    val gclid: String? = null,
    val hasCookie: Boolean? = null,
    val everflowTransactionId: String? = null,
    val heapIdentity: String? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["gclid"] = gclid
        collector["has_cookie"] = hasCookie
        collector["everflow_transaction_id"] = everflowTransactionId
        collector["heap_identity"] = heapIdentity
    }
}