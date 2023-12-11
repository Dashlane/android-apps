package com.dashlane.session.repository

import com.dashlane.core.premium.PremiumStatus
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionObserver
import com.dashlane.storage.securestorage.UserSecureStorageManager
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountStatusRepository @Inject constructor(
    private val storageUser: UserSecureStorageManager,
    private val clock: Clock,
    private val sessionManager: SessionManager,
    private val userPreferencesManager: UserPreferencesManager
) :
    SessionObserver, BySessionRepository<PremiumStatus> {

    private var premiumStatuses = mutableMapOf<Session, PremiumStatus>()

    override fun get(session: Session?): PremiumStatus? = session?.let { getPremiumStatus(it) }

    fun getPremiumStatus(session: Session) =
        premiumStatuses.getOrElse(session) { reloadStatus(session) }

    fun reloadStatus(session: Session): PremiumStatus {
        val status = storageUser.readPremiumServerStatus(session)?.let {
            PremiumStatus(
                it,
                false,
                clock,
                sessionManager.session,
                userPreferencesManager
            )
        } ?: PremiumStatus(clock)
        premiumStatuses[session] = status
        return status
    }
}