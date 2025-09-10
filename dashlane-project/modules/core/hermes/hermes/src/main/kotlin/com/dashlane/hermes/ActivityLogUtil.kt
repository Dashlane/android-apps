package com.dashlane.hermes

import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.endpoints.teams.Properties
import com.dashlane.server.api.time.InstantEpochMilli
import com.dashlane.server.api.time.toInstantEpochMilli
import java.time.Instant
import java.util.UUID

fun activityLog(
    schemaVersion: ActivityLog.SchemaVersion = ActivityLog.SchemaVersion.V_1_0_0,
    dateTime: InstantEpochMilli = Instant.now().toInstantEpochMilli(),
    uuid: String = UUID.randomUUID().toString(),
    logType: ActivityLog.LogType,
    properties: Properties,
) = ActivityLog(
    schemaVersion = schemaVersion,
    dateTime = dateTime,
    uuid = uuid,
    logType = logType,
    properties = properties,
)