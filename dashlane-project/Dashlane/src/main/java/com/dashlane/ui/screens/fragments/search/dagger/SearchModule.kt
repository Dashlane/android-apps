package com.dashlane.ui.screens.fragments.search.dagger

import com.dashlane.ui.screens.fragments.search.SearchService
import com.dashlane.ui.screens.fragments.search.SearchServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SearchModule {

    @Binds
    fun bindSearchService(impl: SearchServiceImpl): SearchService
}