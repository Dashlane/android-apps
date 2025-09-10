package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.DataSource
import com.dashlane.hermes.generated.definitions.TransferDataStatus
import javax.annotation.processing.Generated

@Generated
data class SubmitData(
    val itemCount: Int? = null,
    val itemSource: BackupFileType,
    val submitStep: TransferDataStatus,
    val itemType: DataSource,
) : TrackingLog {
    override val name
        get() = "submit_data"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["item_count"] = itemCount
        collector["item_source"] = itemSource
        collector["submit_step"] = submitStep
        collector["item_type"] = itemType
    }
}