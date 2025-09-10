package com.dashlane.dagger.singleton

import com.dashlane.breach.GetFileBreachesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object BreachModule {
    @Provides
    fun provideGetFileBreachesService(retrofit: Retrofit): GetFileBreachesService = retrofit.create()
}