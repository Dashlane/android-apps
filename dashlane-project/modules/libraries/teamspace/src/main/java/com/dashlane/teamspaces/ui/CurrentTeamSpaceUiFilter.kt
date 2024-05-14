package com.dashlane.teamspaces.ui

import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter.Companion.COMBINED_SPACE_FILTER_ID
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter.Companion.PERSONAL_SPACE_FILTER_ID
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface CurrentTeamSpaceUiFilter {

    val teamSpaceFilterState: StateFlow<SpaceFilterState>

    val currentFilter: SpaceFilterState

    fun updateFilter(teamSpace: TeamSpace)

    fun loadFilter()

    companion object {
        const val PERSONAL_SPACE_FILTER_ID = "personal"
        const val COMBINED_SPACE_FILTER_ID = "combined"
    }
}

class CurrentTeamSpaceUiFilterImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val userSecureStorage: UserSecureStorageManager,
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope,
    @DefaultCoroutineDispatcher private val dispatcher: CoroutineDispatcher
) : CurrentTeamSpaceUiFilter {

    private val _teamSpaceFilterState = MutableStateFlow<SpaceFilterState>(SpaceFilterState.Init)
    override val teamSpaceFilterState = _teamSpaceFilterState.asStateFlow()
    override val currentFilter: SpaceFilterState
        get() = _teamSpaceFilterState.value

    private val session: Session?
        get() = sessionManager.session

    init {
        loadFilter()
    }

    override fun updateFilter(teamSpace: TeamSpace) {
        session?.let {
            userSecureStorage.storeCurrentSpaceFilter(
                session = it,
                filterId = teamSpace.filterId
            )
            loadFilter()
        }
    }

    override fun loadFilter() {
        coroutineScope.launch(dispatcher) {
            
            val availableSpaces = teamSpaceAccessorProvider.get()?.availableSpaces
            availableSpaces?.singleOrNull()?.let {
                _teamSpaceFilterState.emit(SpaceFilterState.Loaded(it))
                return@launch
            }

            val cachedFilterId: String? = session?.let { userSecureStorage.readCurrentSpaceFilter(it) }
            val filter = teamSpaceAccessorProvider.get()?.availableSpaces
                ?.firstOrNull { it.filterId == cachedFilterId && it !is TeamSpace.Business.Past }
                ?: TeamSpace.Combined
            _teamSpaceFilterState.emit(SpaceFilterState.Loaded(filter))
        }
    }

    private val TeamSpace.filterId: String
        get() = when (this) {
            TeamSpace.Combined -> COMBINED_SPACE_FILTER_ID
            TeamSpace.Personal -> PERSONAL_SPACE_FILTER_ID
            is TeamSpace.Business -> teamId
        }
}