package com.dashlane.accountstatus

import android.util.Log
import com.dashlane.accountstatus.AccountStatusRepository.Companion.DEFAULT_ACCOUNT_STATUS
import com.dashlane.login.LoginInfo
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo.B2cSubscription
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo.B2cSubscription.AutoRenewInfo
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.util.anonymize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import javax.inject.Inject

interface AccountStatusRepository : SessionObserver, BySessionRepository<AccountStatus> {

    val accountStatusState: StateFlow<Map<Session, AccountStatus>>

    suspend fun refreshFor(session: Session): Boolean

    suspend fun reloadStatus(session: Session): AccountStatus

    companion object {
        val DEFAULT_PREMIUM_STATUS = PremiumStatus(
            capabilities = listOf(),
            b2cStatus = PremiumStatus.B2cStatus(
                autoRenewal = false,
                isTrial = false,
                statusCode = PremiumStatus.B2cStatus.StatusCode.FREE
            )
        )
        val DEFAULT_SUBSCRIPTION_INFO = SubscriptionInfo(
            b2cSubscription = B2cSubscription(
                autoRenewInfo = AutoRenewInfo(
                    reality = false,
                    theory = false
                ),
                hasInvoices = false
            )
        )
        val DEFAULT_ACCOUNT_STATUS = AccountStatus(
            premiumStatus = DEFAULT_PREMIUM_STATUS,
            subscriptionInfo = DEFAULT_SUBSCRIPTION_INFO
        )
    }
}

class AccountStatusRepositoryImpl @Inject constructor(
    private val accountStatusStorage: AccountStatusStorage,
    private val accountStatusService: AccountStatusService,
    private val accountStatusListener: AccountStatusPostUpdateManager
) : AccountStatusRepository {

    private val _accountStatusState = MutableStateFlow<Map<Session, AccountStatus>>(mapOf())
    override val accountStatusState = _accountStatusState.asStateFlow()

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        super.sessionStarted(session, loginInfo)
        reloadStatus(session)
    }

    override fun get(session: Session?): AccountStatus? {
        session ?: return null
        return accountStatusState.value[session]
    }

    override suspend fun refreshFor(session: Session): Boolean {
        return runCatching {
            val accountStatus = accountStatusService.fetchAccountStatus(session)
            if (!accountStatusStorage.saveAccountStatus(session.localKey, session.username, accountStatus)) {
                throw IOException("PremiumStatus update failed")
            }
            updateAccountStatusForSession(session, accountStatus)
        }.onFailure {
            Log.d("ACCOUNT_STATUS", "Failed to fetch a valid AccountStatus\n${it.anonymize()}")
            
            reloadStatus(session)
        }.isSuccess
    }

    override suspend fun reloadStatus(session: Session): AccountStatus {
        val status = accountStatusStorage.readAccountStatus(session.localKey, session.username) ?: DEFAULT_ACCOUNT_STATUS
        updateAccountStatusForSession(session, status)
        return status
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        super.sessionEnded(session, byUser, forceLogout)
        _accountStatusState.emit(
            _accountStatusState.value.toMutableMap().also {
                it.remove(session)
            }.toMap()
        )
    }

    private suspend fun updateAccountStatusForSession(session: Session, accountStatus: AccountStatus) {
        val savedStatus = _accountStatusState.value[session]
        accountStatusListener.onUpdate(newStatus = accountStatus, oldStatus = savedStatus)
        _accountStatusState.emit(
            _accountStatusState.value.toMutableMap().also {
                it[session] = accountStatus
            }.toMap()
        )
    }
}