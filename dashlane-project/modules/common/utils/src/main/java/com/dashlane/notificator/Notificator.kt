package com.dashlane.notificator

import androidx.fragment.app.FragmentActivity

fun interface Notificator {
    fun customErrorDialogMessage(
        activity: FragmentActivity?,
        topic: String?,
        message: String?,
        shouldCloseCaller: Boolean
    )
}