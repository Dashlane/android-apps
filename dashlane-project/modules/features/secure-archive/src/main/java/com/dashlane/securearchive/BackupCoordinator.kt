package com.dashlane.securearchive

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.dashlane.navigation.Navigator
import com.dashlane.securearchive.contract.BackupActivityResult
import com.dashlane.util.SnackbarUtils
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupCoordinator @Inject constructor(
    private val secureArchiveManager: SecureArchiveManager,
    private val backupLogger: BackupLogger,
    private val navigator: Navigator,
) {
    fun onExportStarted() {
        backupLogger.logExportFlowStarted()
    }

    @Suppress("DEPRECATION")
    fun handleChooseFileResult(
        activity: ComponentActivity,
        uri: Uri?,
        onValidDashFile: (Uri) -> Unit,
        onRetryImport: () -> Unit,
    ) {
        activity.lifecycleScope.launch {
            if (uri != null && secureArchiveManager.hasData(uri)) {
                onValidDashFile(uri)
            } else {
                SnackbarUtils.showSnackbar(
                    activity,
                    activity.getString(R.string.backup_import_failure_message),
                    SNACKBAR_DURATION,
                ) {
                    setAction(R.string.backup_import_failure_action) {
                        onRetryImport()
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    fun handleExportResult(
        activity: Activity,
        exportResult: BackupActivityResult,
        onRetryExport: () -> Unit,
    ) {
        if (exportResult.isSuccessful) {
            backupLogger.logExportSuccessDisplay()
            displaySuccessSnackbar(activity, exportResult.isShared)
        } else {
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.backup_export_failure_message),
                SNACKBAR_DURATION,
            ) {
                setAction(R.string.backup_export_failure_action) {
                    onRetryExport()
                }
            }
        }
    }

    private fun displaySuccessSnackbar(activity: Activity, isShared: Boolean) {
        if (isShared) {
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.backup_export_sharing_fallback_success_message),
                SNACKBAR_DURATION,
            )
        } else {
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.backup_export_success_message),
                SNACKBAR_DURATION,
            ) {
                
                setAction(R.string.backup_export_success_action) {
                    activity.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                }
            }
        }
    }

    fun onImportStarted() {
        backupLogger.logImportFlowStarted()
    }

    @Suppress("DEPRECATION")
    fun handleImportResult(
        activity: Activity,
        importResult: BackupActivityResult,
        onRetryImport: () -> Unit,
    ) {
        val isSuccessful = importResult.isSuccessful

        if (isSuccessful) {
            navigator.goToHome()
            val count = importResult.count
            backupLogger.logImportSuccessDisplay()
            SnackbarUtils.showSnackbar(
                activity,
                activity.resources.getQuantityString(R.plurals.backup_import_success_message, count, count),
                SNACKBAR_DURATION,
            )
        } else {
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.backup_import_failure_message),
                SNACKBAR_DURATION,
            ) {
                setAction(R.string.backup_import_failure_action) {
                    onRetryImport()
                }
            }
        }
    }

    companion object {
        private const val SNACKBAR_DURATION = 5_000
    }
}