package com.dashlane.dagger.singleton

import com.dashlane.abtesting.RemoteAbTestManager
import com.dashlane.featureflipping.FeatureFlipManager
import com.dashlane.session.RemoteConfiguration
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
interface RemoteConfigurationModule {

    @Binds
    @IntoSet
    fun bindRemoteAbTestManager(remoteAbTestManager: RemoteAbTestManager): RemoteConfiguration

    @Binds
    @IntoSet
    fun bindFeatureFlipManager(featureFlipManager: FeatureFlipManager): RemoteConfiguration
}