package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class CredentialSecurityStatus(
    val isWeak: Boolean? = null,
    val isReused: Boolean? = null,
    val isDeleted: Boolean? = null,
    val isCompromised: Boolean? = null,
    val isExcluded: Boolean? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["is_weak"] = isWeak
        collector["is_reused"] = isReused
        collector["is_deleted"] = isDeleted
        collector["is_compromised"] = isCompromised
        collector["is_excluded"] = isExcluded
    }
}