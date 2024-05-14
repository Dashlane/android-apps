package com.dashlane.dagger.singleton

import com.dashlane.BuildConfig
import com.dashlane.server.api.Authorization.Analytics
import com.dashlane.server.api.Authorization.App
import com.dashlane.server.api.UserAgent
import com.dashlane.server.api.UserAgentProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HttpAppModule {

    @Binds
    abstract fun bindsUserAgentProvider(userAgentProvider: UserAgentProviderImpl): UserAgentProvider

    companion object {
        @Provides
        fun provideUserAgent(userAgentProvider: UserAgentProvider): UserAgent = UserAgent(userAgentProvider)

        @Provides
        fun provideAppAuthorization(): App = App(
            accessToken = BuildConfig.API_ACCESS_KEY,
            signatureKey = BuildConfig.API_SECRET_KEY
        )

        @Provides
        fun provideAnalyticsAuthorization(): Analytics = Analytics(
            accessToken = BuildConfig.ANALYTICS_ACCESS_KEY,
            signatureKey = BuildConfig.ANALYTICS_SECRET_KEY
        )
    }
}