package com.dashlane.common.app.hilt

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MoshiProvider {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()
}