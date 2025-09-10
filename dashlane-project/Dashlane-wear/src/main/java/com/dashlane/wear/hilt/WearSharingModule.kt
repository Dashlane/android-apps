package com.dashlane.wear.hilt

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sync.GetLatestContentService
import com.dashlane.sharing.SharingKeysHelper
import com.dashlane.sharing.SharingKeysHelperImpl
import com.dashlane.sync.sharing.SharingSync
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
interface WearSharingModule {
    @Binds
    fun bindsSharingKeysHelper(impl: SharingKeysHelperImpl): SharingKeysHelper

    @Binds
    fun bindsSharingSync(impl: SharingSyncTemp): SharingSync
}

class SharingSyncTemp @Inject constructor() : SharingSync {
    override suspend fun syncSharing(session: Authorization.User, sharingSummary: GetLatestContentService.Data.SharingSummary) = Unit
}