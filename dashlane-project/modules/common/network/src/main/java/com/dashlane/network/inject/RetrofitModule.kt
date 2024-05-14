package com.dashlane.network.inject

import com.dashlane.network.ServerUrlOverride
import com.dashlane.network.webservices.DashlaneUrls
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Singleton
    @Provides
    @LegacyWebservicesApi
    fun getRetrofit(callFactory: Call.Factory, serverUrlOverride: ServerUrlOverride): Retrofit =
        createRetrofit(serverUrlOverride.serverUrl ?: DashlaneUrls.URL_WEBSERVICES, callFactory)

    @Provides
    @Streaming
    @Singleton
    fun getStreamingRetrofit(serverUrlOverride: ServerUrlOverride): Retrofit {
        return Retrofit.Builder()
            .baseUrl(serverUrlOverride.serverUrl ?: DashlaneUrls.URL_WEBSERVICES)
            .build()
    }

    companion object {

        @JvmStatic
        fun createRetrofit(
            baseUrl: String,
            callFactory: Call.Factory,
            vararg extraCallAdapterFactories: CallAdapter.Factory
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .callFactory(callFactory)
                .apply {
                    extraCallAdapterFactories.forEach { addCallAdapterFactory(it) }
                }
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}

@Qualifier
annotation class LegacyWebservicesApi

@Qualifier
annotation class Streaming