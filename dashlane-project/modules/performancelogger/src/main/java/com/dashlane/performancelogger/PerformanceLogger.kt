package com.dashlane.performancelogger

import com.dashlane.server.api.endpoints.monitoring.ReportClientTestPerformanceService
import com.dashlane.server.api.time.DurationMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

abstract class PerformanceLogger @OptIn(DelicateCoroutinesApi::class) constructor(
    private val coroutineScope: CoroutineScope = GlobalScope,
    private val service: ReportClientTestPerformanceService
) {
    abstract val action: String
    private var start: Instant? = null
    private var end: Instant? = null
    var type: String? = null

    fun logStart(type: String? = null) {
        this.type = type
        start = Instant.now()
    }

    open fun logStop() {
        start ?: return
        end = Instant.now()
        send()
    }

    fun clear() {
        start = null
        end = null
    }

    internal fun send() {
        start ?: return
        end ?: return
        send(Duration.between(start, end))
    }

    internal fun send(duration: Duration) {
        coroutineScope.launch {
            runCatching {
                service.execute(
                    ReportClientTestPerformanceService.Request(
                        duration = DurationMillis(duration.toMillis()),
                        action = ReportClientTestPerformanceService.Request.Action(action),
                        type = type
                    )
                )
            }
            clear()
        }
    }
}