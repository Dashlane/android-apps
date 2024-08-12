package com.dashlane.dagger.singleton

import com.dashlane.breach.BreachService
import com.dashlane.breach.BreachServiceImpl
import com.dashlane.breach.GetFileBreachesService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
interface BreachModule {

    @Binds
    fun bindsBreachService(impl: BreachServiceImpl): BreachService

    companion object {

        @Provides
        fun provideGetFileBreachesService(retrofit: Retrofit): GetFileBreachesService =
            retrofit.create()
    }
}
