package com.dashlane.security

import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.identitydashboard.password.AuthentifiantSecurityEvaluator
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.teamspaces.manager.TeamspaceManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject



class UsageLogCode12SecurityScore @Inject constructor(
    private val authentifiantSecurityEvaluator: AuthentifiantSecurityEvaluator,
    private val userPreferencesManager: UserPreferencesManager,
    private val mainDataAccessor: MainDataAccessor,
) {

    private val credentialDataQuery: CredentialDataQuery
        get() = mainDataAccessor.getCredentialDataQuery()
    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()
    private val genericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    

    @OptIn(DelicateCoroutinesApi::class)
    fun sendIfRequire() {
        val lastSentTimestamp = userPreferencesManager.getLong(UsageLogCode12SecurityScore.PREF_LAST_SENT_TIMESTAMP, 0)
        if ((System.currentTimeMillis() - lastSentTimestamp) < MAX_TIME_BETWEEN_UL12) {
            
            return
        }
        
        GlobalScope.launch {
            authentifiantSecurityEvaluator
                .computeResult(
                    credentialDataQuery,
                    genericDataQuery,
                    vaultDataQuery,
                    TeamspaceManager.COMBINED_TEAMSPACE
                )
        }
    }

    companion object {
        private const val PREF_LAST_SENT_TIMESTAMP = "last_sent_ul12_score_all_space"
        private const val MAX_TIME_BETWEEN_UL12 = 7 * 24 * 3600 * 1000 

        fun markAsSent(userPreferencesManager: UserPreferencesManager) {
            userPreferencesManager.putLong(
                UsageLogCode12SecurityScore.PREF_LAST_SENT_TIMESTAMP,
                System.currentTimeMillis()
            )
        }
    }
}