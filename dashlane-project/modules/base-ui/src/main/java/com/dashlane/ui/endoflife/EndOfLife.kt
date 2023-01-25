package com.dashlane.ui.endoflife

import android.app.Activity

interface EndOfLife {
    suspend fun checkBeforeSession(activity: Activity)

    fun showExpiredVersionMessaging(activity: Activity)
}