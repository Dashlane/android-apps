package com.dashlane.network.inject

import com.dashlane.network.ServerUrlOverride
import com.dashlane.network.webservices.DashlaneUrls
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun getStreamingRetrofit(serverUrlOverride: ServerUrlOverride): Retrofit {
        return Retrofit.Builder()
            .baseUrl(serverUrlOverride.serverUrl ?: DashlaneUrls.URL_API)
            .build()
    }
}
