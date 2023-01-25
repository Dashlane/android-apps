package com.dashlane.ui.activities.fragments.vault.dagger;

import com.dashlane.storage.DataStorageProvider;
import com.dashlane.ui.activities.fragments.vault.Vault;
import com.dashlane.ui.activities.fragments.vault.VaultDataProvider;
import com.dashlane.ui.activities.fragments.vault.VaultLogger;
import com.dashlane.ui.activities.fragments.vault.VaultPresenter;
import com.dashlane.ui.activities.fragments.vault.VaultViewModel;
import com.dashlane.ui.activities.fragments.vault.VaultViewModelFactory;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.FragmentComponent;

@Module
@InstallIn(FragmentComponent.class)
public abstract class VaultModule {
    @Binds
    abstract Vault.DataProvider bindVaultDataProvider(VaultDataProvider dataProvider);

    @Binds
    abstract Vault.Logger bindVaultLogger(VaultLogger logger);

    @Binds
    abstract Vault.Presenter bindVaultPresenter(VaultPresenter presenter);

    @Provides
    static VaultViewModel provideVaultViewModel(Fragment fragment, DataStorageProvider dataStorageProvider) {
        return new ViewModelProvider(fragment, new VaultViewModelFactory(dataStorageProvider)).get(VaultViewModel.class);
    }
}