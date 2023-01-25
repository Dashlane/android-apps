package com.dashlane.network.inject

import android.content.Context
import com.dashlane.server.api.DashlaneTime
import com.dashlane.server.api.dagger.DashlaneApiComponent
import com.dashlane.util.inject.ApplicationComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit



interface RetrofitComponent : ApplicationComponent, DashlaneApiComponent {

    val okHttpClient: OkHttpClient

    @get:LegacyWebservicesApi
    val webservicesRetrofit: Retrofit

    @get:Streaming
    val streamingRetrofit: Retrofit

    val dashlaneTime: DashlaneTime

    companion object {

        

        operator fun invoke(context: Context) =
            (context.applicationContext as RetrofitApplication).component
    }
}