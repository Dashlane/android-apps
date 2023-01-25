package com.dashlane.login.pages.password

import com.dashlane.core.sync.DataSyncHelper
import com.dashlane.session.Session
import com.dashlane.util.logI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class InitialSync(private val session: Session, private val dataSyncHelper: DataSyncHelper) {
    

    fun CoroutineScope.launchInitialSync() {
        launchInitialSync(session)
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun CoroutineScope.launchInitialSync(session: Session) = launch(Dispatchers.Default) {
        logI { "Launching initial sync" }
        launch { dataSyncHelper.runInitialSync(session) }
    }
}