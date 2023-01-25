package com.dashlane.login.progress

import android.content.Intent
import androidx.annotation.IntRange



interface LoginSyncProgressContract {

    interface ViewProxy {
        fun showUnlinkError()
        fun setMessage(text: String)
        fun setProgress(@IntRange(from = 0, to = 100) value: Int)
        fun finish(intent: Intent? = null)
    }

    interface Presenter {
        fun retry()
    }
}