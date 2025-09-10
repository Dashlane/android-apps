package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class Browse(
    val originComponent: BrowseComponent? = null,
    val originPage: AnyPage? = null,
    val component: BrowseComponent? = null,
    val page: AnyPage? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["origin_component"] = originComponent
        collector["origin_page"] = originPage
        collector["component"] = component
        collector["page"] = page
    }
}