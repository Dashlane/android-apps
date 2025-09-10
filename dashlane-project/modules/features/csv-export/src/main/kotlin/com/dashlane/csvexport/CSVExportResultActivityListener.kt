package com.dashlane.csvexport

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.SnackbarUtils
import java.lang.ref.WeakReference
import javax.inject.Inject

class CSVExportResultActivityListener @Inject constructor() : AbstractActivityLifecycleListener() {
    companion object {
        private const val DURATION = 5_000
    }

    private lateinit var activity: WeakReference<DashlaneActivity>

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        if (activity !is DashlaneActivity) return
        this.activity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        if (activity !is DashlaneActivity) return
        this.activity.clear()
    }

    override fun onActivityResult(
        activity: DashlaneActivity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(activity, requestCode, resultCode, data)
        if (requestCode != CsvExportActivity.REQUEST_CODE) return
        if (resultCode == CsvExportActivity.RESULT_CODE_SUCCESS) {
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.csv_export_success_message),
                DURATION,
            ) {
                
                setAction(R.string.csv_export_success_cta) {
                    activity.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                }
            }
        } else if (resultCode == CsvExportActivity.RESULT_CODE_FAILURE) {
            SnackbarUtils.showSnackbar(
                activity,
                activity.getString(R.string.csv_export_error),
                DURATION,
            )
        }
    }
}