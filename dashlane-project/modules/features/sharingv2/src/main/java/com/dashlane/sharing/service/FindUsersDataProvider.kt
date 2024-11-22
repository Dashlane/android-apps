package com.dashlane.sharing.service

import com.dashlane.session.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import com.dashlane.session.Session
import javax.inject.Inject

class FindUsersDataProvider @Inject constructor(
    private val getUsersPublicKeyService: GetUsersPublicKeyService
) {
    companion object {
        private const val FIND_USER_BATCH_SIZE = 100
    }

    suspend fun findUsers(
        session: Session,
        aliases: List<String>
    ): List<GetUsersPublicKeyService.Data.Data> {
        return aliases.chunked(FIND_USER_BATCH_SIZE).mapNotNull { batch ->
            runCatching {
                val response = getUsersPublicKeyService.execute(
                    session.authorization,
                    GetUsersPublicKeyService.Request(batch)
                )
                response.data.data
            }.getOrNull()
        }.flatten()
    }
}