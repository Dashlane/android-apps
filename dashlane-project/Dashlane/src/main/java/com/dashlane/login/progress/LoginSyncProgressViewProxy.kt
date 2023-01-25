package com.dashlane.login.progress

import android.app.Activity
import android.content.Intent
import androidx.annotation.IntRange
import com.dashlane.R
import com.dashlane.ui.util.DialogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay

@Suppress("EXPERIMENTAL_API_USAGE")
class LoginSyncProgressViewProxy(
    val activity: Activity,
    initialMessage: String,
    coroutineScope: CoroutineScope
) : LoginSyncProgressContract.ViewProxy, CoroutineScope by coroutineScope {

    var presenter: LoginSyncProgressContract.Presenter? = null

    private val percentViewProxy =
        LoginSyncProgressProcessPercentViewProxy(activity.findViewById(R.id.progress_process_percent_layout)).apply {
            setMessage(initialMessage)
            setNotes(activity.getText(R.string.login_sync_progress_notes))
            showLoader()
        }

    private val messageActor = actor<String>(
        Dispatchers.Main,
        Channel.CONFLATED
    ) {
        consumeEach {
            percentViewProxy.setMessage(it)
            delay(1000) 
        }
    }

    override fun setMessage(text: String) {
        messageActor.trySend(text)
    }

    override fun setProgress(@IntRange(from = 0, to = 100) value: Int) {
        percentViewProxy.setProgress(value)
    }

    override fun showUnlinkError() = DialogHelper().builder(activity, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
        .setTitle(R.string.login_sync_progress_unlink_devices_error_title)
        .setMessage(R.string.login_sync_progress_unlink_devices_error_message)
        .setPositiveButton(R.string.login_sync_progress_unlink_devices_error_positive_button) { _, _ ->
            presenter?.retry()
        }
        .setNegativeButton(R.string.login_sync_progress_unlink_devices_error_negative_button) { _, _ ->
            activity.finish()
        }
        .setCancelable(false)
        .create()
        .show()

    override fun finish(intent: Intent?) {
        if (intent != null) {
            val message = activity.getString(R.string.login_sync_progress_success)
            messageActor.trySend(message)
            percentViewProxy.showSuccess(message) {
                activity.startActivity(intent)
                activity.finishAffinity()
            }
        } else {
            activity.finishAffinity()
        }
    }
}
