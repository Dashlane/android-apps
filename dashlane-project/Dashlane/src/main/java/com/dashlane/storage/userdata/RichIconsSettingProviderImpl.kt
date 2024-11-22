package com.dashlane.storage.userdata

import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class RichIconsSettingProviderImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDataRepository,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>
) : RichIconsSettingProvider {

    override val editable: Boolean
        get() = teamSpaceAccessorProvider.get()?.isRichIconsEnabled != false

    override val richIcons: Boolean
        get() =
            if (!editable) {
                false
            } else {
                sessionManager.session?.let {
                    userDataRepository[it]?.getSettings()?.richIcons
                } ?: true
            }
}