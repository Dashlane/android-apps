package com.dashlane.storage.userdata.accessor.filter

import com.dashlane.storage.userdata.accessor.filter.datatype.AllDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.DataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.EditableDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.lock.DefaultLockFilter
import com.dashlane.storage.userdata.accessor.filter.lock.EditableLockFilter
import com.dashlane.storage.userdata.accessor.filter.lock.LockFilter
import com.dashlane.storage.userdata.accessor.filter.space.EditableSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.space.SpaceFilter
import com.dashlane.storage.userdata.accessor.filter.status.DefaultStatusFilter
import com.dashlane.storage.userdata.accessor.filter.status.EditableStatusFilter
import com.dashlane.storage.userdata.accessor.filter.status.StatusFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.xml.domain.SyncObjectType



data class CounterFilter(
    override var dataTypeFilter: DataTypeFilter = AllDataTypeFilter,
    override var spaceFilter: SpaceFilter = NoSpaceFilter,
    override var lockFilter: LockFilter = DefaultLockFilter,
    override var statusFilter: StatusFilter = DefaultStatusFilter
) : BaseFilter, EditableDataTypeFilter, EditableSpaceFilter, EditableLockFilter, EditableStatusFilter {

    override fun getSpacesRestrictions(teamspaceAccessor: TeamspaceAccessor) =
        spaceFilter.getSpacesRestrictions(teamspaceAccessor)

    override val dataTypes: Array<out SyncObjectType>
        get() = dataTypeFilter.dataTypes

    override val requireUserUnlock: Boolean
        get() = lockFilter.requireUserUnlock

    override val onlyVisibleStatus: Boolean
        get() = statusFilter.onlyVisibleStatus
}