package com.dashlane.logger.utils

import com.dashlane.hermes.LogFlush
import com.dashlane.logger.Log.d
import com.dashlane.useractivity.log.UserActivityFlush
import javax.inject.Inject

class LogsSender @Inject constructor(
    private val userActivityFlush: UserActivityFlush,
    private val logFlush: LogFlush
) {
    fun flushLogs() {
        d("TRACKING", "Send logs")
        userActivityFlush.invoke()
        logFlush.invoke()
    }
}