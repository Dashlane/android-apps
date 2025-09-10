package com.dashlane.session.observer

import com.dashlane.autofill.linkservices.services.migration.AppMetaDataToLinkedAppsMigration
import com.dashlane.autofill.linkservices.services.migration.RememberToLinkedAppsMigration
import com.dashlane.data.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.util.inject.OptionalProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LinkedAppsMigrationObserver @Inject constructor(
    private val appMetaDataToLinkedAppsMigration: AppMetaDataToLinkedAppsMigration,
    private val rememberToLinkedAppsMigration: RememberToLinkedAppsMigration,
    private val sessionCoroutineScopeProvider: OptionalProvider<CoroutineScope>,
) : SessionObserver {
    private val sessionCoroutineScope: CoroutineScope?
        get() = sessionCoroutineScopeProvider.get()

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        sessionCoroutineScope?.launch {
            appMetaDataToLinkedAppsMigration.migrate()
            rememberToLinkedAppsMigration.migrate()
        }
    }
}