package com.dashlane.securearchive

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.ImportDataStatus
import com.dashlane.hermes.generated.definitions.ImportDataStep
import com.dashlane.hermes.generated.definitions.ImportSource
import com.dashlane.hermes.generated.events.user.ExportData
import com.dashlane.hermes.generated.events.user.ImportData
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

internal class BackupLogger @Inject constructor(
    private val logRepository: LogRepository,
    private val usageLogRepository: UsageLogRepository?
) {

    fun logStart(which: Which) = log(which, "start")

    fun logExportSuccessDisplay() {
        logRepository.queueEvent(ExportData(BackupFileType.SECURE_VAULT))
        log(
            Which.EXPORT,
            action = "success",
            subaction = "display"
        )
    }

    fun logImportSuccessDisplay(count: Int) {
        logRepository.queueEvent(
            ImportData(
                backupFileType = BackupFileType.SECURE_VAULT,
                importDataStatus = ImportDataStatus.SUCCESS,
                importSource = ImportSource.SOURCE_DASH,
                importDataStep = ImportDataStep.SUCCESS,
                isDirectImport = false
            )
        )
        log(
            Which.IMPORT,
            action = "success",
            subaction = count.toString()
        )
    }

    fun logSeeFileClicked() = log(
        Which.EXPORT,
        action = "success",
        subaction = "click_see_file"
    )

    fun logFailureDisplay(which: Which) = log(
        which,
        action = "failure",
        subaction = "display"
    )

    fun logTryAgainClicked(which: Which) = log(
        which,
        action = "failure",
        subaction = "click_try_again"
    )

    fun logPopupDisplayed(which: Which) = log(
        which,
        action = "popup",
        subaction = "display"
    )

    fun logPopupActionClicked(which: Which) = log(
        which,
        action = "popup",
        subaction = "click_action"
    )

    fun logPopupCancelClicked(which: Which) = log(
        which,
        action = "popup",
        subaction = "click_cancel"
    )

    fun logPopupWrongPassword(which: Which) = log(
        which,
        action = "popup",
        subaction = "wrong_password"
    )

    fun logPermissionDisapprovalDisplayed() = log(
        Which.EXPORT,
        action = "permission_disapproval",
        subaction = "display"
    )

    fun logPermissionDisapprovalClicked() = log(
        Which.EXPORT,
        action = "permission_disapproval",
        subaction = "click_settings"
    )

    fun logChooseFileSelected() = log(
        Which.IMPORT,
        action = "choose_file",
        subaction = "select"
    )

    fun logChooseFileCancelled() = log(
        Which.IMPORT,
        action = "choose_file",
        subaction = "cancel"
    )

    enum class Which(
        val subtype: String
    ) {
        IMPORT("import"),
        EXPORT("export")
    }

    private fun log(
        which: Which,
        action: String? = null,
        subaction: String? = null
    ) {
        usageLogRepository?.enqueue(
            UsageLogCode75(
                type = "local_backup",
                subtype = which.subtype,
                action = action,
                subaction = subaction
            )
        )
    }
}