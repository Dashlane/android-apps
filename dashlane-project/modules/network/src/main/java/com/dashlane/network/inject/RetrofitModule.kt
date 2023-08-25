package com.dashlane.network.inject

import com.dashlane.network.webservices.DashlaneUrls
import dagger.Module
import dagger.Provides
import okhttp3.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class RetrofitModule {

    @Singleton
    @Provides
    @LegacyWebservicesApi
    fun getRetrofit(callFactory: Call.Factory): Retrofit =
        createRetrofit(DashlaneUrls.URL_WEBSERVICES, callFactory)

    @Provides
    @Streaming
    @Singleton
    fun getStreamingRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DashlaneUrls.URL_WEBSERVICES)
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