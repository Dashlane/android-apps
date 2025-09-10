package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.TransferDataSource
import com.dashlane.hermes.generated.definitions.TransferDataStatus
import com.dashlane.hermes.generated.definitions.TransferDataStep
import javax.annotation.processing.Generated

@Generated
data class ExportData(
    val exportDataStatus: TransferDataStatus,
    val exportDestination: TransferDataSource? = null,
    val exportDataStep: TransferDataStep,
    val exportedItemsCount: Int? = null,
    val itemsToExportCount: Int? = null,
    val backupFileType: BackupFileType,
) : TrackingLog {
    override val name
        get() = "export_data"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["export_data_status"] = exportDataStatus
        collector["export_destination"] = exportDestination
        collector["export_data_step"] = exportDataStep
        collector["exported_items_count"] = exportedItemsCount
        collector["items_to_export_count"] = itemsToExportCount
        collector["backup_file_type"] = backupFileType
    }
}