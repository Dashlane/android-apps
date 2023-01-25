package com.dashlane.dagger.sync

import com.dashlane.dagger.singleton.SingletonProvider

object SyncDataStorageComponentProvider {
    fun getForDefault(): SyncDataStorageComponent {
        return SyncDataStorageDefaultComponent.ComponentProvider.get(SingletonProvider.getComponent())
    }

    fun getForRaclette(): SyncDataStorageComponent {
        return SyncDataStorageRacletteComponent.ComponentProvider.get(SingletonProvider.getComponent())
    }
}