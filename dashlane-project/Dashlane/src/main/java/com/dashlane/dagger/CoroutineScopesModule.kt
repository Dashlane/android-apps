package com.dashlane.dagger

import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@InstallIn(SingletonComponent::class)
@Module
object CoroutineScopesModule {

    @Singleton
    @ApplicationCoroutineScope
    @Provides
    fun providesCoroutineScope(
        @DefaultCoroutineDispatcher defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
}
