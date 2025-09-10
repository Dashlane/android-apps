package com.dashlane.hermes.service

import com.dashlane.server.api.analytics.AnalyticsApi
import com.dashlane.server.api.analytics.events.EventService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface AnalyticsLogService {
    suspend fun sendLogs(
        formattedLogBatch: String,
        logType: EventService.EventType,
    ): Boolean

    interface TestLogChecker {
        operator fun invoke(): Boolean
    }
}

class AnalyticsLogServiceImpl(
    private val analyticsApi: AnalyticsApi,
    private val testLogChecker: AnalyticsLogService.TestLogChecker,
    private val ioDispatcher: CoroutineDispatcher,
) : AnalyticsLogService {
    override suspend fun sendLogs(
        formattedLogBatch: String,
        logType: EventService.EventType,
    ): Boolean =
        withContext(ioDispatcher) {
            val analyticsEndpoints = analyticsApi.analyticsEndpoints
            val request = EventService.Request(body = formattedLogBatch)
            val testLog = testLogChecker()
            val endpoint = analyticsEndpoints.default.event
            return@withContext endpoint.execute(request, logType, testLog).success
        }
}