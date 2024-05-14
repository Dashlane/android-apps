package com.dashlane.ui.widgets

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.notificator.Notificator
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment.TwoButtonClicker
import com.dashlane.util.Toaster
import javax.inject.Inject

class NotificatorImpl @Inject constructor(private val toaster: Toaster) : Notificator {
    override fun customErrorDialogMessage(
        activity: FragmentActivity?,
        topic: String?,
        message: String?,
        shouldCloseCaller: Boolean
    ) {
        
        if (activity != null && !activity.isFinishing) {
            val dialog = NotificationDialogFragment.Builder()
                .setTitle(topic)
                .setMessage(message)
                .setNegativeButtonText(null)
                .setPositiveButtonText(activity.getString(R.string.ok))
                .setCancelable(false)
                .setClicker(object : TwoButtonClicker {
                    override fun onNegativeButton() {
                        if (shouldCloseCaller) {
                            activity.finish()
                        }
                    }

                    override fun onPositiveButton() {
                        if (shouldCloseCaller) {
                            activity.finish()
                        }
                    }
                }).build()
            dialog.show(activity.supportFragmentManager, "customErrorMessageDialog")
        } else {
            
            toaster.show(message, Toast.LENGTH_LONG)
        }
    }
}
