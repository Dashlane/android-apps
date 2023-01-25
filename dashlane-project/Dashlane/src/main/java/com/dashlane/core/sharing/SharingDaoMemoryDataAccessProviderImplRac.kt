package com.dashlane.core.sharing

import javax.inject.Inject

class SharingDaoMemoryDataAccessProviderImplRac @Inject constructor(
    private val provider: SharingDaoMemoryDataAccessProviderImpl
) : SharingDaoMemoryDataAccessProvider {
    override suspend fun create(): SharingDaoMemoryDataAccess {
        return provider.createForRaclette().apply { init() }
    }
}