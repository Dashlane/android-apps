package com.dashlane.dagger.singleton

import com.dashlane.util.GsonJsonSerialization
import com.dashlane.util.JsonSerialization
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JsonModule {
    @Provides
    @Singleton
    fun provideJsonSerialization(): JsonSerialization = GsonJsonSerialization(Gson())

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()
}
