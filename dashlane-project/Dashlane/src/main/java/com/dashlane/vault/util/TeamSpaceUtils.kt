package com.dashlane.vault.util

import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isValueNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.isSpaceItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

object TeamSpaceUtils {

    @JvmStatic
    fun getTeamSpaceId(item: VaultItem<*>): String {
        return getTeamSpaceIdOrDefault(item.syncObject.spaceId)
    }

    @JvmStatic
    fun getTeamSpaceId(item: SyncObject?): String {
        return getTeamSpaceIdOrDefault(item?.spaceId)
    }

    @JvmStatic
    fun getTeamSpaceId(item: SummaryObject?): String {
        return getTeamSpaceIdOrDefault(item?.spaceId)
    }

    private fun getTeamSpaceIdOrDefault(spaceId: String?): String =
        spaceId?.takeUnless { it.isValueNull() }
            ?: TeamspaceManager.PERSONAL_TEAMSPACE.teamId
}

fun TeamspaceAccessor.hasValidSpace(item: SummaryObject): Boolean =
    get(TeamSpaceUtils.getTeamSpaceId(item)) != null

fun VaultItem<*>.getTeamSpaceLog() = if (isSpaceItem() &&
    TeamSpaceUtils.getTeamSpaceId(this).isNotSemanticallyNull()
) {
    Space.PROFESSIONAL
} else {
    Space.PERSONAL
}
