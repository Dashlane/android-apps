package com.dashlane.dagger.singleton

import com.dashlane.session.SessionManager
import com.dashlane.session.SessionManagerImpl
import com.dashlane.session.SessionRestorer
import com.dashlane.session.SessionTrasher
import com.dashlane.session.SessionTrasherImpl
import com.dashlane.session.restorer.SessionRestorerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SessionManagerModule {

    @Singleton
    @Binds
    fun bindSessionManager(impl: SessionManagerImpl): SessionManager

    @Singleton
    @Binds
    fun bindSessionRestorer(impl: SessionRestorerImpl): SessionRestorer

    @Binds
    fun bindsSessionTrasher(impl: SessionTrasherImpl): SessionTrasher
}