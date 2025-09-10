package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AuthMethodMigrationFlowStep
import com.dashlane.hermes.generated.definitions.MigrateAuthMethodError
import com.dashlane.hermes.generated.definitions.OriginalAuthMethod
import javax.annotation.processing.Generated

@Generated
data class MigrateAuthMethod(
    val errorName: MigrateAuthMethodError? = null,
    val authMethodMigrationFlowStep: AuthMethodMigrationFlowStep,
    val originalAuthMethod: OriginalAuthMethod? = null,
) : TrackingLog {
    override val name
        get() = "migrate_auth_method"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["error_name"] = errorName
        collector["auth_method_migration_flow_step"] = authMethodMigrationFlowStep
        collector["original_auth_method"] = originalAuthMethod
    }
}