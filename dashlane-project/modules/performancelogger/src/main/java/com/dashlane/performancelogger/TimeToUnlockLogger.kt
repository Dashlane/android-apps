package com.dashlane.performancelogger

import com.dashlane.server.api.endpoints.monitoring.ReportClientTestPerformanceService
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeToUnlockLogger @Inject constructor(
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    service: ReportClientTestPerformanceService
) : PerformanceLogger(globalCoroutineScope, service) {

    companion object {
        const val TYPE_MP = "mp"
        const val TYPE_PIN = "pin"
        const val TYPE_BIOMETRIC = "biometric"
    }

    override val action: String
        get() = "timeToUnlock"
}