package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AuthenticatorNames
import javax.annotation.processing.Generated

@Generated
data class OtherAuthenticatorsInstalledReportAnonymous(
    val otherAuthenticatorList: List<AuthenticatorNames>? = null,
) : TrackingLog {
    override val name
        get() = "other_authenticators_installed_report"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["other_authenticator_list"] = otherAuthenticatorList
    }
}