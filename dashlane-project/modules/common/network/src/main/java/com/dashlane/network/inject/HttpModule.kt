package com.dashlane.network.inject

import android.util.Log
import com.dashlane.network.ServerUrlOverride
import com.dashlane.network.tools.CloudflareHeaderInterceptor
import com.dashlane.network.tools.MoreDetailedExceptionRequestInterceptor
import com.dashlane.network.webservices.DashlaneUrls.URL_API
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.ConnectivityCheck
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.DashlaneApiClient
import com.dashlane.server.api.DashlaneTime
import com.dashlane.server.api.LongPollingInterceptor
import com.dashlane.server.api.UserAgent
import com.dashlane.server.api.analytics.AnalyticsApi
import com.dashlane.server.api.analytics.AnalyticsApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpModule {

    @Singleton
    @Provides
    fun getOkHttpClient(userAgent: UserAgent, serverUrlOverride: ServerUrlOverride): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(MoreDetailedExceptionRequestInterceptor())
            .addInterceptor(userAgent.interceptor)
            .addInterceptor(LongPollingInterceptor())
            .addInterceptor(CloudflareHeaderInterceptor(serverUrlOverride))
            
            .addInterceptor(
                HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        Log.v("HTTP", message)
                    }
                }).apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    @Provides
    fun getHttpCallFactory(client: OkHttpClient): Call.Factory = client

    @Singleton
    @Provides
    fun getDashlaneApi(
        callFactory: Call.Factory,
        authorization: Authorization.App,
        connectivityCheck: ConnectivityCheck,
        serverUrlOverride: ServerUrlOverride
    ) =
        DashlaneApi(
            client = DashlaneApiClient(
                callFactory = callFactory,
                connectivityCheck = connectivityCheck,
                host = serverUrlOverride.apiUrl ?: URL_API
            ),
            appAuthorization = authorization,
            clock = SystemClockElapsedRealTime()
        )

    @Singleton
    @Provides
    fun getAnalyticsApi(
        dashlaneApi: DashlaneApi,
        okHttpClient: OkHttpClient,
        connectivityCheck: ConnectivityCheck,
        analyticsAuthorization: Authorization.Analytics
    ):
        AnalyticsApi {
        return AnalyticsApi(
            client = AnalyticsApiClient(
                callFactory = okHttpClient.newBuilder()
                    
                    
                    .readTimeout(3, TimeUnit.MINUTES)
                    .build(),
                connectivityCheck = connectivityCheck
            ),
            analyticsAuthorization = analyticsAuthorization,
            dashlaneApi = dashlaneApi
        )
    }

    @Provides
    fun getDashlaneTime(dashlaneApi: DashlaneApi): DashlaneTime =
        dashlaneApi.dashlaneTime

    private class SystemClockElapsedRealTime : SimpleClock(ZoneOffset.UTC) {
        override fun millis(): Long = android.os.SystemClock.elapsedRealtime()
    }

    abstract class SimpleClock(private val zone: ZoneId) : Clock() {

        override fun instant(): Instant = Instant.ofEpochMilli(millis())
        override fun getZone(): ZoneId = zone

        override fun withZone(zone: ZoneId): Clock =
            object : SimpleClock(zone) {
                override fun millis(): Long {
                    return this@SimpleClock.millis()
                }
            }

        abstract override fun millis(): Long
    }
}
