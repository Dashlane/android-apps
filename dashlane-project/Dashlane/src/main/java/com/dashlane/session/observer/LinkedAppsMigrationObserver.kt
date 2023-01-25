package com.dashlane.session.observer

import com.dashlane.autofill.api.linkedservices.AppMetaDataToLinkedAppsMigration
import com.dashlane.autofill.api.linkedservices.RememberToLinkedAppsMigration
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LinkedAppsMigrationObserver @Inject constructor(
    private val appMetaDataToLinkedAppsMigration: AppMetaDataToLinkedAppsMigration,
    private val rememberToLinkedAppsMigration: RememberToLinkedAppsMigration
) : SessionObserver {

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        GlobalScope.launch {
            appMetaDataToLinkedAppsMigration.migrate()
            rememberToLinkedAppsMigration.migrate()
        }
    }
}