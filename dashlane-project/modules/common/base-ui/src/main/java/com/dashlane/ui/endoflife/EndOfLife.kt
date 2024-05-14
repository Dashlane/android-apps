package com.dashlane.ui.endoflife

import android.app.Activity
import com.dashlane.session.SessionObserver

interface EndOfLife : SessionObserver {
    suspend fun checkBeforeSession(activity: Activity)

    fun showExpiredVersionMessaging(activity: Activity)
}
