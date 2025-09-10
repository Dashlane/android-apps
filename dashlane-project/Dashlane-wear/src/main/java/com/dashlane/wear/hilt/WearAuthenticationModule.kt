package com.dashlane.wear.hilt

import android.content.Context
import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.authentication.login.AuthenticationDeviceRepository
import com.dashlane.authentication.login.AuthenticationDeviceRepositoryImpl
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.session.repository.UserDataRepositoryImpl
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.session.repository.UserDatabaseRepositoryImpl
import com.dashlane.util.DeviceUtils
import com.dashlane.util.getOsLang
import com.dashlane.util.tryOrNull
import com.dashlane.utils.android.R
import com.dashlane.wear.BuildConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WearAuthenticationModule {
    @Binds
    fun bindsUserDataRepository(impl: UserDataRepositoryImpl): UserDataRepository

    @Binds
    fun bindsUserDatabaseRepository(impl: UserDatabaseRepositoryImpl): UserDatabaseRepository

    @Binds
    fun bindsAuthenticationDeviceRepository(impl: AuthenticationDeviceRepositoryImpl): AuthenticationDeviceRepository

    companion object {
        @Provides
        fun providesDeviceRegistrationInfo(
            @ApplicationContext
            context: Context,
            deviceInfoRepository: DeviceInfoRepository,
        ) = object : DeviceRegistrationInfo {
            override val appVersion: String
                get() = BuildConfig.VERSION_NAME

            override val installOrigin: String?
                get() = null

            override val deviceName: String
                get() = deviceInfoRepository.deviceName

            override val country: String
                get() = deviceInfoRepository.deviceCountry

            override val osCountry: String
                get() = DeviceUtils.getDeviceCountry(context)

            override val language: String
                get() = tryOrNull { context.getString(R.string.language_iso_639_1) } ?: "EN"

            override val osLanguage: String
                get() = getOsLang()
        }
    }
}