package com.dashlane.performancelogger

import com.dashlane.server.api.endpoints.monitoring.ReportClientTestPerformanceService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeToLoadLocalLogger @Inject constructor(service: ReportClientTestPerformanceService) :
    PerformanceLogger(service = service) {
    override val action: String
        get() = "timeToLoadLocal"
}