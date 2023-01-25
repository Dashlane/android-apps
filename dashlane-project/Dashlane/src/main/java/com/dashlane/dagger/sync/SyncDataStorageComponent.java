package com.dashlane.dagger.sync;

import com.dashlane.storage.DataStorageProvider;
import com.dashlane.sync.sharing.SharingSync;

public interface SyncDataStorageComponent {
    SharingSync getSharingSync();

    DataStorageProvider getDataStorageProvider();
}
