package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CryptoAlgorithm
import com.dashlane.hermes.generated.definitions.CryptoMigrationStatus
import com.dashlane.hermes.generated.definitions.CryptoMigrationType
import javax.annotation.processing.Generated

@Generated
data class MigrateCrypto(
    val previousCrypto: CryptoAlgorithm,
    val newCrypto: CryptoAlgorithm,
    val type: CryptoMigrationType,
    val status: CryptoMigrationStatus,
) : TrackingLog {
    override val name
        get() = "migrate_crypto"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["previous_crypto"] = previousCrypto
        collector["new_crypto"] = newCrypto
        collector["type"] = type
        collector["status"] = status
    }
}