package com.dashlane.update

import com.dashlane.data.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateInstaller @Inject constructor(
    private val appUpdateManager: AppUpdateManager,
) : SessionObserver {
    private val appUpdateFlow = MutableSharedFlow<AppUpdateInfo>()
    val sharedFlow = appUpdateFlow.asSharedFlow()

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        checkUpdate()
    }

    internal suspend fun checkUpdate() {
        try {
            val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()
            val updateAvailability = appUpdateInfo.updateAvailability()

            if (updateAvailability == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateFlow.emit(appUpdateInfo)
            } else if (updateAvailability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    
                    { _, _, _, _, _, _, _ -> },
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                    INSTALL_UPDATE_REQUEST_CODE,
                )
            }
        } catch (e: Exception) {
        }
    }

    companion object {
        const val INSTALL_UPDATE_REQUEST_CODE = 12987
    }
}