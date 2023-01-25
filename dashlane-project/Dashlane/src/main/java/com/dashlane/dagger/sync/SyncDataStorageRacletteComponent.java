package com.dashlane.dagger.sync;

import com.dashlane.cryptography.CryptographyComponent;
import com.dashlane.cryptography.SharingCryptographyComponent;
import com.dashlane.dagger.CoroutinesModule;
import com.dashlane.dagger.singleton.SharingModule;
import com.dashlane.dagger.singleton.SingletonComponentProxy;
import com.dashlane.network.inject.RetrofitComponent;
import com.dashlane.preference.DashlanePreferencesComponent;
import com.dashlane.server.api.dagger.DashlaneApiEndpointsModule;
import com.dashlane.sharing.SharingKeysHelperComponent;
import com.dashlane.storage.userdata.accessor.injection.DataAccessComponent;
import com.dashlane.ui.component.UiPartComponent;

import dagger.Component;

@Component(
        modules = {
                SyncDataStorageModule.class,
                DashlaneApiEndpointsModule.class,
                CoroutinesModule.class,
                SharingModule.class
        },
        dependencies = {
                SyncSingletonComponent.class,
                DashlanePreferencesComponent.class,
                DataAccessComponent.class,
                UiPartComponent.class,
                CryptographyComponent.class,
                SharingCryptographyComponent.class,
                SharingKeysHelperComponent.class,
                RetrofitComponent.class 
        }
)
public interface SyncDataStorageRacletteComponent extends SyncDataStorageComponent {
    final class ComponentProvider {
        private ComponentProvider() {
            throw new UnsupportedOperationException();
        }

        public static SyncDataStorageRacletteComponent get(SingletonComponentProxy singletonComponent) {
            return DaggerSyncDataStorageRacletteComponent.builder()
                    .syncSingletonComponent(singletonComponent)
                    .dashlanePreferencesComponent(singletonComponent)
                    .dataAccessComponent(singletonComponent)
                    .uiPartComponent(singletonComponent)
                    .cryptographyComponent(singletonComponent)
                    .sharingCryptographyComponent(singletonComponent)
                    .sharingKeysHelperComponent(singletonComponent)
                    .retrofitComponent(singletonComponent)
                    .build();
        }
    }
}
