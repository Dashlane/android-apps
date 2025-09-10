package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.Space
import javax.annotation.processing.Generated

@Generated
data class RemoveTwoFactorAuthenticationFromCredentialAnonymous(
    val domain: Domain,
    val authenticatorIssuerId: Sha256Hash? = null,
    val space: Space,
) : TrackingLog {
    override val name
        get() = "remove_two_factor_authentication_from_credential"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["domain"] = domain
        collector["authenticator_issuer_id"] = authenticatorIssuerId
        collector["space"] = space
    }
}