package com.dashlane.inappbilling

interface BillingManager {

    fun connect()

    fun disconnect()

    fun areSubscriptionsSupported(): Boolean

    suspend fun getServiceConnection(): ConnectionScope?
}

suspend inline fun BillingManager.withServiceConnection(block: ConnectionScope.() -> ServiceResult): ServiceResult =
    getServiceConnection()?.block() ?: ServiceResult.Failure.Error