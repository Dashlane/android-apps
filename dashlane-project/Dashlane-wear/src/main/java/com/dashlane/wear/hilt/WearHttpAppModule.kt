package com.dashlane.wear.hilt

import android.os.Build
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.UserAgent
import com.dashlane.server.api.UserAgent.Companion.PARTNER
import com.dashlane.server.api.UserAgent.Companion.PLATFORM
import com.dashlane.server.api.UserAgentProvider
import com.dashlane.util.getOsLang
import com.dashlane.wear.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WearHttpAppModule {
    @Provides
    fun providesUserAgentProvider() = object : UserAgentProvider {
        override val appVersionName: String
            get() = BuildConfig.VERSION_NAME
        override val osVersion: String
            get() = Build.VERSION.RELEASE
        override val partner: String
            get() = PARTNER
        override val platform: String
            get() = PLATFORM
        override val language: String
            get() = getOsLang()
    }

    @Provides
    fun provideUserAgent(userAgentProvider: UserAgentProvider): UserAgent = UserAgent(userAgentProvider)

    @Provides
    fun provideAppAuthorization(): Authorization.App = Authorization.App(
        accessToken = BuildConfig.WEAR_API_ACCESS_KEY,
        signatureKey = BuildConfig.WEAR_API_SECRET_KEY,
    )

    @Provides
    fun provideAnalyticsAuthorization(): Authorization.Analytics = Authorization.Analytics(
        accessToken = BuildConfig.WEAR_ANALYTICS_ACCESS_KEY,
        signatureKey = BuildConfig.WEAR_ANALYTICS_SECRET_KEY,
    )
}