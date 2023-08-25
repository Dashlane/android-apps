package com.dashlane.login.pages.password

import com.dashlane.core.sync.DataSyncHelper
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitialSync @Inject constructor(
    private val sessionManager: SessionManager,
    private val dataSyncHelper: DataSyncHelper
) {
    private val session: Session?
        get() = sessionManager.session

    fun CoroutineScope.launchInitialSync() {
        session?.run { launchInitialSync(this) }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun CoroutineScope.launchInitialSync(session: Session) = launch(Dispatchers.Default) {
        launch { dataSyncHelper.runInitialSync(session) }
    }
}
