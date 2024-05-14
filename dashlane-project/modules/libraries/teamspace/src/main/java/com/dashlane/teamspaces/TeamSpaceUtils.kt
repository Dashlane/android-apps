package com.dashlane.teamspaces

import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isValueNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.SyncObjectTypeUtils

object TeamSpaceUtils {

    fun getTeamSpaceId(item: VaultItem<*>): String? {
        return getTeamSpaceIdOrDefault(item.syncObject.spaceId)
    }

    fun getTeamSpaceId(item: SyncObject?): String? {
        return getTeamSpaceIdOrDefault(item?.spaceId)
    }

    fun getTeamSpaceId(item: SummaryObject?): String? {
        return getTeamSpaceIdOrDefault(item?.spaceId)
    }

    private fun getTeamSpaceIdOrDefault(spaceId: String?): String? = spaceId?.takeUnless { it.isValueNull() }
}

val SyncObjectType.isSpaceSupported: Boolean
    get() {
        return this in SyncObjectTypeUtils.WITH_TEAMSPACES
    }

fun VaultItem<*>.isSpaceItem() = syncObjectType.isSpaceSupported
fun SummaryObject.isSpaceItem() = syncObjectType.isSpaceSupported

fun TeamSpaceAccessor.hasValidSpace(item: SummaryObject): Boolean = get(TeamSpaceUtils.getTeamSpaceId(item)) != null

fun VaultItem<*>.getTeamSpaceLog() = if (isSpaceItem() &&
    TeamSpaceUtils.getTeamSpaceId(this).isNotSemanticallyNull()
) {
    Space.PROFESSIONAL
} else {
    Space.PERSONAL
}