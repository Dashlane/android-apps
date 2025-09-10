package com.dashlane.securearchive

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.TransferDataSource
import com.dashlane.hermes.generated.definitions.TransferDataStatus
import com.dashlane.hermes.generated.definitions.TransferDataStep
import com.dashlane.hermes.generated.events.user.ExportData
import com.dashlane.hermes.generated.events.user.ImportData
import javax.inject.Inject

class BackupLogger @Inject constructor(
    private val logRepository: LogRepository,
) {
    fun logExportFlowStarted() {
        logRepository.queueEvent(
            ExportData(
                backupFileType = BackupFileType.DASH,
                exportDataStep = TransferDataStep.SELECT_EXPORT_DESTINATION,
                exportDataStatus = TransferDataStatus.START,
                exportDestination = TransferDataSource.SOURCE_DASH,
            ),
        )
    }

    fun logExportSuccessDisplay() {
        logRepository.queueEvent(
            ExportData(
                exportDataStatus = TransferDataStatus.SUCCESS,
                exportDestination = TransferDataSource.SOURCE_DASH,
                exportDataStep = TransferDataStep.SUCCESS,
                backupFileType = BackupFileType.DASH,
            ),
        )
    }

    fun logImportFlowStarted() {
        logRepository.queueEvent(
            ImportData(
                backupFileType = BackupFileType.DASH,
                importDataStatus = TransferDataStatus.START,
                importSource = TransferDataSource.SOURCE_DASH,
                importDataStep = TransferDataStep.SELECT_FILE,
                isDirectImport = false,
            ),
        )
    }

    fun logImportSuccessDisplay() {
        logRepository.queueEvent(
            ImportData(
                backupFileType = BackupFileType.DASH,
                importDataStatus = TransferDataStatus.SUCCESS,
                importSource = TransferDataSource.SOURCE_DASH,
                importDataStep = TransferDataStep.SUCCESS,
                isDirectImport = false,
            ),
        )
    }
}