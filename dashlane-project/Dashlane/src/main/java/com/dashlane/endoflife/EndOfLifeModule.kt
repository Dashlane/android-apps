package com.dashlane.endoflife

import com.dashlane.ui.endoflife.EndOfLife
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EndOfLifeModule {

    @Singleton
    @Binds
    abstract fun bindEndOfLife(endOfLife: EndOfLifeObserver): EndOfLife
}
