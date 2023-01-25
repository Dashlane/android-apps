package com.dashlane.update

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.dashlane.events.AppEvents
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.tryOrNull
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AppUpdateInstaller @Inject constructor(
    @ApplicationContext
    context: Context,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val appEvents: AppEvents
) : SessionObserver {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)
    var availableUpdate: AppUpdateInfo? = null
        private set

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        if (updateNeeded()) {
            
            appEvents.post(AppUpdateNeededEvent())
        }
        if (availableUpdate?.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            
            appUpdateManager.startUpdateFlowForResult(
                availableUpdate,
                AppUpdateType.IMMEDIATE,
                
                { _, _, _, _, _, _, _ -> },
                INSTALL_UPDATE_REQUEST_CODE
            )
        }
    }

    

    fun installUpdate(activity: DashlaneActivity) {
        activity.lifecycleScope.launch(mainCoroutineDispatcher) {
            if (!updateNeeded()) return@launch
            appUpdateManager.startUpdateFlowForResult(
                availableUpdate,
                AppUpdateType.IMMEDIATE,
                activity,
                INSTALL_UPDATE_REQUEST_CODE
            )
        }
    }

    

    private suspend fun checkForUpdates() {
        if (availableUpdate != null) return
        availableUpdate = appUpdateManager.requestAppUpdateInfo()
    }

    

    private suspend fun updateNeeded(): Boolean = tryOrNull {
        if (availableUpdate == null) checkForUpdates()
        val appUpdateInfo = availableUpdate ?: return false

        val updateAvailableForInstall = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
        if (updateAvailableForInstall) {
            availableUpdate = appUpdateInfo
        }
        updateAvailableForInstall
    } ?: false

    companion object {
        const val INSTALL_UPDATE_REQUEST_CODE = 12987
    }
}