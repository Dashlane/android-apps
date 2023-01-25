package com.dashlane.dagger

import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.MainImmediateCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @MainCoroutineDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher =
        Dispatchers.Main

    @Provides
    @MainImmediateCoroutineDispatcher
    fun provideMainImmediateCoroutineDispatcher(): CoroutineDispatcher =
        Dispatchers.Main.immediate

    @Provides
    @DefaultCoroutineDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher =
        Dispatchers.Default

    @Provides
    @IoCoroutineDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher =
        Dispatchers.IO
}