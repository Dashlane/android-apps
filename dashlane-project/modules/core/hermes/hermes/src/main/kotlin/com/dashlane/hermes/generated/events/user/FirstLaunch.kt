package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Android
import com.dashlane.hermes.generated.definitions.Ios
import com.dashlane.hermes.generated.definitions.Web
import javax.annotation.processing.Generated

@Generated
data class FirstLaunch(
    val isMarketingOptIn: Boolean? = null,
    val web: Web? = null,
    val android: Android? = null,
    val ios: Ios? = null,
) : TrackingLog {
    override val name
        get() = "first_launch"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_marketing_opt_in"] = isMarketingOptIn
        collector["web"] = web
        collector["android"] = android
        collector["ios"] = ios
    }
}