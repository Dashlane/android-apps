package com.dashlane.ui.menu

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.hermes.generated.events.user.SelectSpace
import com.dashlane.login.lock.LockManager
import com.dashlane.navigation.Navigator
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.ui.menu.domain.BuildMenuNavigationUseCase
import com.dashlane.ui.menu.domain.MenuConfigurationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val navigator: Navigator,
    private val logRepository: LogRepository,
    private val accountStatusRepository: AccountStatusRepository,
    private val sessionManager: SessionManager,
    private val lockManager: LockManager,
    private val menuConfigurationProvider: MenuConfigurationProvider,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter
) : ViewModel(), NavController.OnDestinationChangedListener {

    private var teamspaceSelectionMode: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            refresh()
        }

    private val session: Session?
        get() = sessionManager.session

    private val _uiState: MutableStateFlow<MenuState> = MutableStateFlow(MenuState.Init)

    val uiState: StateFlow<MenuState>
        get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentTeamSpaceUiFilter.teamSpaceFilterState.collect {
                
                refresh()
            }
        }
        viewModelScope.launch {
            accountStatusRepository.accountStatusState.collect { accountStatuses ->
                accountStatuses[session]?.let {
                    refresh()
                }
            }
        }
    }

    fun onUpgradeClick() {
        navigator.goToOffers()
    }

    fun onHeaderProfileClick(canUpgrade: Boolean) {
        if (!canUpgrade) return
        navigator.goToCurrentPlan()
    }

    fun onHeaderTeamspaceSelectorClick() {
        teamspaceSelectionMode = !teamspaceSelectionMode
    }

    private fun onTeamspaceSelected(selectedSpace: TeamSpace) {
        val spaceFilter = session?.let { currentTeamSpaceUiFilter.currentFilter }

        if (spaceFilter != selectedSpace) {
            logRepository.queueEvent(SelectSpace(selectedSpace.toHermesSpace()))
        }
        sessionManager.session?.let { currentTeamSpaceUiFilter.updateFilter(selectedSpace) }
        teamspaceSelectionMode = false
        refresh()
    }

    private fun TeamSpace.toHermesSpace(): Space {
        return when (this) {
            TeamSpace.Personal -> Space.PERSONAL
            TeamSpace.Combined -> Space.ALL
            is TeamSpace.Business -> Space.PROFESSIONAL
        }
    }

    fun onLockout() {
        lockManager.lock()
    }

    fun refresh() {
        val configuration = menuConfigurationProvider.getConfiguration(teamspaceSelectionMode)
        val items = BuildMenuNavigationUseCase(navigator, configuration).build {
            onTeamspaceSelected(it)
        }
        _uiState.value = MenuState.Loaded(items = items)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        refresh()
    }
}