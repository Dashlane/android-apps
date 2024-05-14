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
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.xml.domain.SyncObjectType

class CredentialFilter(
    override var spaceFilter: SpaceFilter = NoSpaceFilter,
    override var uidFilter: UidFilter = NoUidFilter,
    override var lockFilter: LockFilter = DefaultLockFilter,
    override var statusFilter: StatusFilter = DefaultStatusFilter
) : BaseFilter, EditableSpaceFilter, EditableUidFilter, EditableLockFilter, EditableStatusFilter {

    override val dataTypes: Array<out SyncObjectType> = arrayOf(SyncObjectType.AUTHENTIFIANT)

    override fun getSpacesRestrictions(currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter) =
        spaceFilter.getSpacesRestrictions(currentTeamSpaceUiFilter)

    override val onlyOnUids: Array<out String>?
        get() = uidFilter.onlyOnUids

    override val requireUserUnlock: Boolean
        get() = lockFilter.requireUserUnlock

    var domains: Array<out String>? = null
        private set

    var allowSimilarDomains: Boolean = false
    var email: String? = null
    var packageName: String? = null

    fun forDomain(domain: String) {
        this.domains = arrayOf(domain)
    }

    fun forDomains(domains: Collection<String>) {
        this.domains = domains.toTypedArray()
    }

    override val onlyVisibleStatus: Boolean
        get() = statusFilter.onlyVisibleStatus
}