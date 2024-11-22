package com.dashlane.util.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.util.Toaster
import com.dashlane.util.isSemanticallyNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.delay

class ClipboardCopyImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val toaster: Toaster,
    private val workManagerProvider: Provider<WorkManager>,
    private val clipboardManagerProvider: Provider<ClipboardManager>
) : ClipboardCopy {

    private val workManager
        get() = workManagerProvider.get()

    private val clipboardManager
        get() = clipboardManagerProvider.get()

    override fun copyToClipboard(data: String, sensitiveData: Boolean, autoClear: Boolean, @StringRes feedback: Int?) {
        if (data.isSemanticallyNull()) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            toaster.show(feedback ?: R.string.copied_, Toast.LENGTH_SHORT)
        }

        workManager.cancelAllWorkByTag(REQUEST_TAG_CLEAR_CLIPBOARD)

        try {
            ClipData.newPlainText("data", data)?.also { clipData ->
                clipData.description.extras = PersistableBundle().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, sensitiveData)
                    } else {
                        putBoolean("android.content.extra.IS_SENSITIVE", sensitiveData)
                    }
                }
                clipboardManager.setPrimaryClip(clipData)
            }
        } catch (e: Exception) {
        }

        if (!autoClear) {
            return
        }
        try {
            if (preferencesManager[sessionManager.session?.username].getBoolean(ConstantsPrefs.CLEAR_CLIPBOARD_ON_TIMEOUT)) {
                val request = OneTimeWorkRequestBuilder<ClearClipboardWorker>()
                    .setInitialDelay(30, TimeUnit.SECONDS) 
                    .addTag(REQUEST_TAG_CLEAR_CLIPBOARD)
                    .build()

                workManager.enqueue(request)
            }
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val REQUEST_TAG_CLEAR_CLIPBOARD = "clear_clipboard"
    }

    class ClearClipboardWorker(
        appContext: Context,
        params: WorkerParameters
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            try {
                val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                
                
                repeat(50) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("credential", "$it"))
                    
                    delay(50)
                }
                
                clipboard.setPrimaryClip(ClipData.newPlainText("credential", " "))

                
                clipboard.clearPrimaryClip()
            } catch (e: Exception) {
            }

            return Result.success()
        }
    }
}
