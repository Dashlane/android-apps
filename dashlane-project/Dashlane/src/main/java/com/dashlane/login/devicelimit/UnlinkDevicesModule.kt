package com.dashlane.login.devicelimit

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
interface UnlinkDevicesModule {
    @Binds
    fun bindPresenter(impl: UnlinkDevicesPresenter): UnlinkDevicesContract.Presenter
}