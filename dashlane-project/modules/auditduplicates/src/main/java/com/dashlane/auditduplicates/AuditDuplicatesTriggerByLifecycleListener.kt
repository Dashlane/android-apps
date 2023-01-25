package com.dashlane.auditduplicates

import android.app.Activity
import android.os.Bundle
import com.dashlane.auditduplicates.grouping.CalculateDuplicatesGroupsWithReload
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.userfeatures.UserFeaturesChecker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AuditDuplicatesTriggerByLifecycleListener @Inject constructor(
    @GlobalCoroutineScope
    globalCoroutineScope: CoroutineScope,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val mainDataAccessor: MainDataAccessor,
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val userPreferencesManager: UserPreferencesManager
) : AbstractActivityLifecycleListener() {

    private val credentialDataQuery: CredentialDataQuery
        get() = mainDataAccessor.getCredentialDataQuery()
    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val actor = globalCoroutineScope.actor<Audit>(capacity = Channel.CONFLATED) {
        for (command in this) {
            when (command) {
                is Audit.Start -> {
                    startAudit()
                }
            }
        }
    }

    sealed class Audit {
        object Start : Audit()
    }

    

    private suspend fun startAudit() {
        withContext(defaultCoroutineDispatcher) {
            val calculateDuplicatesGroups = CalculateDuplicatesGroupsWithReload(
                ReloadAuthentifiantWithMoreDetailFromVault(vaultDataQuery)
            )
            val auditDuplicatesLogger = AuditDuplicatesUsageLogger(sessionManager, bySessionUsageLogRepository)
            val auditDuplicatesRepository = AuditDuplicatesPreferencesRepository(userPreferencesManager)

            AuditDuplicates(
                userFeaturesChecker,
                credentialDataQuery,
                calculateDuplicatesGroups,
                auditDuplicatesLogger,
                auditDuplicatesRepository
            ).startAudit()
        }
    }

    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onFirstLoggedInActivityCreated(activity, savedInstanceState)
        runCatching { actor.trySend(Audit.Start) }
    }
}
