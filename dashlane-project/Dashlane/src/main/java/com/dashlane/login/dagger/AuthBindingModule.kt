package com.dashlane.login.dagger

import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.authentication.RemoteKeyFactory
import com.dashlane.authentication.RemoteKeyFactoryImpl
import com.dashlane.authentication.SsoServerKeyFactory
import com.dashlane.authentication.SsoServerKeyFactoryImpl
import com.dashlane.authentication.UuidFactory
import com.dashlane.authentication.UuidFactoryImpl
import com.dashlane.login.DeviceRegistrationInfoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface AuthBindingModule {
    @Binds
    fun bindDeviceRegistrationInfo(impl: DeviceRegistrationInfoImpl): DeviceRegistrationInfo

    @Binds
    fun bindUuidFactory(impl: UuidFactoryImpl): UuidFactory

    @Binds
    fun bindSsoServerKeyFactory(impl: SsoServerKeyFactoryImpl): SsoServerKeyFactory

    @Binds
    fun bindRemoteKeyFactory(impl: RemoteKeyFactoryImpl): RemoteKeyFactory
}
