package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.ImportDataDropAction
import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.hermes.generated.definitions.TransferDataSource
import com.dashlane.hermes.generated.definitions.TransferDataStatus
import com.dashlane.hermes.generated.definitions.TransferDataStep
import javax.annotation.processing.Generated

@Generated
data class ImportData(
    val importDataDropAction: ImportDataDropAction? = null,
    val importedItemsCount: Int? = null,
    val importDataStatus: TransferDataStatus,
    val isDirectImport: Boolean,
    val importDataStep: TransferDataStep,
    val importSource: TransferDataSource,
    val backupFileType: BackupFileType,
    val itemsToImportCount: Int? = null,
    val space: Space? = null,
) : TrackingLog {
    override val name
        get() = "import_data"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["import_data_drop_action"] = importDataDropAction
        collector["imported_items_count"] = importedItemsCount
        collector["import_data_status"] = importDataStatus
        collector["is_direct_import"] = isDirectImport
        collector["import_data_step"] = importDataStep
        collector["import_source"] = importSource
        collector["backup_file_type"] = backupFileType
        collector["items_to_import_count"] = itemsToImportCount
        collector["space"] = space
    }
}