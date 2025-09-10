package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DismissType
import com.dashlane.hermes.generated.definitions.Domain
import javax.annotation.processing.Generated

@Generated
data class AutofillDismissAnonymous(
    val dismissType: DismissType,
    val domain: Domain,
    val isNativeApp: Boolean,
) : TrackingLog {
    override val name
        get() = "autofill_dismiss"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["dismiss_type"] = dismissType
        collector["domain"] = domain
        collector["is_native_app"] = isNativeApp
    }
}