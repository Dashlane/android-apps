package com.dashlane.performancelogger

import com.dashlane.server.api.endpoints.monitoring.ReportClientTestPerformanceService
import javax.inject.Inject

class TimeToAutofillLogger @Inject constructor(service: ReportClientTestPerformanceService) :
    PerformanceLogger(service = service) {

    var hasDataSet: Boolean = false

    override val action: String
        get() = "timeToLoadAutofill"

    override fun logStop() {
        if (!hasDataSet) {
            clear()
            return
        }
        super.logStop()
    }
}
