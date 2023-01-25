package com.dashlane.session.repository

import android.content.Context
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.debug.TestAccountDebug.isTestingAccount
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.useractivity.AggregateUserActivity
import com.dashlane.useractivity.log.DeviceExtraData
import com.dashlane.useractivity.log.UserActivityFlush
import com.dashlane.useractivity.log.usage.UsageLogContext
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogRepositoryAggregationDelegate
import com.dashlane.useractivity.log.usage.UsageLogsDataStore
import dagger.Lazy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import java.time.Clock



class BySessionUsageLogRepository(
    private val applicationContext: Context,
    private val deviceExtraData: DeviceExtraData,
    private val userDataRepository: UserDataRepository,
    private val dataStore: UsageLogsDataStore,
    private val flush: UserActivityFlush,
    private val aggregateUserActivityLazy: Lazy<AggregateUserActivity>
) : SessionObserver, BySessionRepository<UsageLogRepository> {

    private val bySession = mutableMapOf<Session, UsageLogRepository>()

    private val aggregateUserActivity
        get() = aggregateUserActivityLazy.get()

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        bySession.remove(session)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun get(session: Session?): UsageLogRepository? =
        session?.let {
            bySession.getOrPutNullable(session) {
                val anonymousUserId =
                    userDataRepository[session]?.getSettings()?.anonymousUserId?.takeUnless { it.isEmpty() }
                if (anonymousUserId == null) {
                    DeveloperUtilities.throwRuntimeExceptionDebug(
                        applicationContext,
                        "Send UsageLog but `anonymousUserId` not available"
                    )
                    null
                } else {
                    UsageLogRepositoryAggregationDelegate(
                        UsageLogRepository(
                            GlobalScope,
                            dataStore,
                            UserExtraData(
                                deviceExtraData,
                                anonymousUserId,
                                session.sessionId,
                                session.userId.takeIf { session.isTestingAccount() }
                            ),
                            flush,
                            Clock.systemDefaultZone()
                        ),
                        aggregateUserActivity
                    )
                }
            }
        }

    private inline fun <K, V> MutableMap<K, V>.getOrPutNullable(key: K, defaultValue: () -> V?): V? {
        return get(key) ?: defaultValue()?.also { put(key, it) }
    }

    private data class UserExtraData(
        val deviceExtraData: DeviceExtraData,
        val anonymousUserId: String,
        val sessionId: String,
        val testAccountLogin: String? = null
    ) : UsageLogContext.Provider {

        override val usageLogContext: UsageLogContext
            get() = UsageLogContext(
                anonymousUserId = anonymousUserId,
                anonymousDeviceId = deviceExtraData.anonymousDeviceId,
                sessionIdentifier = sessionId,
                appVersion = deviceExtraData.dashlaneVersion,
                osVersion = deviceExtraData.osVersion,
                testAccountLogin = testAccountLogin
            )
    }
}