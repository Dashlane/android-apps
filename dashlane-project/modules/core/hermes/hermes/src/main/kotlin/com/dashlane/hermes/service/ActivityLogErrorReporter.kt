package com.dashlane.hermes.service

import com.dashlane.hermes.storage.LogItem
import com.dashlane.server.api.endpoints.teams.InvalidActivityLog

interface ActivityLogErrorReporter {
    fun report(
        username: String? = null,
        invalidLogs: List<InvalidLog>,
    )

    data class InvalidLog(
        val logItem: LogItem,
        val invalidLog: InvalidActivityLog,
    )
}