package com.dashlane.dagger.singleton;

import com.dashlane.network.inject.LegacyWebservicesApi;
import com.dashlane.sharing.service.SharingServiceNew;
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider;
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProviderImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;



@Module
public class SharingModule {

    @Provides
    public SharingServiceNew provideSharingServiceNew(@LegacyWebservicesApi Retrofit retrofit) {
        return retrofit.create(SharingServiceNew.class);
    }

    @Provides
    @Singleton
    public SharingDataProvider provideSharingDataProvider(SharingDataProviderImpl provider) {
        return provider;
    }
}
