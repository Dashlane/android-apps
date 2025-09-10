package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.HelpCenterArticleCta
import javax.annotation.processing.Generated

@Generated
data class OpenHelpCenter(
    val helpCenterArticleCta: HelpCenterArticleCta? = null,
) : TrackingLog {
    override val name
        get() = "open_help_center"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["help_center_article_cta"] = helpCenterArticleCta
    }
}