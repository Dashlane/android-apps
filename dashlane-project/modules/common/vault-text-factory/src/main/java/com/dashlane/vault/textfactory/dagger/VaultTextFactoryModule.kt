package com.dashlane.vault.textfactory.dagger

import com.dashlane.vault.textfactory.identity.IdentityNameHolderService
import com.dashlane.vault.textfactory.identity.IdentityNameHolderServiceImpl
import com.dashlane.vault.textfactory.list.DataIdentifierListTextResolver
import com.dashlane.vault.textfactory.list.DataIdentifierListTextResolverImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface VaultTextFactoryModule {

    @Binds
    fun bindsDataIdentifierListTextResolver(impl: DataIdentifierListTextResolverImpl): DataIdentifierListTextResolver

    @Binds
    fun bindsIdentityNameHolderService(impl: IdentityNameHolderServiceImpl): IdentityNameHolderService
}