package com.dashlane.accountstatus

import com.dashlane.session.SessionManager
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class AccountStatusProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository
) : OptionalProvider<AccountStatus> {
    override fun get(): AccountStatus? = sessionManager.session?.let { accountStatusRepository[it] }
}