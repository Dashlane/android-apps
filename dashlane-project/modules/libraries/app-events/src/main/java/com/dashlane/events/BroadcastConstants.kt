package com.dashlane.events

object BroadcastConstants {
    const val PASSWORD_SUCCESS_BROADCAST = "com.dashlane.PASSWORD"
    const val NEW_TOKEN_BROADCAST = "com.dashlane.NEW_TOKEN"
    const val SUCCESS_EXTRA = "success"
    const val SYNC_FINISHED_BROADCAST = "com.dashlane.SYNCFINISH"
    const val SYNC_PROGRESS_BROADCAST = "com.dashlane.SYNC_PROGRESS_SHOW"
    const val SYNC_PROGRESS_BROADCAST_SHOW_PROGRESS = "$SYNC_PROGRESS_BROADCAST.showProgress"
}
