package com.dashlane.storage.userdata.accessor.filter

import com.dashlane.storage.userdata.accessor.filter.lock.DefaultLockFilter
import com.dashlane.storage.userdata.accessor.filter.lock.EditableLockFilter
import com.dashlane.storage.userdata.accessor.filter.lock.LockFilter
import com.dashlane.storage.userdata.accessor.filter.space.EditableSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.space.SpaceFilter
import com.dashlane.storage.userdata.accessor.filter.status.DefaultStatusFilter
import com.dashlane.storage.userdata.accessor.filter.status.EditableStatusFilter
import com.dashlane.storage.userdata.accessor.filter.status.StatusFilter
import com.dashlane.storage.userdata.accessor.filter.uid.EditableUidFilter
import com.dashlane.storage.userdata.accessor.filter.uid.NoUidFilter
import com.dashlane.storage.userdata.accessor.filter.uid.UidFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.vault.summary.CollectionVaultItems
import com.dashlane.xml.domain.SyncObjectType

class CollectionFilter(
    override var spaceFilter: SpaceFilter = NoSpaceFilter,
    override var uidFilter: UidFilter = NoUidFilter,
    override var lockFilter: LockFilter = DefaultLockFilter,
    override var statusFilter: StatusFilter = DefaultStatusFilter
) : BaseFilter, EditableSpaceFilter, EditableUidFilter, EditableLockFilter, EditableStatusFilter {

    override val dataTypes: Array<out SyncObjectType> = arrayOf(SyncObjectType.COLLECTION)

    override fun getSpacesRestrictions(teamspaceAccessor: TeamspaceAccessor) =
        spaceFilter.getSpacesRestrictions(teamspaceAccessor)

    override val onlyOnUids: Array<out String>?
        get() = uidFilter.onlyOnUids

    override val requireUserUnlock: Boolean
        get() = lockFilter.requireUserUnlock

    override val onlyVisibleStatus: Boolean
        get() = statusFilter.onlyVisibleStatus

    var withVaultItem: CollectionVaultItems? = null
    var withoutVaultItem: CollectionVaultItems? = null
    var withVaultItemId: String? = null
    var withoutVaultItemId: String? = null
    var name: String? = null
}