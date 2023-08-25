package com.dashlane.session.observer

import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver

class AnnouncementCenterObserver(private val announcementCenter: AnnouncementCenter) : SessionObserver {

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        
        announcementCenter.reset()
    }
}