package com.dashlane.teamspaces.manager

import androidx.annotation.VisibleForTesting
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.teams.SpaceDeletedService
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.teamspaces.model.TeamSpace.Business.Past
import javax.inject.Inject

class SpaceDeletedNotifier @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val spaceDeletedService: SpaceDeletedService
) {
    suspend fun sendIfNeeded(session: Session) {
        val spaceIds = preferencesManager[session.username].getStringSet(PREF_SPACE_IDS)
        if (spaceIds.isNullOrEmpty()) {
            return
        }
        for (spaceId in spaceIds) {
            val spaceIdAsLong = spaceId.toLongOrNull()
            if (spaceIdAsLong == null) {
                continue
            }
            notifyDeleted(spaceIdAsLong, session.authorization)
        }
    }

    fun storeSpaceToDelete(spaceToDeleted: Past) {
        val preferences = preferencesManager[sessionManager.session?.username]
        val spaceId = spaceToDeleted.teamId
        var spaceIds: HashSet<String>? = preferences.getStringSet(PREF_SPACE_IDS)?.toHashSet()
        if (spaceIds == null) {
            spaceIds = HashSet()
        } else if (spaceIds.contains(spaceId)) {
            return 
        }
        spaceIds.add(spaceId)
        preferences.putStringSet(PREF_SPACE_IDS, spaceIds)
    }

    @VisibleForTesting
    suspend fun notifyDeleted(spaceId: Long, userAuthorization: Authorization.User) {
        if (spaceIdsSendInProgress.contains(spaceId)) {
            return
        }
        spaceIdsSendInProgress.add(spaceId)
        callDeleteSpace(spaceId, userAuthorization)
    }

    @VisibleForTesting
    suspend fun callDeleteSpace(spaceId: Long, userAuthorization: Authorization.User) {
        val result = runCatching {
            spaceDeletedService.execute(
                userAuthorization = userAuthorization,
                request = SpaceDeletedService.Request(
                    teamId = spaceId,
                )
            )
        }

        spaceIdsSendInProgress.remove(spaceId)
        if (result.isSuccess) {
            onSpaceDeleted(spaceId.toString())
        }
    }

    @VisibleForTesting
    fun onSpaceDeleted(spaceId: String?) {
        val preferences = preferencesManager[sessionManager.session?.username]
        val spaceIds: HashSet<String>? = preferences.getStringSet(PREF_SPACE_IDS)?.toHashSet()
        if (spaceId == null || spaceIds == null || !spaceIds.contains(spaceId)) {
            return 
        }
        spaceIds.remove(spaceId)
        preferences.putStringSet(PREF_SPACE_IDS, spaceIds)
    }

    companion object {
        private const val PREF_SPACE_IDS = "notifyItemsDeletedSpaceIds"
        private val spaceIdsSendInProgress: MutableSet<Long> = HashSet()
    }
}
