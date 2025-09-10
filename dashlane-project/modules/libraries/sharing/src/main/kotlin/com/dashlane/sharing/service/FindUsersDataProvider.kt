package com.dashlane.sharing.service

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import javax.inject.Inject

class FindUsersDataProvider @Inject constructor(
    private val getUsersPublicKeyService: GetUsersPublicKeyService,
) {
    companion object {
        private const val FIND_USER_BATCH_SIZE = 100
    }

    suspend fun findUsers(
        authorization: Authorization.User,
        aliases: List<String>,
    ): List<GetUsersPublicKeyService.Data.Data> {
        return aliases.chunked(FIND_USER_BATCH_SIZE).mapNotNull { batch ->
            runCatching {
                val response = getUsersPublicKeyService.execute(
                    authorization,
                    GetUsersPublicKeyService.Request(batch),
                )
                response.data.data
            }.getOrNull()
        }.flatten()
    }
}