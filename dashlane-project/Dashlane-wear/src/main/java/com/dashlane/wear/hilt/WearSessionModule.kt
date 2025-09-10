package com.dashlane.wear.hilt

import com.dashlane.abtesting.RemoteConfiguration
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionManagerImpl
import com.dashlane.session.SessionRestorer
import com.dashlane.session.SessionTrasher
import com.dashlane.session.SessionTrasherImpl
import com.dashlane.session.restorer.SessionRestorerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WearSessionModule {
    @Binds
    fun bindsSessionManager(impl: SessionManagerImpl): SessionManager

    @Binds
    fun bindsSessionInitializer(impl: SessionManagerImpl): SessionInitializer

    @Binds
    fun bindsSessionTrash(impl: SessionTrasherImpl): SessionTrasher

    @Binds
    fun bindsSessionRestorer(impl: SessionRestorerImpl): SessionRestorer

    companion object {
        @Provides
        fun providesRemoteConfiguration() = emptySet<RemoteConfiguration>()
    }
}