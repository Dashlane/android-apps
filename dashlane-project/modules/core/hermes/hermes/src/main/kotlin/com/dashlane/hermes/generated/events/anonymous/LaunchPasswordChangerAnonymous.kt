package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.PasswordChangerFailureReason
import javax.annotation.processing.Generated

@Suppress("kotlin:S1133")
@Deprecated("")
@Generated
data class LaunchPasswordChangerAnonymous(
    val passwordChangerFailureReason: PasswordChangerFailureReason? = null,
    val domain: Domain,
    val isBulkChange: Boolean,
    val bulkChangeCredentialCount: Int? = null,
    val isSuccess: Boolean,
    val passwordChangerRecipeVersion: Sha256Hash? = null,
    val lastRecipeStepPosition: Int,
) : TrackingLog {
    override val name
        get() = "launch_password_changer"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["password_changer_failure_reason"] = passwordChangerFailureReason
        collector["domain"] = domain
        collector["is_bulk_change"] = isBulkChange
        collector["bulk_change_credential_count"] = bulkChangeCredentialCount
        collector["is_success"] = isSuccess
        collector["password_changer_recipe_version"] = passwordChangerRecipeVersion
        collector["last_recipe_step_position"] = lastRecipeStepPosition
    }
}