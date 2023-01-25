package com.dashlane.usersupportreporter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import com.dashlane.network.webservices.CrashReportUploadService
import com.dashlane.ui.util.DialogHelper
import com.dashlane.usersupport.reporter.R
import com.dashlane.util.Toaster
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine



class UserSupportFileUploader @Inject constructor(
    private val userSupportFileLogger: UserSupportFileLogger,
    private val service: CrashReportUploadService,
    private val toaster: Toaster,
    private val clipboardManagerProvider: Provider<ClipboardManager>
) {
    private val clipboardManager
        get() = clipboardManagerProvider.get()

    fun startSyncLogsUpload(context: Context, coroutineScope: CoroutineScope, crashDeviceId: String, file: File) {
        coroutineScope.launch(Dispatchers.Main) {
            showConfirmationDialog(
                context = context,
                title = R.string.user_support_sync_logs_upload_title,
                message = R.string.user_support_sync_logs_upload_message
            )
            upload(context, file, crashDeviceId)
        }
    }

    fun startCrashLogsUpload(context: Context, coroutineScope: CoroutineScope, crashDeviceId: String) {
        coroutineScope.launch(Dispatchers.Main) {
            val file = userSupportFileLogger.logFile ?: return@launch
            showConfirmationDialog(
                context = context,
                title = 0,
                message = R.string.user_support_file_confirmation_description
            )
            upload(context, file, crashDeviceId)
        }
    }

    private fun getCrashIdForUpload(crashDeviceId: String) =
        "{${crashDeviceId.uppercase()}}"

    private suspend fun upload(context: Context, file: File, crashDeviceId: String) {
        val uploadFileName =
            CRASH_FILE_FORMATTED.format(getCrashIdForUpload(crashDeviceId), userSupportFileLogger.timestamp)
        val loadingDialog = showUploadingDialog(context)
        val result = uploadFile(file, uploadFileName)
        loadingDialog.dismiss()
        if (result) {
            showUploadFinishedDialog(context, uploadFileName)
        } else {
            showUploadFailedDialog(context)
        }
    }

    private suspend fun uploadFile(file: File, uploadFileName: String): Boolean {
        val filePart = createUploadBody(file, uploadFileName)
        return runCatching { service.upload(filePart).isSuccess }.getOrDefault(false)
    }

    private fun createUploadBody(file: File, uploadFileName: String): MultipartBody.Part {
        val requestBody = file.asRequestBody("text/plain".toMediaType())
        return MultipartBody.Part.createFormData("file", uploadFileName, requestBody)
    }

    private suspend fun showConfirmationDialog(
        context: Context,
        @StringRes
        title: Int,
        @StringRes
        message: Int
    ) = suspendCoroutine<Unit> { continuation ->
        createDialog(context)
            .apply { if (title != 0) setTitle(title) }
            .setMessage(message)
            .setNegativeButton(R.string.cancel) { _, _ ->
                continuation.resumeWithException(CancellationException())
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                continuation.resume(Unit)
            }
            .setCancelable(false)
            .show()
    }

    private fun showUploadingDialog(context: Context): AlertDialog {
        return createDialog(context)
            .setMessage(R.string.user_support_file_upload_description)
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }

    private fun showUploadFinishedDialog(context: Context, crashDeviceId: String) {
        createDialog(context)
            .setTitle(R.string.user_support_file_finish_title)
            .setMessage(crashDeviceId)
            .setPositiveButton(R.string.copy) { _, _ ->
                copy(crashDeviceId)
            }
            .setCancelable(false)
            .show()
    }

    private fun showUploadFailedDialog(context: Context) {
        createDialog(context)
            .setMessage(R.string.user_support_file_fail_title)
            .setNegativeButton(R.string.ok, null)
            .show()
    }

    private fun createDialog(context: Context) =
        DialogHelper().builder(ContextThemeWrapper(context, R.style.Theme_Dashlane_NoActionBar))

    private fun copy(data: String) {
        runCatching {
            val clip = ClipData.newPlainText("data", data)
            if (clip != null) {
                clipboardManager.setPrimaryClip(clip)
            }
            toaster.show(R.string.copied, Toast.LENGTH_SHORT)
        }
    }

    companion object {
        private const val CRASH_FILE_FORMATTED = "crash_android_%s_%s.log"
    }
}