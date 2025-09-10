package com.dashlane.wear.hilt

import com.dashlane.navigation.NavigationUriBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class WearNavigationModule {
    @Provides
    fun provideNavigationUriBuilder() = NavigationUriBuilder()
}