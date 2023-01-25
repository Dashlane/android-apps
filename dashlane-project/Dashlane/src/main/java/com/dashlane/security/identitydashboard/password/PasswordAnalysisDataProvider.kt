package com.dashlane.security.identitydashboard.password

import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.manager.TeamspaceManagerWeakListener
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.debug.DaDaDa
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PasswordAnalysisDataProvider @Inject constructor(
    private val mainDataAccessor: MainDataAccessor,
    private val authentifiantSecurityEvaluator: AuthentifiantSecurityEvaluator,
    private val dadada: DaDaDa,
    private val sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository
) : BaseDataProvider<PasswordAnalysisContract.Presenter>(), PasswordAnalysisContract.DataProvider,
    TeamspaceManager.Listener {

    private val credentialDataQuery: CredentialDataQuery
        get() = mainDataAccessor.getCredentialDataQuery()
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()
    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()
    private val dataSaver: DataSaver
        get() = mainDataAccessor.getDataSaver()

    override fun shouldDisplayProcessDuration() = dadada.isEnabled

    private val teamspaceManagerListener = TeamspaceManagerWeakListener(this)

    override fun listenForChanges() {
        sessionManager.session?.let {
            teamspaceManagerListener.listen(teamspaceRepository.getTeamspaceManager(it))
        }
    }

    override fun unlistenForChanges() {
        teamspaceManagerListener.listen(null) 
    }

    override suspend fun saveModified(authentifiant: VaultItem<SyncObject.Authentifiant>) {
        dataSaver.save(authentifiant.copyWithAttrs { syncState = SyncState.MODIFIED })
    }

    override suspend fun getAuthentifiantsSecurityInfo(): AuthentifiantSecurityEvaluator.Result? {
        return withContext(Dispatchers.Default) { getAuthentifiantSecurityInfoAsync() }
    }

    override fun onStatusChanged(teamspace: Teamspace?, previousStatus: String?, newStatus: String?) {
        
    }

    override fun onChange(teamspace: Teamspace?) {
        presenter.requireRefresh(true)
    }

    override fun onTeamspacesUpdate() {
        
    }

    private suspend fun getAuthentifiantSecurityInfoAsync(): AuthentifiantSecurityEvaluator.Result? {
        val teamspace = sessionManager.session?.let {
            teamspaceRepository.getTeamspaceManager(it)?.current
        } ?: return null
        return authentifiantSecurityEvaluator.computeResult(
            credentialDataQuery,
            genericDataQuery,
            vaultDataQuery,
            teamspace
        )
    }
}