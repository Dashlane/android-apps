package com.dashlane.session.repository

import android.content.Context
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.useractivity.AggregateUserActivity
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogRepositoryAggregationDelegate
import dagger.Lazy
import kotlinx.coroutines.DelicateCoroutinesApi

class BySessionUsageLogRepository(
    private val applicationContext: Context,
    private val userDataRepository: UserDataRepository,
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
                    UsageLogRepositoryAggregationDelegate(aggregateUserActivity)
                }
            }
        }

    private inline fun <K, V> MutableMap<K, V>.getOrPutNullable(key: K, defaultValue: () -> V?): V? {
        return get(key) ?: defaultValue()?.also { put(key, it) }
    }
}