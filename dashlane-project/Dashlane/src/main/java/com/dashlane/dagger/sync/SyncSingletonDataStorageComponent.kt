package com.dashlane.dagger.sync

import com.dashlane.storage.DataStorageProvider
import com.dashlane.core.sharing.SharingDaoMemoryDataAccessProvider
import com.dashlane.sync.sharing.SharingSync

interface SyncSingletonDataStorageComponent {
    val sharingSync: SharingSync
    val dataStorageProvider: DataStorageProvider
    val sharingDaoMemoryDataAccessProvider: SharingDaoMemoryDataAccessProvider
}