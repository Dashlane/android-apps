package com.dashlane.storage.userdata.accessor.filter

import com.dashlane.storage.userdata.accessor.filter.datatype.AllDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.DataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.EditableDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.lock.DefaultLockFilter
import com.dashlane.storage.userdata.accessor.filter.lock.EditableLockFilter
import com.dashlane.storage.userdata.accessor.filter.lock.LockFilter
import com.dashlane.storage.userdata.accessor.filter.sharing.EditableSharingPermissionFilter
import com.dashlane.storage.userdata.accessor.filter.sharing.NoSharingFilter
import com.dashlane.storage.userdata.accessor.filter.sharing.SharingFilter
import com.dashlane.storage.userdata.accessor.filter.space.EditableSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.space.SpaceFilter
import com.dashlane.storage.userdata.accessor.filter.status.DefaultStatusFilter
import com.dashlane.storage.userdata.accessor.filter.status.EditableStatusFilter
import com.dashlane.storage.userdata.accessor.filter.status.StatusFilter
import com.dashlane.storage.userdata.accessor.filter.uid.EditableUidFilter
import com.dashlane.storage.userdata.accessor.filter.uid.NoUidFilter
import com.dashlane.storage.userdata.accessor.filter.uid.SpecificUidFilter
import com.dashlane.storage.userdata.accessor.filter.uid.UidFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.xml.domain.SyncObjectType



class GenericFilter(
    override var dataTypeFilter: DataTypeFilter = AllDataTypeFilter,
    override var spaceFilter: SpaceFilter = NoSpaceFilter,
    override var uidFilter: UidFilter = NoUidFilter,
    override var sharingFilter: SharingFilter = NoSharingFilter,
    override var lockFilter: LockFilter = DefaultLockFilter,
    override var statusFilter: StatusFilter = DefaultStatusFilter
) : BaseFilter, EditableSpaceFilter, EditableDataTypeFilter, EditableUidFilter, EditableSharingPermissionFilter,
    EditableLockFilter, EditableStatusFilter {

    constructor(uid: String, dataType: SyncObjectType? = null) :
            this(
                uidFilter = SpecificUidFilter(uid),
                dataTypeFilter = dataType?.let { SpecificDataTypeFilter(it) } ?: AllDataTypeFilter
            )

    override fun getSpacesRestrictions(teamspaceAccessor: TeamspaceAccessor): Array<out Teamspace>? =
        spaceFilter.getSpacesRestrictions(teamspaceAccessor)

    override val dataTypes: Array<out SyncObjectType>
        get() = dataTypeFilter.dataTypes

    override val onlyOnUids: Array<out String>?
        get() = uidFilter.onlyOnUids

    override val sharingPermissions: Array<out String>?
        get() = sharingFilter.sharingPermissions

    override val requireUserUnlock: Boolean
        get() = lockFilter.requireUserUnlock

    override val onlyVisibleStatus: Boolean
        get() = statusFilter.onlyVisibleStatus
}