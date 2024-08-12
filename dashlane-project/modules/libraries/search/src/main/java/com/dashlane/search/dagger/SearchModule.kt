package com.dashlane.search.dagger

import com.dashlane.search.SearchSorter
import com.dashlane.search.utils.RankingSearchSorter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SearchModule {

    @Binds
    fun bindsSearchSorter(impl: RankingSearchSorter): SearchSorter
}