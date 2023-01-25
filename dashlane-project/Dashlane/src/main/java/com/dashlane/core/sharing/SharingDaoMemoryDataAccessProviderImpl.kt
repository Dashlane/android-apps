package com.dashlane.core.sharing

import com.dashlane.session.SessionManager
import com.dashlane.storage.DataStorageProvider
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.util.JsonSerialization
import javax.inject.Inject

class SharingDaoMemoryDataAccessProviderImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val jsonSerialization: JsonSerialization,
    private val provider: DataStorageProvider
) : SharingDaoMemoryDataAccessProvider {
    override suspend fun create(): SharingDaoMemoryDataAccess =
        if (provider.useRaclette) {
            createForRaclette()
        } else {
            createForLegacy()
        }.apply {
            init()
        }

    fun createForRaclette(): SharingDaoMemoryDataAccessRacletteImpl {
        return SharingDaoMemoryDataAccessRacletteImpl(
            sessionManager = sessionManager,
            userDataRepository = userDataRepository,
            delegate = createForLegacy(),
        )
    }

    private fun createForLegacy() =
        SharingDaoMemoryDataAccessImpl(sessionManager, userDataRepository, jsonSerialization)
}