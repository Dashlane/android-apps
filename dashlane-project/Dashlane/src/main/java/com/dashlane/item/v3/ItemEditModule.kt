package com.dashlane.item.v3

import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
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
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
interface ItemEditModule {
    @Binds
    fun bindItemEditRepository(impl: ItemEditRepositoryImpl): ItemEditRepository

    @Binds
    fun bindGeneratedPasswordRepository(impl: GeneratedPasswordRepositoryImpl): GeneratedPasswordRepository

    @Binds
    fun bindNewItemRepository(impl: NewItemRepositoryImpl): NewItemRepository

    @Binds
    fun bindCollectionsRepository(impl: CollectionsRepositoryImpl): CollectionsRepository

    @Binds
    fun bindPasswordHealthRepository(impl: PasswordHealthRepositoryImpl): PasswordHealthRepository

    companion object {
        @Provides
        @ViewModelScoped
        fun provideWifiManager(
            @ApplicationContext context: Context,
        ): WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        @Provides
        @ViewModelScoped
        fun provideLocationManager(
            @ApplicationContext context: Context,
        ): LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
}