package com.dashlane.logger.utils

import com.dashlane.hermes.LogFlush
import com.dashlane.logger.Log.d
import javax.inject.Inject

class LogsSender @Inject constructor(
    private val logFlush: LogFlush
) {
    fun flushLogs() {
        d("TRACKING", "Send logs")
        logFlush.invoke()
    }
}