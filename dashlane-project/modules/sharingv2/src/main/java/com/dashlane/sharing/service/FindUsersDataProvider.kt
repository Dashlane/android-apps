package com.dashlane.sharing.service

import com.dashlane.session.Session
import com.dashlane.sharing.service.response.FindUsersResponse
import com.dashlane.util.JsonSerialization
import javax.inject.Inject

class FindUsersDataProvider @Inject constructor(
    private val sharingServiceNew: SharingServiceNew,
    private val jsonSerialization: JsonSerialization
) {
    companion object {
        private const val FIND_USER_BATCH_SIZE = 100
    }

    suspend fun findUsers(
        session: Session,
        aliases: Set<String>
    ): Map<String, FindUsersResponse.User> {
        val login = session.userId
        val uki = session.uki
        val result: List<MutableMap<String, FindUsersResponse.User>> =
            aliases.chunked(FIND_USER_BATCH_SIZE).mapNotNull { batch ->
                sharingServiceNew.findUsers(
                    login = login,
                    uki = uki,
                    aliases = ObjectToJson(batch, jsonSerialization)
                ).content
            }
        return result.reduce { acc, mutableMap ->
            acc.also { it.putAll(mutableMap) }
        }
    }
}