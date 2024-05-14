package com.dashlane.teamspaces.manager

import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.session.SessionManager
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class TeamSpaceAccessorProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository
) : OptionalProvider<TeamSpaceAccessor> {

    override fun get(): TeamSpaceAccessor? {
        val session = sessionManager.session ?: return null

        return TeamSpaceAccessorImpl(
            session = session,
            accountStatusRepository = accountStatusRepository
        )
    }
}