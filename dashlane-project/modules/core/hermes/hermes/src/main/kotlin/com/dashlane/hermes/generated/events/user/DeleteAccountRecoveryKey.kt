package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DeleteKeyReason
import javax.annotation.processing.Generated

@Generated
data class DeleteAccountRecoveryKey(
    val deleteKeyReason: DeleteKeyReason,
) : TrackingLog {
    override val name
        get() = "delete_account_recovery_key"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["delete_key_reason"] = deleteKeyReason
    }
}