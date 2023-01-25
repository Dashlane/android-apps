package com.dashlane.performancelogger

import com.dashlane.server.api.endpoints.monitoring.ReportClientTestPerformanceService
import java.time.Duration
import javax.inject.Inject

class TimeToRacletteWriteLogger @Inject constructor(service: ReportClientTestPerformanceService) :
    PerformanceLogger(service = service) {
    override val action: String
        get() = "timeToRacletteWrite"

    fun sendDuration(duration: Duration, operationCount: Int) {
        type = operationCount.toString()
        send(duration)
    }
}