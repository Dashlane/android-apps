package com.dashlane.logger

import com.dashlane.hermes.service.AnalyticsErrorReporter
import com.dashlane.server.api.exceptions.DashlaneApiException
import javax.inject.Inject

class AnalyticsErrorReporterImpl @Inject constructor() : AnalyticsErrorReporter {
    override fun report(username: String?, exception: DashlaneApiException) {
            message = exception.message ?: "",
            throwable = exception,
        )
    }
}