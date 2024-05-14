package com.dashlane.security.identitydashboard.password

import com.dashlane.debug.DaDaDa
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PasswordAnalysisDataProvider @Inject constructor(
    private val dataSaver: DataSaver,
    private val authentifiantSecurityEvaluator: AuthentifiantSecurityEvaluator,
    private val dadada: DaDaDa,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter
) : BaseDataProvider<PasswordAnalysisContract.Presenter>(), PasswordAnalysisContract.DataProvider {

    override fun shouldDisplayProcessDuration() = dadada.isEnabled

    override suspend fun saveModified(authentifiant: VaultItem<SyncObject.Authentifiant>) {
        dataSaver.save(authentifiant.copyWithAttrs { syncState = SyncState.MODIFIED })
    }

    override suspend fun getAuthentifiantsSecurityInfo(): AuthentifiantSecurityEvaluator.Result? {
        return withContext(Dispatchers.Default) { getAuthentifiantSecurityInfoAsync() }
    }

    private suspend fun getAuthentifiantSecurityInfoAsync(): AuthentifiantSecurityEvaluator.Result? {
        return authentifiantSecurityEvaluator.computeResult(currentTeamSpaceUiFilter.currentFilter.teamSpace)
    }
}