package com.dashlane.common.app.hilt

import com.dashlane.device.DeviceInfoRepository
import com.dashlane.device.DeviceInfoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
fun interface DeviceInfoModule {
    @Binds
    fun bindDeviceIdRepository(impl: DeviceInfoRepositoryImpl): DeviceInfoRepository
}