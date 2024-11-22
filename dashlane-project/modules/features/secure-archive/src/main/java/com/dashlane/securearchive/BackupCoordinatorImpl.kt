package com.dashlane.securearchive

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.dashlane.hermes.LogRepository
import com.dashlane.navigation.Navigator
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.SnackbarUtils
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupCoordinatorImpl @Inject constructor(
    private val secureArchiveManager: SecureArchiveManager,
    private val logRepository: LogRepository,
    private val navigator: Navigator,
) : BackupCoordinator {
    val activityLifecycleListener = object : AbstractActivityLifecycleListener() {
        override fun onActivityResumed(activity: Activity) {
            lastActivityResumed = activity as? DashlaneActivity
            lastActivityResumed?.let {
                withActivityBlock?.invoke(it)
                withActivityBlock = null
            }
        }

        override fun onLastActivityStopped() {
            lastActivityResumed = null
        }

        override fun onActivityResult(activity: DashlaneActivity, requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                REQUEST_CODE_CHOOSE_FILE -> handleChooseFileResult(activity, resultCode, data)
                REQUEST_CODE_EXPORT -> handleExportResult(activity, requestCode, resultCode, data)
                REQUEST_CODE_IMPORT -> handleImportResult(activity, requestCode, resultCode, data)
            }
        }
    }

    private var lastActivityResumed: DashlaneActivity? = null
    private var withActivityBlock: ((DashlaneActivity) -> Unit)? = null

    @Suppress("DEPRECATION")
    override fun startExport() {
        withActivity { activity ->
            activity.lifecycleScope.launch {
                activity.startActivityForResult(
                    BackupActivityIntents.newExportIntent(activity),
                    REQUEST_CODE_EXPORT
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun startImport() {
        withActivity { activity ->
            activity.lockHelper.startAutoLockGracePeriod(Duration.ofMinutes(2))

            val chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "\1/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }.putExtra(Intent.EXTRA_LOCAL_ONLY, true)

            activity.startActivityForResult(chooseFileIntent, REQUEST_CODE_CHOOSE_FILE)
        }
    }

    @Suppress("DEPRECATION")
    private fun handleChooseFileResult(
        activity: DashlaneActivity,
        resultCode: Int,
        data: Intent?
    ) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val uri = data?.data

        activity.lifecycleScope.launch {
            if (uri != null && secureArchiveManager.hasData(uri)) {
                activity.startActivityForResult(
                    BackupActivityIntents.newImportIntent(
                        activity,
                        uri
                    ),
                    REQUEST_CODE_IMPORT
                )
            } else {
                SnackbarUtils.showSnackbar(
                    activity,
                    activity.getString(R.string.backup_import_failure_message),
                    SNACKBAR_DURATION
                ) {
                    setAction(R.string.backup_import_failure_action) {
                        startImport()
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun handleExportResult(
        activity: DashlaneActivity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (resultCode != Activity.RESULT_OK) return

        val isSuccessful = data?.getBooleanExtra(BackupActivityIntents.EXTRA_IS_SUCCESSFUL, false) ?: false
        val isShared = data?.getBooleanExtra(BackupActivityIntents.EXTRA_SHARED, false) ?: false

        if (isSuccessful) {
            getBackupLogger().logExportSuccessDisplay()
            displaySuccessSnackbar(activity, isShared)
        } else {
            val statedWith = data?.getParcelableExtra<Intent?>(BackupActivityIntents.EXTRA_STARTED_WITH) ?: return
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.backup_export_failure_message),
                SNACKBAR_DURATION
            ) {
                setAction(R.string.backup_export_failure_action) {
                    activity.startActivityForResult(statedWith, requestCode)
                }
            }
        }
    }

    private fun displaySuccessSnackbar(activity: DashlaneActivity, isShared: Boolean) {
        if (isShared) {
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.backup_export_sharing_fallback_success_message),
                SNACKBAR_DURATION
            )
        } else {
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.backup_export_success_message),
                SNACKBAR_DURATION
            ) {
                
                setAction(R.string.backup_export_success_action) {
                    activity.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun handleImportResult(
        activity: DashlaneActivity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (resultCode != Activity.RESULT_OK) return

        val isSuccessful = data?.getBooleanExtra(BackupActivityIntents.EXTRA_IS_SUCCESSFUL, false) ?: false

        if (isSuccessful) {
            navigator.goToHome()
            val c = data?.getIntExtra(BackupActivityIntents.EXTRA_COUNT, 0) ?: 0
            getBackupLogger().logImportSuccessDisplay()
            SnackbarUtils.showSnackbar(
                activity,
                activity.resources.getQuantityString(R.plurals.backup_import_success_message, c, c),
                SNACKBAR_DURATION
            )
        } else {
            val statedWith = data?.getParcelableExtra<Intent?>(BackupActivityIntents.EXTRA_STARTED_WITH) ?: return
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.backup_import_failure_message),
                SNACKBAR_DURATION
            ) {
                setAction(R.string.backup_import_failure_action) {
                    activity.startActivityForResult(statedWith, requestCode)
                }
            }
        }
    }

    private fun getBackupLogger() = BackupLogger(logRepository)

    
    private fun withActivity(block: (activity: DashlaneActivity) -> Unit) {
        withActivityBlock = null
        val activity = lastActivityResumed

        if (activity?.isFinishing == false) {
            block(activity)
        } else {
            withActivityBlock = block
        }
    }

    companion object {
        private const val REQUEST_CODE_EXPORT = 27_190
        private const val REQUEST_CODE_CHOOSE_FILE = 27_191
        private const val REQUEST_CODE_IMPORT = 27_192

        private const val SNACKBAR_DURATION = 5_000
    }
}