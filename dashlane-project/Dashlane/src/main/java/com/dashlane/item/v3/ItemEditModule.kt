package com.dashlane.item.v3

import com.dashlane.item.v3.repositories.CollectionsRepository
import com.dashlane.item.v3.repositories.CollectionsRepositoryImpl
import com.dashlane.item.v3.repositories.GeneratedPasswordRepository
import com.dashlane.item.v3.repositories.GeneratedPasswordRepositoryImpl
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.repositories.ItemEditRepositoryImpl
import com.dashlane.item.v3.repositories.NewItemRepository
import com.dashlane.item.v3.repositories.NewItemRepositoryImpl
import com.dashlane.item.v3.repositories.PasswordHealthRepository
import com.dashlane.item.v3.repositories.PasswordHealthRepositoryImpl
import com.dashlane.item.v3.util.ItemEditValueUpdateManager
import com.dashlane.item.v3.util.ItemEditValueUpdateManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface ItemEditModule {
    @Binds
    fun bindItemEditRepository(impl: ItemEditRepositoryImpl): ItemEditRepository

    @Binds
    fun bindItemEditValueUpdateManager(impl: ItemEditValueUpdateManagerImpl): ItemEditValueUpdateManager

    @Binds
    fun bindGeneratedPasswordRepository(impl: GeneratedPasswordRepositoryImpl): GeneratedPasswordRepository

    @Binds
    fun bindNewItemRepository(impl: NewItemRepositoryImpl): NewItemRepository

    @Binds
    fun bindCollectionsRepository(impl: CollectionsRepositoryImpl): CollectionsRepository

    @Binds
    fun bindPasswordHealthRepository(impl: PasswordHealthRepositoryImpl): PasswordHealthRepository
}