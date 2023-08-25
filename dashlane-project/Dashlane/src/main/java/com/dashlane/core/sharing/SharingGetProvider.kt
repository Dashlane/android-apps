package com.dashlane.core.sharing

import com.dashlane.server.api.Authorization
import com.dashlane.sharing.SharingSyncCommunicator
import com.dashlane.sync.sharing.SharingSync
import javax.inject.Inject

class SharingGetProvider @Inject constructor(
    private val sharingCommunicator: SharingSyncCommunicator,
) {
    suspend fun requestUpdate(
        session: Authorization.User,
        idsToRequest: SharingSync.IdCollection
    ): SharingSyncCommunicator.GetSharingResult {
        if (idsToRequest.isEmpty) {
            return SharingSyncCommunicator.GetSharingResult(
            emptyList(),
            emptyList(),
            emptyList()
        )
        }
        val result: List<SharingSyncCommunicator.GetSharingResult> =
            idsToRequest.chunked().map {
                sharingCommunicator.get(
                    session,
                    itemUids = it.items,
                    itemGroupUids = it.itemGroups,
                    userGroupUids = it.userGroups
                )
            }
        return result.reduce { acc, e ->
            acc.copy(
                itemGroups = acc.itemGroups + e.itemGroups,
                userGroups = acc.userGroups + e.userGroups,
                itemContents = acc.itemContents + e.itemContents
            )
        }
    }
}
