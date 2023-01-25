package com.dashlane.ui.activities.fragments.vault.dagger

import com.dashlane.ui.activities.fragments.vault.Vault
import com.dashlane.ui.activities.fragments.vault.VaultLogger
import com.dashlane.ui.activities.fragments.vault.list.VaultList
import com.dashlane.ui.activities.fragments.vault.list.VaultListDataProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class VaultListModule {
    @Binds
    abstract fun bindVaultListDataProvider(dataProvider: VaultListDataProvider): VaultList.DataProvider

    @Binds
    abstract fun bindVaultLogger(logger: VaultLogger): Vault.Logger
}