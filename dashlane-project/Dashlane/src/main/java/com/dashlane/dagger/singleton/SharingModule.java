package com.dashlane.dagger.singleton;

import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider;
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProviderImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SharingModule {

    @Provides
    @Singleton
    public SharingDataProvider provideSharingDataProvider(SharingDataProviderImpl provider) {
        return provider;
    }
}
