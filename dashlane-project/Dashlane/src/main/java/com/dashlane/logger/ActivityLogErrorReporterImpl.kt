package com.dashlane.logger

import com.dashlane.hermes.service.ActivityLogErrorReporter
import com.dashlane.hermes.service.ActivityLogErrorReporter.InvalidLog
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.util.tryOrNull
import com.google.gson.Gson
import javax.inject.Inject

class ActivityLogErrorReporterImpl @Inject constructor() : ActivityLogErrorReporter {

    override fun report(username: String?, invalidLogs: List<InvalidLog>) {
        invalidLogs.forEach { log ->
            val error = log.invalidLog.error.key
            val activityLog = tryOrNull {
                Gson().fromJson(log.logItem.logContent, ActivityLog::class.java)
            }
                message = "$error - An activity log with a log type of " +
                    "${activityLog?.logType?.key} and uid: ${log.logItem.logId} has " +
                    "been detected to have a technical / schema error"
            )
        }
    }
}