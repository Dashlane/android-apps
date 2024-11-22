package com.dashlane.dagger.singleton

import com.dashlane.session.BySessionRepository
import com.dashlane.session.repository.SessionCoroutineScopeProvider
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.util.inject.OptionalProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SessionCoroutineScopeModule {

    @Singleton
    @Binds
    fun bindSessionCoroutineScope(impl: SessionCoroutineScopeRepository): BySessionRepository<CoroutineScope>

    @Singleton
    @Binds
    fun bindSessionCoroutineScopeProvider(impl: SessionCoroutineScopeProvider): OptionalProvider<CoroutineScope>
}