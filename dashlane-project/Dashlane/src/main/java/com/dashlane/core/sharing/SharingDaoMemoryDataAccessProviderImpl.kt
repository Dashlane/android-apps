package com.dashlane.core.sharing

import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import javax.inject.Inject

class SharingDaoMemoryDataAccessProviderImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository
) : SharingDaoMemoryDataAccessProvider {
    override suspend fun create(): SharingDaoMemoryDataAccess =
        createForRaclette()
            .apply {
                init()
            }

    private fun createForRaclette(): SharingDaoMemoryDataAccessRacletteImpl {
        return SharingDaoMemoryDataAccessRacletteImpl(
            sessionManager = sessionManager,
            userDataRepository = userDataRepository,
            delegate = SharingDaoMemoryDataAccessImpl(),
        )
    }
}