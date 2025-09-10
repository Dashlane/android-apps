package com.dashlane.passwordhealth.identitydashboard.password

import com.dashlane.passwordanalysis.AnalyzedAuthentifiant
import com.dashlane.passwordanalysis.toAnalyzedAuthentifiant
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.manager.DataIdentifierSpaceCategorization
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.obfuscated.isNullOrEmpty
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthentifiantSecurityDataProvider @Inject constructor(
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val vaultDataQuery: VaultDataQuery,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
) {
    suspend fun getAuthentifiantsForAnalysis(
        teamSpace: TeamSpace,
        ignoreUserLock: Boolean,
    ): Pair<List<AnalyzedAuthentifiant>, List<AnalyzedAuthentifiant>> =
        withContext(ioDispatcher) {
            val teamSpaceAccessor = teamSpaceAccessorProvider.get()

            val forbiddenSpaceIds =
                teamSpaceAccessor?.pastBusinessTeams?.map { it.teamId } ?: emptyList()

            
            val allItems = vaultDataQuery.queryAll(
                vaultFilter {
                    if (ignoreUserLock) {
                        ignoreUserLock()
                    }
                    specificDataType(SyncObjectType.AUTHENTIFIANT)
                },
            ).mapNotNull { item ->
                val authentifiant =
                    item.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)
                        ?: return@mapNotNull null

                authentifiant.takeUnless {
                    it.syncObject.password.isNullOrEmpty() || it.syncObject.spaceId in forbiddenSpaceIds
                }?.toAnalyzedAuthentifiant()
            }
            val spaceCategorization = teamSpaceAccessorProvider.get()?.let {
                DataIdentifierSpaceCategorization(
                    teamspaceAccessor = it,
                    currentTeamSpaceUiFilter = currentTeamSpaceUiFilter,
                    forTeamspace = teamSpace,
                )
            }

            allItems to allItems.filter { spaceCategorization?.canBeDisplay(it.item) == true }
        }

    suspend fun getSecurityBreaches(ignoreUserLock: Boolean): List<SyncObject.SecurityBreach> =
        withContext(ioDispatcher) {
            val filter = vaultFilter {
                if (ignoreUserLock) {
                    ignoreUserLock()
                }
                specificDataType(SyncObjectType.SECURITY_BREACH)
            }
            vaultDataQuery.queryAll(filter)
                .mapNotNull { it.syncObject as? SyncObject.SecurityBreach }
                .distinctBy { it.breachId }
        }
}