package com.dashlane.dagger.singleton

import com.dashlane.session.SessionManager
import com.dashlane.session.SessionManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionManagerModule {

    @Singleton
    @Binds
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager
}