package com.dashlane.security.identitydashboard.password

import com.dashlane.debug.services.DaDaDaBase
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PasswordAnalysisDataProvider @Inject constructor(
    private val dataSaver: DataSaver,
    private val authentifiantSecurityEvaluator: AuthentifiantSecurityEvaluator,
    private val dadada: DaDaDaBase,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
    @ApplicationCoroutineScope
    private val coroutineScope: CoroutineScope
) : BaseDataProvider<PasswordAnalysisContract.Presenter>(), PasswordAnalysisContract.DataProvider {

    private var latestSecurityScoreEvaluatorResult: Deferred<AuthentifiantSecurityEvaluator.Result?>? =
        null

    override fun shouldDisplayProcessDuration() = dadada.isEnabled

    override suspend fun saveModified(authentifiant: VaultItem<SyncObject.Authentifiant>) {
        dataSaver.save(authentifiant.copyWithAttrs { syncState = SyncState.MODIFIED })
    }

    override suspend fun getAuthentifiantsSecurityInfo(forceUpdate: Boolean): AuthentifiantSecurityEvaluator.Result? {
        return withContext(Dispatchers.Default) { getAuthentifiantSecurityInfoAsync(forceUpdate) }
    }

    private suspend fun getAuthentifiantSecurityInfoAsync(forceUpdate: Boolean): AuthentifiantSecurityEvaluator.Result? {
        if (forceUpdate) {
            latestSecurityScoreEvaluatorResult?.cancel()
            latestSecurityScoreEvaluatorResult = null
        }
        val result =
            latestSecurityScoreEvaluatorResult ?: coroutineScope.async(Dispatchers.Default) {
                authentifiantSecurityEvaluator.computeResult(currentTeamSpaceUiFilter.currentFilter.teamSpace)
            }.apply {
                latestSecurityScoreEvaluatorResult?.cancel()
                latestSecurityScoreEvaluatorResult = this
            }
        return result.await()
    }
}