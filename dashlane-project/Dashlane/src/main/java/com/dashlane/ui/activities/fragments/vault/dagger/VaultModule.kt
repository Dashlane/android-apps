package com.dashlane.ui.activities.fragments.vault.dagger

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.ui.activities.fragments.vault.Vault
import com.dashlane.ui.activities.fragments.vault.VaultDataProvider
import com.dashlane.ui.activities.fragments.vault.VaultPresenter
import com.dashlane.ui.activities.fragments.vault.VaultViewModel
import com.dashlane.ui.activities.fragments.vault.VaultViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
interface VaultModule {
    @Binds
    fun bindVaultDataProvider(dataProvider: VaultDataProvider): Vault.DataProvider

    @Binds
    fun bindVaultPresenter(presenter: VaultPresenter): Vault.Presenter

    companion object {

        @Provides
        fun provideVaultViewModel(fragment: Fragment, genericDataQuery: GenericDataQuery): VaultViewModel =
            ViewModelProvider(fragment, VaultViewModelFactory(genericDataQuery))[VaultViewModel::class.java]
    }
}