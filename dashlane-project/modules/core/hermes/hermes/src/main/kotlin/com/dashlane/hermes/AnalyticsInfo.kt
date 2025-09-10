package com.dashlane.hermes

import java.util.UUID

data class AnalyticsInfo(val installationId: UUID, val userId: UUID?, val deviceId: UUID?)