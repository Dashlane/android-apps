package com.dashlane.autofill.api.monitorlog

import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class MonitorAutofillIssuesSessionStartTrigger @JvmOverloads constructor(
    private val monitorAutofillIssues: MonitorAutofillIssues,
    private val monitorAutofillIssuesLogger: MonitorAutofillIssuesLogger,
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        sessionCoroutineScopeRepository.getCoroutineScope(session).launch(dispatcher) {
            runCatching {
                monitorAutofillIssuesLogger.logAutofillDeviceInfo(monitorAutofillIssues.collectAutofillStakeholdersInfo())
            }
        }
    }
}