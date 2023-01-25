package com.dashlane.dagger.sync;

import com.dashlane.cryptography.CryptographyComponent;
import com.dashlane.cryptography.SharingCryptographyComponent;
import com.dashlane.dagger.CoroutinesModule;
import com.dashlane.dagger.singleton.SharingModule;
import com.dashlane.dagger.singleton.SingletonComponentProxy;
import com.dashlane.preference.DashlanePreferencesComponent;
import com.dashlane.sharing.SharingKeysHelperComponent;
import com.dashlane.storage.userdata.accessor.injection.DataAccessComponent;
import com.dashlane.ui.component.UiPartComponent;
import com.dashlane.util.inject.ApplicationComponent;

import dagger.Component;

@Component(
        modules = {
                CoroutinesModule.class,
                SharingModule.class
        },
        dependencies = {
                SyncSingletonComponent.class,
                SyncSingletonDataStorageComponent.class,
                ApplicationComponent.class,
                DashlanePreferencesComponent.class,
                DataAccessComponent.class,
                UiPartComponent.class,
                CryptographyComponent.class,
                SharingCryptographyComponent.class,
                SharingKeysHelperComponent.class})
public interface SyncDataStorageDefaultComponent extends SyncDataStorageComponent {
    final class ComponentProvider {
        private ComponentProvider() {
            throw new UnsupportedOperationException();
        }

        public static SyncDataStorageDefaultComponent get(SingletonComponentProxy singletonComponent) {
            return DaggerSyncDataStorageDefaultComponent.builder()
                                          .syncSingletonComponent(singletonComponent)
                                          .syncSingletonDataStorageComponent(singletonComponent)
                                          .applicationComponent(singletonComponent)
                                          .cryptographyComponent(singletonComponent)
                                          .sharingCryptographyComponent(singletonComponent)
                                          .sharingKeysHelperComponent(singletonComponent)
                                          .applicationComponent(singletonComponent)
                                          .uiPartComponent(singletonComponent)
                                          .dataAccessComponent(singletonComponent)
                                          .dashlanePreferencesComponent(singletonComponent)
                                          .build();
        }
    }
}
