package com.dashlane.wear.hilt

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class WearGsonModule {
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }
}