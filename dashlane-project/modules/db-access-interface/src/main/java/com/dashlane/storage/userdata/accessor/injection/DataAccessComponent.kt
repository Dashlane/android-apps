package com.dashlane.storage.userdata.accessor.injection

import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.util.inject.OptionalProvider



interface DataAccessComponent {
    val mainDataAccessor: MainDataAccessor
    val teamspaceAccessor: OptionalProvider<TeamspaceAccessor>
}