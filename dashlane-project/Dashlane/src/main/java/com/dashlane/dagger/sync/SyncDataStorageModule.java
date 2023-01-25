package com.dashlane.dagger.sync;

import com.dashlane.core.sharing.SharingDaoMemoryDataAccessProvider;
import com.dashlane.core.sharing.SharingDaoMemoryDataAccessProviderImplRac;
import com.dashlane.core.sharing.SharingSyncImpl;
import com.dashlane.storage.DataStorageProvider;
import com.dashlane.storage.DataStorageProviderImplRac;
import com.dashlane.sync.sharing.SharingSync;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class SyncDataStorageModule {
    @Binds
    abstract SharingDaoMemoryDataAccessProvider bindSharingKeysHelper(SharingDaoMemoryDataAccessProviderImplRac object);

    @Binds
    abstract DataStorageProvider bindDataStorageProvider(DataStorageProviderImplRac object);

    @Binds
    abstract SharingSync bindSharingSync(SharingSyncImpl object);
}
