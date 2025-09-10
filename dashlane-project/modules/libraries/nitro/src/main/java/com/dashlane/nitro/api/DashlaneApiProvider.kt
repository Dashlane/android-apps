package com.dashlane.nitro.api

import com.dashlane.network.inject.HttpModule.Companion.SystemClockElapsedRealTime
import com.dashlane.nitro.api.tools.toHostUrl
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.ConnectivityCheck
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.DashlaneApiClient
import okhttp3.Call
import javax.inject.Inject
import javax.inject.Named

class DashlaneApiProvider @Inject constructor(
    @Named("NitroCallFactory") private val callFactory: Call.Factory,
    private val connectivityCheck: ConnectivityCheck,
    private val authorization: Authorization.App,
) {
    fun getApiClient(
        nitroHost: String,
        behindProxy: Boolean,
    ): DashlaneApiClient = DashlaneApiClient(
        callFactory = callFactory,
        connectivityCheck = connectivityCheck,
        host = if (behindProxy) {
            
            
            "${nitroHost.toHostUrl()}/v1-nitro-encryption-service/"
        } else {
            
            
            "${nitroHost.toHostUrl()}/api/"
        },
    )

    fun getApi(
        dashlaneApiClient: DashlaneApiClient,
        behindProxy: Boolean,
    ): DashlaneApi = DashlaneApi(
        client = dashlaneApiClient,
        appAuthorization = authorization,
        clock = SystemClockElapsedRealTime(),
        needSynchronizedClock = behindProxy,
    )
}