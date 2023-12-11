package com.dashlane.securearchive

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.ImportDataStatus
import com.dashlane.hermes.generated.definitions.ImportDataStep
import com.dashlane.hermes.generated.definitions.ImportSource
import com.dashlane.hermes.generated.events.user.ExportData
import com.dashlane.hermes.generated.events.user.ImportData
import javax.inject.Inject

internal class BackupLogger @Inject constructor(
    private val logRepository: LogRepository
) {

    fun logExportSuccessDisplay() {
        logRepository.queueEvent(ExportData(BackupFileType.SECURE_VAULT))
    }

    fun logImportSuccessDisplay() {
        logRepository.queueEvent(
            ImportData(
                backupFileType = BackupFileType.SECURE_VAULT,
                importDataStatus = ImportDataStatus.SUCCESS,
                importSource = ImportSource.SOURCE_DASH,
                importDataStep = ImportDataStep.SUCCESS,
                isDirectImport = false
            )
        )
    }

    enum class Which(
        val subtype: String
    ) {
        IMPORT("import"),
        EXPORT("export")
    }
}