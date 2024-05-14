package com.dashlane.ui.activities.fragments.vault.dagger;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dashlane.storage.userdata.accessor.GenericDataQuery;
import com.dashlane.ui.activities.fragments.vault.Vault;
import com.dashlane.ui.activities.fragments.vault.VaultDataProvider;
import com.dashlane.ui.activities.fragments.vault.VaultPresenter;
import com.dashlane.ui.activities.fragments.vault.VaultViewModel;
import com.dashlane.ui.activities.fragments.vault.VaultViewModelFactory;

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
    abstract Vault.Presenter bindVaultPresenter(VaultPresenter presenter);

    @Provides
    static VaultViewModel provideVaultViewModel(Fragment fragment, GenericDataQuery genericDataQuery) {
        return new ViewModelProvider(fragment, new VaultViewModelFactory(genericDataQuery)).get(VaultViewModel.class);
    }
}