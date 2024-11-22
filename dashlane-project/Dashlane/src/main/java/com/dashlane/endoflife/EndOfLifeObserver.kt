package com.dashlane.endoflife

import android.app.Activity
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.announcements.PopupAnnouncement
import com.dashlane.announcements.displayers.SystemPopupDisplayer
import com.dashlane.announcements.modules.EndOfLifeAnnouncementModule
import com.dashlane.announcements.modules.EndOfLifeModuleProvider
import com.dashlane.login.LoginInfo
import com.dashlane.server.api.endpoints.platforms.AppVersionStatusService
import com.dashlane.session.Session
import com.dashlane.ui.util.DialogHelper
import javax.inject.Inject

class EndOfLifeObserver @Inject constructor(
    private val service: AppVersionStatusService,
    private val module: EndOfLifeAnnouncementModule,
    private val endOfLifeModuleProvider: EndOfLifeModuleProvider,
    private val dialogHelper: DialogHelper,
    private val announcementCenter: AnnouncementCenter
) : EndOfLife {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        runCatching {
            service.execute()
        }.onSuccess {
            module.registerAnnouncements(it.data)
        }
    }

    override suspend fun checkBeforeSession(activity: Activity) {
        runCatching {
            service.execute()
        }.onSuccess {
            showMessaging(activity, it.data)
        }
    }

    override fun showExpiredVersionMessaging(activity: Activity) {
        showMessaging(
            activity,
            AppVersionStatusService.Data(
                updatePossible = false,
                daysBeforeExpiration = 0,
                userSupportLink = null,
                status = AppVersionStatusService.Data.Status.EXPIRED_VERSION
            )
        )
    }

    private fun showMessaging(activity: Activity, data: AppVersionStatusService.Data) {
        val content =
            endOfLifeModuleProvider.createSystemPopupContent(data) ?: return
        val announcement = PopupAnnouncement(EndOfLifeAnnouncementModule.ID, content)
        endOfLifeModuleProvider.addDisplayCondition(announcement, data)
        SystemPopupDisplayer(dialogHelper, announcementCenter).display(activity, announcement)
    }
}
