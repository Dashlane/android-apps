package com.dashlane.hermes.service

import com.dashlane.server.api.exceptions.DashlaneApiException

interface AnalyticsErrorReporter {
    fun report(
        username: String? = null,
        exception: DashlaneApiException,
    )
}