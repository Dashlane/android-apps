package com.dashlane.dagger.singleton

import android.content.res.AssetManager
import com.dashlane.url.registry.UrlDomainRegistryFactory
import com.dashlane.url.registry.UrlDomainRegistryFiles
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent



@Module
@InstallIn(SingletonComponent::class)
object DomainCategoryModule {
    @Provides
    fun provideUrlDomainRegistryFactory(files: UrlDomainRegistryFiles): UrlDomainRegistryFactory =
        UrlDomainRegistryFactory(files)

    @Provides
    fun provideUrlDomainRegistryFiles(assetManager: AssetManager): UrlDomainRegistryFiles =
        UrlDomainRegistryFiles(assetManager)
}