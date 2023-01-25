package com.dashlane.session.repository

import com.dashlane.core.premium.PremiumStatus
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.storage.securestorage.UserSecureStorageManager
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AccountStatusRepository @Inject constructor(private val storageUser: UserSecureStorageManager) :
    SessionObserver, BySessionRepository<PremiumStatus> {

    private var premiumStatuses = mutableMapOf<Session, PremiumStatus>()

    override fun get(session: Session?): PremiumStatus? = session?.let { getPremiumStatus(it) }

    fun getPremiumStatus(session: Session) = premiumStatuses.getOrElse(session) { reloadStatus(session) }

    fun reloadStatus(session: Session): PremiumStatus {
        val status = storageUser.readPremiumServerStatus(session)?.let { PremiumStatus(it, false) } ?: PremiumStatus()
        premiumStatuses[session] = status
        return status
    }
}