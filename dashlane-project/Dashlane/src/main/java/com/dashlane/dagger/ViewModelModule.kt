package com.dashlane.dagger

import android.content.Context
import com.dashlane.session.authorization
import com.dashlane.server.api.DashlaneApi
import com.dashlane.session.SessionManager
import com.dashlane.url.icon.v2.UrlDomainIconDataStore
import com.dashlane.url.icon.v2.UrlDomainIconDatabase
import com.dashlane.url.icon.v2.UrlDomainIconRepository
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @ViewModelScoped
    @Provides
    fun provideUrlDomainIconV2Repository(
        @ApplicationCoroutineScope applicationCoroutineScope: CoroutineScope,
        @ApplicationContext context: Context,
        @IoCoroutineDispatcher ioDispatcher: CoroutineDispatcher,
        dashlaneApi: DashlaneApi,
        sessionManager: SessionManager,
    ): UrlDomainIconRepository {
        return UrlDomainIconRepository(
            coroutineScope = applicationCoroutineScope,
            dashlaneTime = dashlaneApi.dashlaneTime,
            dataStore = UrlDomainIconDataStore(UrlDomainIconDatabase.invoke(context)),
            getIconService = dashlaneApi.endpoints.icons.getIconService,
            requestIconService = dashlaneApi.endpoints.icons.requestIconService,
            ioDispatcher = ioDispatcher,
            userAuthorizationProvider = {
                requireNotNull(sessionManager.session?.authorization)
            }
        )
    }
}