package com.dashlane.hermes

import java.util.UUID

interface AnalyticsIdRepository {
    fun setUserAnalyticsInfo(
        userId: UUID,
        deviceId: UUID,
    )

    fun getAnalyticsInfo(): AnalyticsInfo

    fun resetAnalyticsInfo()
}

@Suppress("FunctionName")
fun AnalyticsIdRepository(installationId: UUID): AnalyticsIdRepository = AnalyticsIdRepositoryImpl(installationId)

private class AnalyticsIdRepositoryImpl(private val installationId: UUID) :
    AnalyticsIdRepository {
    private var userId: UUID? = null
    private var deviceId: UUID? = null

    override fun setUserAnalyticsInfo(
        userId: UUID,
        deviceId: UUID,
    ) {
        this.userId = userId
        this.deviceId = deviceId
    }

    override fun getAnalyticsInfo() = AnalyticsInfo(installationId = installationId, userId = userId, deviceId = deviceId)

    override fun resetAnalyticsInfo() {
        userId = null
        deviceId = null
    }
}